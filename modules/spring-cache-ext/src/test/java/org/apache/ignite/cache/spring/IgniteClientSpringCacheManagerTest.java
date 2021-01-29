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

import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCacheConfiguration;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.BinaryConfiguration;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.ClientConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.apache.ignite.configuration.ClientConnectorConfiguration.DFLT_PORT;

/** */
public class IgniteClientSpringCacheManagerTest extends GridSpringCacheManagerAbstractTest {
    /** */
    private AnnotationConfigApplicationContext ctx;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(igniteInstanceName);

        cfg.setCacheConfiguration(new CacheConfiguration<>(CACHE_NAME));
        cfg.setBinaryConfiguration(new BinaryConfiguration().setCompactFooter(false));

        return cfg;
    }

    /** {@inheritDoc} */
    @Override protected void beforeTestsStarted() throws Exception {
        super.beforeTestsStarted();

        startGrid();
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        super.beforeTest();

        ctx = new AnnotationConfigApplicationContext(IgniteClientSpringCacheApplicationContext.class);

        svc = ctx.getBean(GridSpringCacheTestService.class);
        dynamicSvc = ctx.getBean(GridSpringDynamicCacheTestService.class);

        svc.reset();
        dynamicSvc.reset();
    }


    /** {@inheritDoc} */
    @Override protected void afterTest() {
        grid().cache(CACHE_NAME).removeAll();

        grid().destroyCache(DYNAMIC_CACHE_NAME);

        ctx.stop();
    }

    /** */
    @Configuration
    @EnableCaching
    public static class IgniteClientSpringCacheApplicationContext extends CachingConfigurerSupport {
        /** */
        @Bean
        public GridSpringCacheTestService cacheService() {
            return new GridSpringCacheTestService();
        }

        /** */
        @Bean
        public GridSpringDynamicCacheTestService dynamicCacheService() {
            return new GridSpringDynamicCacheTestService();
        }

        /** */
        @Bean
        public IgniteClient igniteClient() {
            return Ignition.startClient(new ClientConfiguration()
                .setAddresses("127.0.0.1:" + DFLT_PORT)
                .setBinaryConfiguration(new BinaryConfiguration().setCompactFooter(false)));
        }

        /** */
        @Bean
        public AbstractCacheManager cacheManager(IgniteClient cli) {
            IgniteClientSpringCacheManager mgr = new IgniteClientSpringCacheManager();

            mgr.setClientInstance(cli);
            mgr.setCacheConfigurations(new ClientCacheConfiguration()
                .setName(DYNAMIC_CACHE_NAME)
                .setBackups(2));

            return mgr;
        }

        /** {@inheritDoc} */
        @Override public KeyGenerator keyGenerator() {
            return new GridSpringCacheTestKeyGenerator();
        }
    }
}
