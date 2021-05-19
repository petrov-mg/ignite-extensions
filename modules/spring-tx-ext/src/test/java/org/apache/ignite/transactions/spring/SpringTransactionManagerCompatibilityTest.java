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

package org.apache.ignite.transactions.spring;

import java.util.Collection;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.compatibility.testframework.junits.Dependency;
import org.apache.ignite.compatibility.testframework.junits.IgniteCompatibilityAbstractTest;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.util.GridJavaProcess;
import org.apache.ignite.springdata.proxy.IgniteClientCacheProxy;
import org.apache.ignite.testframework.GridTestUtils;
import org.apache.ignite.transactions.spring.IgniteClientSpringTransactionManagerTest.IgniteClientSpringTransactionManagerApplicationContext;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.apache.ignite.cache.CacheAtomicityMode.TRANSACTIONAL;

/** */
public class SpringTransactionManagerCompatibilityTest extends IgniteCompatibilityAbstractTest {
    /** {@inheritDoc} */
    @Override protected @NotNull Collection<Dependency> getDependencies(String igniteVer) {
        Collection<Dependency> res = super.getDependencies(igniteVer);

        res.add(new Dependency("spring-tx-ext", "ignite-spring-tx-ext", "1.0.0-SNAPSHOT", false));
//        res.add(new Dependency("spring-tx", "org.springframework", "spring-tx", "5.1.0.RELEASE", false));
        res.add(new Dependency("spring", "ignite-spring", false));
        res.add(new Dependency("spring-tx", "org.springframework", "spring-tx", "4.3.26.RELEASE", false));

        return res;
    }

    /** */
    protected void processNodeConfiguration(IgniteConfiguration cfg) {
        cfg.setCacheConfiguration(new CacheConfiguration<>("test-cache")
            .setAtomicityMode(TRANSACTIONAL));
    }


    /** */
    @Test
    public void test() throws Exception {
        startGrid(1, "2.8.0", this::processNodeConfiguration, ignite -> {});

        GridJavaProcess proc = GridJavaProcess.exec(
            TestRunner.class.getName(),
            "",
            log,
            log::info,
            null,
            null,
            getProcessProxyJvmArgs("2.8.0"),
            null
        );

        try {
            GridTestUtils.waitForCondition(() -> !proc.getProcess().isAlive(), 5_000L);

            assertEquals(0, proc.getProcess().exitValue());
        }
        finally {
            if (proc.getProcess().isAlive())
                proc.kill();
        }
    }

    /** */
    public static class TestRunner {
        /** */
        public static void main(String[] args) throws Exception {
            AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();

            ctx.register(IgniteClientSpringTransactionManagerApplicationContext.class);
            ctx.refresh();

            GridSpringTransactionService svc = ctx.getBean(GridSpringTransactionService.class);

            IgniteClient cli = ctx.getBean(IgniteClient.class);

            ClientCache<Integer, String> cache = cli.cache("test-cache");

            svc.put(new IgniteClientCacheProxy<>(cache), 1);

            assertEquals(1, cache.size());
        }
    }
}
