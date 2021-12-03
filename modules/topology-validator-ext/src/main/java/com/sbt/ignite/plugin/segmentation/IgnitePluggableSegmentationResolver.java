/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sbt.ignite.plugin.segmentation;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import org.apache.ignite.IgniteException;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.cluster.BaselineNode;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.events.DiscoveryEvent;
import org.apache.ignite.internal.GridKernalContext;
import org.apache.ignite.internal.cluster.DetachedClusterNode;
import org.apache.ignite.internal.managers.discovery.DiscoCache;
import org.apache.ignite.internal.managers.eventstorage.DiscoveryEventListener;
import org.apache.ignite.internal.managers.eventstorage.HighPriorityListener;
import org.apache.ignite.internal.processors.affinity.AffinityTopologyVersion;
import org.apache.ignite.internal.processors.cluster.ChangeGlobalStateFinishMessage;
import org.apache.ignite.internal.processors.configuration.distributed.DistributedConfigurationLifecycleListener;
import org.apache.ignite.internal.processors.configuration.distributed.DistributedPropertyDispatcher;
import org.apache.ignite.internal.processors.configuration.distributed.SimpleDistributedProperty;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.thread.IgniteThreadPoolExecutor;
import org.apache.ignite.thread.OomExceptionHandler;

import static java.lang.Boolean.FALSE;
import static org.apache.ignite.cluster.ClusterState.ACTIVE;
import static org.apache.ignite.cluster.ClusterState.ACTIVE_READ_ONLY;
import static org.apache.ignite.configuration.IgniteConfiguration.DFLT_THREAD_KEEP_ALIVE_TIME;
import static org.apache.ignite.events.EventType.EVT_NODE_FAILED;
import static org.apache.ignite.events.EventType.EVT_NODE_JOINED;
import static org.apache.ignite.events.EventType.EVT_NODE_LEFT;
import static org.apache.ignite.internal.cluster.DistributedConfigurationUtils.setDefaultValue;
import static org.apache.ignite.internal.managers.communication.GridIoPolicy.UNDEFINED;

/** */
public class IgnitePluggableSegmentationResolver implements PluggableSegmentationResolver {
    /** */
    public static final String SEG_RESOLVER_ENABLED_PROP_NAME = "org.apache.ignite.segmentation.resolver.enabled";

    /** */
    private static final String SEG_RESOLVER_THREAD_PREFIX = "segmentation-resolver";

    /** */
    private static final int[] TOP_CHANGED_EVTS = new int[] {
        EVT_NODE_LEFT,
        EVT_NODE_JOINED,
        EVT_NODE_FAILED
    };

    /** */
    private final SimpleDistributedProperty<Boolean> segResolverEnabledProp = new SimpleDistributedProperty<>(
        SEG_RESOLVER_ENABLED_PROP_NAME,
        Boolean::parseBoolean
    );

    /** Ignite kernel context. */
    private final GridKernalContext ctx;

    /** Ignite logger. */
    private final IgniteLogger log;

    /** */
    private final IgniteThreadPoolExecutor stateChangeExec;

    /** */
    private long lastCheckedTopVer;

    /** */
    private volatile State state;

    /** @param ctx Ignite kernel context. */
    public IgnitePluggableSegmentationResolver(GridKernalContext ctx) {
        this.ctx = ctx;

        log = ctx.log(getClass());

        stateChangeExec = new IgniteThreadPoolExecutor(
            SEG_RESOLVER_THREAD_PREFIX,
            ctx.igniteInstanceName(),
            1,
            1,
            DFLT_THREAD_KEEP_ALIVE_TIME,
            new LinkedBlockingQueue<>(),
            UNDEFINED,
            new OomExceptionHandler(ctx));

        stateChangeExec.allowCoreThreadTimeOut(true);
    }

    /** {@inheritDoc} */
    @Override public boolean isValidSegment() {
        return isDisabled() || state != State.INVALID;
    }

    /** */
    public void start() {
        if (ctx.clientNode())
            return;

        onGlobalClusterStateChanged(ctx.state().clusterState().state());

        ctx.event().addDiscoveryEventListener(new TopologyChangedEventListener(), TOP_CHANGED_EVTS);

        ctx.discovery().setCustomEventListener(
            ChangeGlobalStateFinishMessage.class,
            (topVer, snd, msg) -> onGlobalClusterStateChanged(msg.state())
        );

        ctx.state().baselineConfiguration().listenAutoAdjustTimeout((name, oldVal, newVal) -> {
            if (newVal != null)
                checkBaselineAutoAdjustConfiguration(ctx.state().isBaselineAutoAdjustEnabled(), newVal);
        });

        ctx.state().baselineConfiguration().listenAutoAdjustEnabled((name, oldVal, newVal) -> {
           if (newVal != null)
               checkBaselineAutoAdjustConfiguration(newVal, ctx.state().baselineAutoAdjustTimeout());
        });

        ctx.internalSubscriptionProcessor().registerDistributedConfigurationListener(
            new DistributedConfigurationLifecycleListener() {
                /** {@inheritDoc} */
                @Override public void onReadyToRegister(DistributedPropertyDispatcher dispatcher) {
                    dispatcher.registerProperty(segResolverEnabledProp);
                }

                /** {@inheritDoc} */
                @Override public void onReadyToWrite() {
                    boolean isLocNodeCrd = U.isLocalNodeCoordinator(ctx.discovery());

                    Boolean segResolverEnabled = segResolverEnabledProp.get();

                    if (segResolverEnabled == null && !isLocNodeCrd || FALSE.equals(segResolverEnabled)) {
                        U.warn(log, "Segmentation Resolver will be disabled because it is not configured for the" +
                            " cluster the current node joined. Make sure the Segmentation Resolver plugin is" +
                            " configured on all cluster nodes.");
                    }

                    setDefaultValue(segResolverEnabledProp, isLocNodeCrd, log);
                }
            });
    }

    /** @return Discovery data. */
    public Serializable provideDiscoveryData() {
        return state;
    }

    /**
     * @param data Discovery data.
     * @param joiningNodeId ID of the joining node.
     */
    public void onDiscoveryDataReceived(UUID joiningNodeId, Serializable data) {
        if (ctx.localNodeId().equals(joiningNodeId))
            state = (State)data;
    }

    /**
     * @param node Joining node.
     * @param data Joining node discovery data.
     */
    public void validateNewNode(ClusterNode node, Serializable data) {
        if (node.isClient())
            return;

        if (data == null) {
            throw new IgniteException("The Segmentation Resolver plugin is not configured for the server node that is" +
                " trying to join the cluster. Since the Segmentation Resolver is only applicable if all server nodes" +
                " in the cluster have one, node join request will be rejected [rejectedNodeId=" + node.id() + ']');
        }

        // If the new node is joining but some node failed/left events has not been handled by
        // {@linkTopologyChangedEventListener} yet, we cannot guarantee that the {@link state} on the joining node will
        // be consistent with one on the cluster nodes.
        if (state == State.VALID) {
            DiscoCache discoCache = ctx.discovery().discoCache(new AffinityTopologyVersion(lastCheckedTopVer, 0));

            if (discoCache != null) {
                for (ClusterNode srv : discoCache.serverNodes()) {
                    if (!ctx.discovery().alive(srv))
                        throw new IgniteException("Node join request will be rejected due to concurrent node left" +
                            " process handling [rejectedNodeId=" + node.id() + ']');
                }
            }
        }
    }

    /** */
    private boolean isDisabled() {
        return !segResolverEnabledProp.getOrDefault(false);
    }

    /** */
    private void onGlobalClusterStateChanged(ClusterState clusterState) {
        state = clusterState == ACTIVE ? State.VALID : State.CLUSTER_WRITE_BLOCKED;
    }

    /**
     * Current implementation of segmentation detection compares node of each topology with configured baseline nodes.
     * If baseline auto adjustment is configured with zero timeout - baseline is updated on each topology change
     * and the comparison described above makes no sense.
     */
    private boolean checkBaselineAutoAdjustConfiguration(boolean enabled, long timeout) {
        if (isDisabled())
            return false;

        if (enabled && timeout == 0L) {
            U.warn(log, "Segmentation Resolver is currently skipping validation of topology changes because" +
                " Baseline Auto Adjustment with zero timeout is configured for the cluster. Configure" +
                " Baseline Nodes explicitly or set Baseline Auto Adjustment Timeout to greater than zero.");

            return false;
        }

        return true;
    }

    /** */
    private class TopologyChangedEventListener implements DiscoveryEventListener, HighPriorityListener {
        /** {@inheritDoc} */
        @Override public void onEvent(DiscoveryEvent evt, DiscoCache discoCache) {
            State locStateCopy = state;

            lastCheckedTopVer = evt.topologyVersion();

            boolean skipTopChangeCheck = !checkBaselineAutoAdjustConfiguration(
                ctx.state().isBaselineAutoAdjustEnabled(),
                ctx.state().baselineAutoAdjustTimeout()
            );

            if (!skipTopChangeCheck && state == State.VALID && evt.type() == EVT_NODE_FAILED) {
                List<? extends BaselineNode> baselineNodes = discoCache.baselineNodes();

                // Actually Ignite considers segmentation as the sequential node failures. So we detect segmentation
                // even if the single node fails and less than half of baseline nodes are alive.
                if (baselineNodes != null && aliveBaselineNodes(baselineNodes) < baselineNodes.size() / 2 + 1) {
                    locStateCopy = State.INVALID;

                    // The dedicated thread pool is used here to avoid execution of any blocking operation inside
                    // the discovery worker.
                    stateChangeExec.execute(() -> {
                        try {
                            ctx.cluster().get().state(ACTIVE_READ_ONLY);
                        }
                        catch (Throwable e) {
                            U.error(
                                log,
                                "Failed to automatically switch state of the segmented cluster to the READ-ONLY" +
                                    " mode. Cache writes were already restricted for all configured caches, but this" +
                                    " step is still required in order to be able to unlock cache writes in the future." +
                                    " Retry this operation manually, if possible [segmentedNodes=" +
                                    formatTopologyNodes(discoCache.allNodes()) + "]",
                                e
                            );
                        }
                    });

                    U.warn(log, "Cluster segmentation was detected [segmentedNodes=" +
                        formatTopologyNodes(discoCache.allNodes()) + ']');
                }
            }

            state = locStateCopy;
        }

        /** {@inheritDoc} */
        @Override public int order() {
            return 0;
        }

        /**
         * @return Count of alive baseline nodes.
         * Note that the following implementation is tied to how {@link DiscoCache#baselineNodes()} collection is
         * populated.
         */
        private int aliveBaselineNodes(Collection<? extends BaselineNode> baselineNodes) {
            int res = 0;

            for (BaselineNode node : baselineNodes) {
                if (!(node instanceof DetachedClusterNode))
                    ++res;
            }

            return res;
        }

        /** @return String representation of the specified cluster node collection. */
        private String formatTopologyNodes(Collection<ClusterNode> nodes) {
            return nodes.stream().map(n -> n.id().toString()).collect(Collectors.joining(", "));
        }
    }

    /** Represents possible states of the current segment. */
    private enum State {
        /** Cluster is in the valida state. No segmentation have happened, or it has been successfully resolved. */
        VALID,

        /**
         * This state is applied during a period of time when cluster segmentation had been detected locally but global
         * cluster state wasn't changed to {@link ClusterState#ACTIVE_READ_ONLY} yet.
         */
        INVALID,

        /**
         *  Cluster state is set to {@link ClusterState#INACTIVE} or {@link ClusterState#ACTIVE_READ_ONLY}. So no cache
         *  writes are not available and hence nothing we should worry about.
         */
        CLUSTER_WRITE_BLOCKED
    }
}
