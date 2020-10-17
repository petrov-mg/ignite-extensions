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

package org.apache.ignite.springdata20.misc;

import org.apache.ignite.Ignition;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.ClientConfiguration;
import org.apache.ignite.configuration.ClientConnectorConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.springdata20.repository.config.EnableIgniteRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.query.spi.EvaluationContextExtension;

import static org.apache.ignite.springdata20.misc.ApplicationConfiguration.IGNITE_INSTANCE_ONE;
import static org.apache.ignite.springdata20.misc.ApplicationConfiguration.IGNITE_INSTANCE_TWO;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

/** */
@Configuration
@EnableIgniteRepositories(excludeFilters = @Filter(type = ASSIGNABLE_TYPE, classes = {IgnitePersonRepository.class}))
public class IgniteClientApplicationConfiguration {
    /** */
    public static final String CACHE_NAME = "PersonCache";

    /** The bean with cache names */
    @Bean
    public CacheNamesBean cacheNames() {
        CacheNamesBean bean = new CacheNamesBean();

        bean.setPersonCacheName("PersonCache");

        return bean;
    }

    /** */
    @Bean
    public EvaluationContextExtension sampleSpELExtension() {
        return new SampleEvaluationContextExtension();
    }

    /** */
    @Bean(value = "sampleExtensionBean")
    public SampleEvaluationContextExtension.SamplePassParamExtension sampleExtensionBean() {
        return new SampleEvaluationContextExtension.SamplePassParamExtension();
    }

    /** Ignite instance bean with default name. */
    @Bean
    public IgniteClient igniteInstance() {
        Ignition.start(igniteConfiguration(IGNITE_INSTANCE_ONE, 10800));

        return Ignition.startClient(new ClientConfiguration().setAddresses("127.0.0.1:10800"));
    }

    /** Ignite instance bean with not default name. */
    @Bean
    public IgniteClient igniteInstanceTWO() {
        Ignition.start(igniteConfiguration(IGNITE_INSTANCE_TWO, 10801));

        return Ignition.startClient(new ClientConfiguration().setAddresses("127.0.0.1:10801"));
    }

    /** */
    private static IgniteConfiguration igniteConfiguration(String igniteInstanceName, int cliConnPort) {
        return new IgniteConfiguration()
            .setIgniteInstanceName(igniteInstanceName)
            .setClientConnectorConfiguration(new ClientConnectorConfiguration().setPort(cliConnPort))
            .setCacheConfiguration(new CacheConfiguration<>(CACHE_NAME).setIndexedTypes(Integer.class, Person.class))
            .setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(new TcpDiscoveryVmIpFinder(true)));
    }
}
