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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * Represents abstract {@link CacheManager} implementation that hand over responsibility to create new cache instances
 * and to create synchronization objects for cache value computations to its inheritors.
 */
public abstract class AbstractCacheManager implements CacheManager {
    /** Default locks count. */
    private static final int DEFAULT_LOCKS_COUNT = 512;

    /** The number of {@link Lock}s used to synchronize the computation of cache values. */
    private int locksCnt = DEFAULT_LOCKS_COUNT;

    /** Caches mapped to their names. */
    private final Map<String, SpringCache> caches = new ConcurrentHashMap<>();

    /** Locks for the synchronous computation of cache values. Used if {@code sync} mode is enabled. */
    private final Map<Integer, Lock> locks = new ConcurrentHashMap<>();

    /** {@inheritDoc} */
    @Override public Cache getCache(String name) {
        return caches.computeIfAbsent(name, k -> createCache(name));
    }

    /** Gets {@link Lock} to synchronize value calculation for specified cache and key. */
    Lock getLock(String cache, Object key) {
        final int idx = Objects.hash(cache, key) % getLocksCount();

        return locks.computeIfAbsent(idx, i -> createLock(idx));
    }

    /** {@inheritDoc} */
    @Override public Collection<String> getCacheNames() {
        return new ArrayList<>(caches.keySet());
    }

    /** Gets maximum number of locks used to synchronize the computation of cache values. */
    public int getLocksCount() {
        return locksCnt;
    }

    /** Sets maximum number of locks used to synchronize the computation of cache values. */
    public void setLocksCount(int locksCnt) {
        this.locksCnt = locksCnt;
    }

    /** Creates {@link SpringCache} instance with specified name. */
    protected abstract SpringCache createCache(String name);

    /**
     * Creates new lock instance with specified identifier that will be used to synchronize the computation of cache
     * values.
     */
    protected abstract Lock createLock(int id);
}