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

package org.apache.ignite.springdata22.misc;

import java.util.List;
import org.apache.ignite.Ignite;
import org.apache.ignite.springdata22.repository.config.Query;
import org.apache.ignite.springdata22.repository.config.RepositoryConfig;
import org.springframework.data.repository.query.Param;

/**
 * Repository that extends common operations with those that are only available when the {@link Ignite} node is
 * used to access the Ignite cluster.
 */
@RepositoryConfig(cacheName = "PersonCache")
public interface IgnitePersonRepository extends AbstractPersonRepository {
    /** */
    @Query(textQuery = true, value = "#{#firstname}", limit = 2)
    public List<PersonProjection> textQueryByFirstNameWithProjectionNamedParameter(@Param("firstname") String val);
}