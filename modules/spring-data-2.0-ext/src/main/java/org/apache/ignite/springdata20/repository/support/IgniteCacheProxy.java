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

package org.apache.ignite.springdata20.repository.support;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.cache.Cache;
import javax.cache.expiry.ExpiryPolicy;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.Query;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.jetbrains.annotations.NotNull;

/** */
public interface IgniteCacheProxy<K, V> {
    /** */
    public V get(K key);

    /** */
    public void put(K key, V val);

    /** */
    public int size(CachePeekMode... peekModes);

    /** */
    public Map<K, V> getAll(Set<? extends K> keys);

    /** */
    public void putAll(Map<? extends K, ? extends V> map);

    /** */
    public boolean remove(K key);

    /** */
    public void removeAll(Set<? extends K> keys);

    /** */
    public void clear();

    /** */
    public IgniteCacheProxy<K, V> withExpiryPolicy(ExpiryPolicy expirePlc);

    /** */
    public <R> QueryCursor<R> query(Query<R> qry) ;

    /** */
    public FieldsQueryCursor<List<?>> query(SqlFieldsQuery qry);

    /** */
    public boolean containsKey(K key);

    /** */
    public @NotNull Iterator<Cache.Entry<K, V>> iterator();
}
