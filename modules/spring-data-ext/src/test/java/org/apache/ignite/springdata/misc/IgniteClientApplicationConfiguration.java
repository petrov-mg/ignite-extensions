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

package org.apache.ignite.springdata.misc;

import org.apache.ignite.Ignition;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.ClientConfiguration;
import org.apache.ignite.configuration.ClientConnectorConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.springdata.repository.config.EnableIgniteRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.apache.ignite.configuration.ClientConnectorConfiguration.DFLT_PORT;

/** */
@Configuration
@EnableIgniteRepositories()
public class IgniteClientApplicationConfiguration {
    /** @return Ignite instance. */
    @Bean
    public IgniteEx igniteServerNode() {
        IgniteConfiguration cfg = new IgniteConfiguration()
            .setCacheConfiguration(
                new CacheConfiguration<Integer, Person>("PersonCache")
                    .setIndexedTypes(Integer.class, Person.class),
                new CacheConfiguration<PersonKey, Person>("PersonWithKeyCache")
            )
            .setClientConnectorConfiguration(new ClientConnectorConfiguration())
            .setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(new TcpDiscoveryVmIpFinder(true)));

        return (IgniteEx)Ignition.start(cfg);
    }

    /** Ignite client instance bean with default name. */
    @Bean
    public IgniteClient igniteInstance() {
        return Ignition.startClient(new ClientConfiguration().setAddresses("127.0.0.1:" + DFLT_PORT));
    }
}