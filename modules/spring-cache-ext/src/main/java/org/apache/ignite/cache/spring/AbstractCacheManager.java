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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/** */
public abstract class AbstractCacheManager implements CacheManager {
    /** Caches map. */
    private final ConcurrentMap<String, AbstractSpringCache> caches = new ConcurrentHashMap<>();

    /** {@inheritDoc} */
    @Override public Cache getCache(String name) {
        return caches.computeIfAbsent(name, k -> getOrCreateCache(name));
    }

    /** {@inheritDoc} */
    @Override public Collection<String> getCacheNames() {
        return new ArrayList<>(caches.keySet());
    }

    /** */
    protected abstract AbstractSpringCache getOrCreateCache(String name);
}
