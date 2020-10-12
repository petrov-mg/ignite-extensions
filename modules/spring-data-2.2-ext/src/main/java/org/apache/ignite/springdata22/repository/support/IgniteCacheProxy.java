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

package org.apache.ignite.springdata22.repository.support;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.cache.Cache;
import javax.cache.expiry.ExpiryPolicy;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.Query;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.client.ClientCache;

/** */
public interface IgniteCacheProxy<K, V> extends Iterable<Cache.Entry<K, V>>{
    /**
     * Gets an entry from the cache.
     *
     * @param key the key whose associated value is to be returned
     * @return the element, or null, if it does not exist.
     * @throws NullPointerException if the key is null.
     */
    public V get(K key);

    /**
     * Associates the specified value with the specified key in the cache.
     * <p>
     * If the {@link ClientCache} previously contained a mapping for the key, the old
     * value is replaced by the specified value.
     *
     * @param key key with which the specified value is to be associated
     * @param val value to be associated with the specified key.
     * @throws NullPointerException if key is null or if value is null.
     */
    public void put(K key, V val);

    /**
     * Gets the number of all entries cached across all nodes. By default, if {@code peekModes} value isn't provided,
     * only size of primary copies across all nodes will be returned. This behavior is identical to calling
     * this method with {@link CachePeekMode#PRIMARY} peek mode.
     * <p>
     * NOTE: this operation is distributed and will query all participating nodes for their cache sizes.
     *
     * @param peekModes Optional peek modes. If not provided, then total cache size is returned.
     */
    public int size(CachePeekMode... peekModes);

    /**
     * Gets a collection of entries from the {@link ClientCache}, returning them as
     * {@link Map} of the values associated with the set of keys requested.
     *
     * @param keys The keys whose associated values are to be returned.
     * @return A map of entries that were found for the given keys. Keys not found
     * in the cache are not in the returned map.
     */
    public Map<K, V> getAll(Set<? extends K> keys);

    /**
     * Copies all of the entries from the specified map to the {@link ClientCache}.
     * <p>
     * The effect of this call is equivalent to that of calling
     * {@link #put(Object, Object) put(k, v)} on this cache once for each mapping
     * from key <tt>k</tt> to value <tt>v</tt> in the specified map.
     * <p>
     * The order in which the individual puts occur is undefined.
     * <p>
     * The behavior of this operation is undefined if entries in the cache
     * corresponding to entries in the map are modified or removed while this
     * operation is in progress, or if map is modified while the operation is in progress.
     * <p>
     *
     * @param map Mappings to be stored in this cache.
     */
    public void putAll(Map<? extends K, ? extends V> map);

    /**
     * Removes the mapping for a key from this cache if it is present.
     * <p>
     * More formally, if this cache contains a mapping from key <tt>k</tt> to value <tt>v</tt> such that
     * <code>(key==null ?  k==null : key.equals(k))</code>, that mapping is removed.
     * (The cache can contain at most one such mapping.)
     *
     * <p>Returns <tt>true</tt> if this cache previously associated the key, or <tt>false</tt> if the cache
     * contained no mapping for the key.
     * <p>
     * The cache will not contain a mapping for the specified key once the
     * call returns.
     *
     * @param key Key whose mapping is to be removed from the cache.
     * @return <tt>false</tt> if there was no matching key.
     */
    public boolean remove(K key);

    /**
     * Removes entries for the specified keys.
     * <p>
     * The order in which the individual entries are removed is undefined.
     *
     * @param keys The keys to remove.
     */
    public void removeAll(Set<? extends K> keys);

    /** Clears the contents of the cache. This method does not notify event listeners and cache writers. */
    public void clear();

    /**
     * Returns cache with the specified expired policy set. This policy will be used for each operation invoked on
     * the returned cache.
     *
     * @return Cache instance with the specified expiry policy set.
     */
    public IgniteCacheProxy<K, V> withExpirePolicy(ExpiryPolicy expirePlc);

    /**
     * Queries cache. Supports {@link ScanQuery} and {@link SqlFieldsQuery}.
     *
     * @param qry Query.
     * @return Cursor.
     *
     */
    public <R> QueryCursor<R> query(Query<R> qry);

    /**
     * Convenience method to execute {@link SqlFieldsQuery}.
     *
     * @param qry Query.
     * @return Cursor.
     */
    public FieldsQueryCursor<List<?>> query(SqlFieldsQuery qry);

    /** */
    public boolean containsKey(K key);
}
