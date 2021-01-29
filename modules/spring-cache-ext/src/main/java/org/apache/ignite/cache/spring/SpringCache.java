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
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteLock;
import org.apache.ignite.springdata.proxy.IgniteCacheProxyImpl;

/** Represents {@link AbstractSpringCache} implementation that uses {@link IgniteCache} to perform cache operations. */
class SpringCache extends AbstractSpringCache {
    /** */
    private final SpringCacheManager mgr;

    /**
     * @param cache Cache.
     * @param mgr Manager
     */
    SpringCache(IgniteCache<Object, Object> cache, SpringCacheManager mgr) {
        super(new IgniteCacheProxyImpl<>(cache));

        this.mgr = mgr;
    }

    /** {@inheritDoc} */
    @Override public <T> T get(final Object key, final Callable<T> valLdr) {
        Object val = cache.get(key);

        if (val == null) {
            IgniteLock lock = mgr.getSyncLock(cache.getName(), key);

            lock.lock();

            try {
                val = cache.get(key);

                if (val == null) {
                    try {
                        T retVal = valLdr.call();

                        val = wrapNull(retVal);

                        cache.put(key, val);
                    }
                    catch (Exception e) {
                        throw new ValueRetrievalException(key, valLdr, e);
                    }
                }
            }
            finally {
                lock.unlock();
            }
        }

        return (T)unwrapNull(val);
    }
}
