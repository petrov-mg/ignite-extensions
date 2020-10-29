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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import javax.cache.Cache;
import javax.cache.expiry.ExpiryPolicy;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.springdata20.repository.IgniteRepository;
import org.jetbrains.annotations.Nullable;

import static java.util.Collections.emptySet;
import static org.apache.ignite.springdata20.repository.support.IgniteRepositoryImpl.toValueIterable;

/** Represents implementation of {@link IgniteRepository} in which Ignite data is acessed through {@link ClientCache}. */
public class IgniteClientRepository<V, K extends Serializable> implements IgniteRepository<V, K> {
    /** Error message indicating that operation is unsupported when thin client is used for accesing the Ignite cluster. */
    private static final String UNSUPPORTED_ERR_MSG = "Operation is unsupported when a thin client is used for " +
        "accessing the Ignite cluester. Use an Ingite node instance instead.";

    /** {@link ClientCache} bound to the repository. */
    private final ClientCache<K, V> cache;

    /** */
    public IgniteClientRepository(ClientCache<K, V> cache) {
        this.cache = cache;
    }

    /** {@inheritDoc} */
    @Override public IgniteCache<K, V> cache() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public Ignite ignite() {
        throw new UnsupportedOperationException(UNSUPPORTED_ERR_MSG);
    }

    /** {@inheritDoc} */
    @Override public <S extends V> S save(K key, S entity) {
        cache.put(key, entity);

        return entity;
    }

    /** {@inheritDoc} */
    @Override public <S extends V> Iterable<S> save(Map<K, S> entities) {
        cache.putAll(entities);

        return entities.values();
    }

    /** {@inheritDoc} */
    @Override public <S extends V> S save(K key, S entity, @Nullable ExpiryPolicy expiryPlc) {
        if (expiryPlc != null)
            cache.withExpirePolicy(expiryPlc).put(key, entity);
        else
            cache.put(key, entity);

        return entity;
    }

    /** {@inheritDoc} */
    @Override public <S extends V> Iterable<S> save(Map<K, S> entities, @Nullable ExpiryPolicy expiryPlc) {
        if (expiryPlc != null)
            cache.withExpirePolicy(expiryPlc).putAll(entities);
        else
            cache.putAll(entities);

        return entities.values();
    }

    /** {@inheritDoc} */
    @Override public <S extends V> S save(S entity) {
        throw new UnsupportedOperationException("Use IgniteRepository.save(key,value) method instead.");
    }

    /** {@inheritDoc} */
    @Override public <S extends V> Iterable<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException("Use IgniteRepository.save(Map<keys,value>) method instead.");
    }

    /** {@inheritDoc} */
    @Override public Optional<V> findById(K id) {
        return Optional.ofNullable(cache.get(id));
    }

    /** {@inheritDoc} */
    @Override public boolean existsById(K id) {
        return cache.containsKey(id);
    }

    /** {@inheritDoc} */
    @Override public Iterable<V> findAll() {
        return toValueIterable(cache.<Cache.Entry<K, V>>query(new ScanQuery<>()).getAll().iterator());
    }

    /** {@inheritDoc} */
    @Override public Iterable<V> findAllById(Iterable<K> ids) {
        return cache.getAll(toSet(ids)).values();
    }

    /** {@inheritDoc} */
    @Override public long count() {
        return cache.size(CachePeekMode.PRIMARY);
    }

    /** {@inheritDoc} */
    @Override public void deleteById(K id) {
        cache.remove(id);
    }

    /** {@inheritDoc} */
    @Override public void delete(V entity) {
        throw new UnsupportedOperationException("Use IgniteRepository.deleteById(key) method instead.");
    }

    /** {@inheritDoc} */
    @Override public void deleteAll(Iterable<? extends V> entities) {
        throw new UnsupportedOperationException("Use IgniteRepository.deleteAllById(keys) method instead.");
    }

    /** {@inheritDoc} */
    @Override public void deleteAllById(Iterable<K> ids) {
        cache.removeAll(toSet(ids));
    }

    /** {@inheritDoc} */
    @Override public void deleteAll() {
        cache.clear();
    }

    /**
     * @param ids Collection of IDs.
     * @return Collection transformed to set.
     */
    private Set<K> toSet(Iterable<K> ids) {
        if (ids instanceof Set)
            return (Set<K>)ids;

        Iterator<K> itr = ids.iterator();

        if (!itr.hasNext())
            return emptySet();

        K key = itr.next();

        Set<K> keys = key instanceof Comparable ? new TreeSet<>() : new HashSet<>();

        keys.add(key);

        while (itr.hasNext()) {
            key = itr.next();

            keys.add(key);
        }

        return keys;
    }
}
