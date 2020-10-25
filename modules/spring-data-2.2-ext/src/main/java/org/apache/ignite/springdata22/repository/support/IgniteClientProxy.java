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

package org.apache.ignite.springdata22.repository.support;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import javax.cache.CacheException;
import org.apache.ignite.DataRegionMetrics;
import org.apache.ignite.DataStorageMetrics;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicLong;
import org.apache.ignite.IgniteAtomicReference;
import org.apache.ignite.IgniteAtomicSequence;
import org.apache.ignite.IgniteAtomicStamped;
import org.apache.ignite.IgniteBinary;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.IgniteCountDownLatch;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.IgniteEncryption;
import org.apache.ignite.IgniteEvents;
import org.apache.ignite.IgniteException;
import org.apache.ignite.IgniteLock;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.IgniteMessaging;
import org.apache.ignite.IgniteQueue;
import org.apache.ignite.IgniteScheduler;
import org.apache.ignite.IgniteSemaphore;
import org.apache.ignite.IgniteServices;
import org.apache.ignite.IgniteSet;
import org.apache.ignite.IgniteSnapshot;
import org.apache.ignite.IgniteTransactions;
import org.apache.ignite.MemoryMetrics;
import org.apache.ignite.PersistenceMetrics;
import org.apache.ignite.cache.affinity.Affinity;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.configuration.AtomicConfiguration;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.CollectionConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.NearCacheConfiguration;
import org.apache.ignite.lang.IgniteProductVersion;
import org.apache.ignite.plugin.IgnitePlugin;
import org.apache.ignite.plugin.PluginNotFoundException;
import org.apache.ignite.spi.tracing.TracingConfigurationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Represents {@link Ignite} proxy which delegates operations to {@link IgniteClient}. */
public class IgniteClientProxy implements Ignite {
    /** Error message indicating that operation is unsupported when thin client is used for accesing the Ignite cluster. */
    static final String UNSUPPORTED_ERR_MSG = "Operation is unsupported when a thin client is used for " +
        "accessing the Ignite cluester. Use an Ingite node instance instead.";
    
    /** {@link IgniteClient} instance that is used as a delegate. */
    private final IgniteClient cli;

    /** */
    public IgniteClientProxy(IgniteClient cli) {
        this.cli = cli;
    }

    /** {@inheritDoc} */
    @Override public String name() {
       throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteLogger log() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteConfiguration configuration() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteCluster cluster() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteCompute compute() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteCompute compute(ClusterGroup grp) {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteMessaging message() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteMessaging message(ClusterGroup grp) {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteEvents events() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteEvents events(ClusterGroup grp) {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteServices services() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteServices services(ClusterGroup grp) {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public ExecutorService executorService() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public ExecutorService executorService(ClusterGroup grp) {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteProductVersion version() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteScheduler scheduler() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <K, V> IgniteCache<K, V> createCache(CacheConfiguration<K, V> cacheCfg) throws CacheException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public Collection<IgniteCache> createCaches(
        Collection<CacheConfiguration> cacheCfgs) throws CacheException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <K, V> IgniteCache<K, V> createCache(String cacheName) throws CacheException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <K, V> IgniteCache<K, V> getOrCreateCache(CacheConfiguration<K, V> cacheCfg) throws CacheException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <K, V> IgniteCache<K, V> getOrCreateCache(String name) {
        return new IgniteCacheClientProxy<>(cli.getOrCreateCache(name));
    }

    /** {@inheritDoc} */
    @Override public Collection<IgniteCache> getOrCreateCaches(
        Collection<CacheConfiguration> cacheCfgs) throws CacheException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <K, V> void addCacheConfiguration(CacheConfiguration<K, V> cacheCfg) throws CacheException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <K, V> IgniteCache<K, V> createCache(CacheConfiguration<K, V> cacheCfg,
        NearCacheConfiguration<K, V> nearCfg) throws CacheException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <K, V> IgniteCache<K, V> getOrCreateCache(CacheConfiguration<K, V> cacheCfg,
        NearCacheConfiguration<K, V> nearCfg) throws CacheException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <K, V> IgniteCache<K, V> createNearCache(String cacheName,
        NearCacheConfiguration<K, V> nearCfg) throws CacheException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <K, V> IgniteCache<K, V> getOrCreateNearCache(String cacheName,
        NearCacheConfiguration<K, V> nearCfg) throws CacheException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public void destroyCache(String cacheName) throws CacheException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public void destroyCaches(Collection<String> cacheNames) throws CacheException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <K, V> IgniteCache<K, V> cache(String name) {
        return new IgniteCacheClientProxy<>(cli.cache(name));
    }

    /** {@inheritDoc} */
    @Override public Collection<String> cacheNames() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteTransactions transactions() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <K, V> IgniteDataStreamer<K, V> dataStreamer(String cacheName) throws IllegalStateException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteAtomicSequence atomicSequence(String name, long initVal,
        boolean create) throws IgniteException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteAtomicSequence atomicSequence(String name, AtomicConfiguration cfg, long initVal,
        boolean create) throws IgniteException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteAtomicLong atomicLong(String name, long initVal, boolean create) throws IgniteException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteAtomicLong atomicLong(String name, AtomicConfiguration cfg, long initVal,
        boolean create) throws IgniteException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <T> IgniteAtomicReference<T> atomicReference(String name, @Nullable T initVal,
        boolean create) throws IgniteException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <T> IgniteAtomicReference<T> atomicReference(String name, AtomicConfiguration cfg,
        @Nullable T initVal, boolean create) throws IgniteException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <T, S> IgniteAtomicStamped<T, S> atomicStamped(String name, @Nullable T initVal,
        @Nullable S initStamp, boolean create) throws IgniteException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <T, S> IgniteAtomicStamped<T, S> atomicStamped(String name, AtomicConfiguration cfg,
        @Nullable T initVal, @Nullable S initStamp, boolean create) throws IgniteException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteCountDownLatch countDownLatch(String name, int cnt, boolean autoDel,
        boolean create) throws IgniteException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteSemaphore semaphore(String name, int cnt, boolean failoverSafe,
        boolean create) throws IgniteException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteLock reentrantLock(String name, boolean failoverSafe, boolean fair,
        boolean create) throws IgniteException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <T> IgniteQueue<T> queue(String name, int cap,
        @Nullable CollectionConfiguration cfg) throws IgniteException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <T> IgniteSet<T> set(String name, @Nullable CollectionConfiguration cfg) throws IgniteException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <T extends IgnitePlugin> T plugin(String name) throws PluginNotFoundException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteBinary binary() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public void close() throws IgniteException {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <K> Affinity<K> affinity(String cacheName) {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public boolean active() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public void active(boolean active) {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public void resetLostPartitions(Collection<String> cacheNames) {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public Collection<MemoryMetrics> memoryMetrics() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public @Nullable MemoryMetrics memoryMetrics(String memPlcName) {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public PersistenceMetrics persistentStoreMetrics() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public Collection<DataRegionMetrics> dataRegionMetrics() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public @Nullable DataRegionMetrics dataRegionMetrics(String memPlcName) {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public DataStorageMetrics dataStorageMetrics() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteEncryption encryption() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public IgniteSnapshot snapshot() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public @NotNull TracingConfigurationManager tracingConfiguration() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }
}
