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
import java.util.HashMap;
import java.util.Map;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCacheConfiguration;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/** */
public class IgniteClientSpringCacheManager extends AbstractCacheManager implements InitializingBean, DisposableBean {
    /** */
    private final Map<String, ClientCacheConfiguration> ccfgs = new HashMap<>();

    /** Ignite instance. */
    private IgniteClient cli;

    /** */
    private ClientConfiguration cliCfg;

    /** */
    private boolean externalCliInstance;

    /** */
    public IgniteClient getClientInstance() {
        return cli;
    }

    /** */
    public void setClientInstance(IgniteClient cli) {
        this.cli = cli;
    }

    /** */
    public ClientConfiguration getClientConfiguration() {
        return cliCfg;
    }

    /** */
    public void setClientConfiguration(ClientConfiguration cliCfg) {
        this.cliCfg = cliCfg;
    }

    /** */
    public Collection<ClientCacheConfiguration> getCacheConfigurations() {
        return ccfgs.values();
    }

    /** */
    public void setCacheConfigurations(ClientCacheConfiguration... cfgs) {
        ccfgs.clear();

        for (ClientCacheConfiguration cfg : cfgs) {
            String name = cfg.getName();

            if (name == null)
                throw new IllegalArgumentException();

            ClientCacheConfiguration prev = ccfgs.putIfAbsent(cfg.getName(), cfg);

            if (prev != null)
                throw new IllegalArgumentException();
        }
    }

    /** {@inheritDoc} */
    @Override protected AbstractSpringCache getOrCreateCache(String name) {
        ClientCacheConfiguration ccfg = ccfgs.get(name);

        return new IgniteClientSpringCache(cli.getOrCreateCache(ccfg == null
            ? new ClientCacheConfiguration().setName(name)
            : ccfg));
    }

    /** {@inheritDoc} */
    @Override public void afterPropertiesSet() {
        if (cli != null) {
            externalCliInstance = true;

            return;
        }

        if (cliCfg == null)
            throw new IllegalArgumentException();

        cli = Ignition.startClient(cliCfg);
    }

    /** {@inheritDoc} */
    @Override public void destroy() throws Exception {
        if (!externalCliInstance)
            cli.close();
    }
}
