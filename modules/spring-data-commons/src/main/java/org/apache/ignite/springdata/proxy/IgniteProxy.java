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

package org.apache.ignite.springdata.proxy;

/** Represents Ignite cluster operations required by Spring Data. */
public interface IgniteProxy extends AutoCloseable {
    /**
     * Gets existing cache with the given name or creates new one.
     *
     * @param name Cache name.
     * @return Cache proxy that provides access to cache with given name.
     */
    public <K, V> IgniteCacheProxy<K, V> getOrCreateCache(String name);

    /**
     * Gets cache with the given name.
     *
     * @param name Cache name.
     * @return Cache proxy that provides access to cache with specified name or {@code null} if it doesn't exist.
     */
    public <K, V> IgniteCacheProxy<K, V> cache(String name);
}
