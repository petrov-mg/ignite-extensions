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

package org.apache.ignite.cache.spring;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCacheConfiguration;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.apache.ignite.internal.util.typedef.internal.A;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * Represents implementation of {@link AbstractCacheManager} that uses thin client to connect to an Ignite cluster
 * and obtain an Ignite cache instance. It requires {@link IgniteClient} instance or {@link ClientConfiguration} to be
 * set before manager use (see {@link #setClientInstance(IgniteClient),
 * {@link #setClientConfiguration(ClientConfiguration)}}).
 *
 * You can provide Ignite client instance to a Spring configuration XML file, like below:
 *
 * <pre name="code" class="xml">
 * &lt;beans xmlns="http://www.springframework.org/schema/beans"
 *        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *        xmlns:tx="http://www.springframework.org/schema/tx"
 *        xsi:schemaLocation="
 *            http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
 *            http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx.xsd"&gt;
 *     &lt;-- Provide Ignite client instance. --&gt;
 *     &lt;bean id="cacheManager" class="org.apache.ignite.cache.spring.IgniteClientSpringCacheManager"&gt;
 *         &lt;property name="clientInstance" ref="igniteClientBean"/&gt;
 *     &lt;/bean>
 *
 *     &lt;-- Use annotation-driven cache configuration. --&gt;
 *     &lt;cache:annotation-driven/&gt;
 * &lt;/beans&gt;
 * </pre>
 */
public class IgniteClientSpringCacheManager extends AbstractCacheManager implements DisposableBean, SmartInitializingSingleton {
    /** Ignite client instance. */
    private IgniteClient cli;

    /** Ignite client configuration. */
    private ClientConfiguration cliCfg;

    /** Cache configurations mapped to cache name. */
    private Map<String, ClientCacheConfiguration> ccfgs;

    /** Flag that indicates whether Ignite client instance was set explicitly. */
    private boolean externalCliInstance;

    /** Gets Ignite client instance. */
    public IgniteClient getClientInstance() {
        return cli;
    }

    /** Sets Ignite client instance. */
    public IgniteClientSpringCacheManager setClientInstance(IgniteClient cli) {
        A.notNull(cli, "cli");

        this.cli = cli;

        externalCliInstance = true;

        return this;
    }

    /** Gets Ignite client configuration. */
    public ClientConfiguration getClientConfiguration() {
        return cliCfg;
    }

    /** Sets Ignite client configuration that will be used to start the client instance by the manager. */
    public IgniteClientSpringCacheManager setClientConfiguration(ClientConfiguration cliCfg) {
        this.cliCfg = cliCfg;

        return this;
    }

    /** Gets configured cache configurations. */
    public Collection<ClientCacheConfiguration> getCacheConfigurations() {
        return ccfgs == null ? Collections.emptyList() : ccfgs.values();
    }

    /**
     * Sets the Ignite cache configurations that will be used to start the Ignite cache with the name requested by
     * the Spring Cache Framework if one does not exist. If configuration with the requested name
     * is not found among the configurations specified by this method, the default configuration with the requested
     * name will be used.
     */
    public IgniteClientSpringCacheManager setCacheConfigurations(ClientCacheConfiguration... cfgs) {
        ccfgs = new HashMap<>();

        for (ClientCacheConfiguration cfg : cfgs) {
            String name = cfg.getName();

            if (name == null)
                throw new IllegalArgumentException("Cache name must not be null.");

            ClientCacheConfiguration prev = ccfgs.putIfAbsent(name, cfg);

            if (prev != null) {
                throw new IllegalArgumentException("Multiple cache configurations with the same name are specified " +
                    "[name=" + name + "]. Each cache configuration must have a unique name.");
            }
        }

        return this;
    }

    /** {@inheritDoc} */
    @Override protected AbstractSpringCache createCache(String name) {
        ClientCacheConfiguration ccfg = ccfgs == null ? null : ccfgs.get(name);

        return new IgniteClientSpringCache(cli.getOrCreateCache(ccfg == null
            ? new ClientCacheConfiguration().setName(name)
            : ccfg));
    }

    /** {@inheritDoc} */
    @Override public void afterSingletonsInstantiated() {
        if (cli != null)
            return;

        if (cliCfg == null) {
            throw new IllegalArgumentException("Neither client instance nor client configuration is specified." +
                " Set the 'igniteClientInstance' property if you already have an Ignite client instance running," +
                " or set the 'clientConfiguration' property if the Ignite client instance need to be started" +
                " implicitly by the manager.");
        }

        cli = Ignition.startClient(cliCfg);
    }

    /** {@inheritDoc} */
    @Override public void destroy() throws Exception {
        if (!externalCliInstance)
            cli.close();
    }
}
