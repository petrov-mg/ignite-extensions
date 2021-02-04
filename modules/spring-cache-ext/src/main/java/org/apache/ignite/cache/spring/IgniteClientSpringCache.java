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

import java.util.concurrent.Callable;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.springdata.proxy.IgniteCacheClientProxy;

/** Represents {@link AbstractSpringCache} implementation that uses {@link ClientCache} to perform cache operations. */
public class IgniteClientSpringCache extends AbstractSpringCache {
    /** */
    IgniteClientSpringCache(ClientCache<Object, Object> cache) {
        super(new IgniteCacheClientProxy<>(cache));
    }

    /** {@inheritDoc} */
    @Override public <T> T get(Object key, Callable<T> valLdr) {
        throw new UnsupportedOperationException();
    }
}
