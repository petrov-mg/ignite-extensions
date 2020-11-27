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

package org.apache.ignite.springdata20.examples;

/**
 * The example shows how to configure Ignite Spring Data integration to access an Ignite cluster through a thin client.
 * In this example, the thin client instance is started by the Spring Data automatically based on the specified thin
 * client configuration.
 */
public class IgniteClientConfigSpringDataExample extends SpringDataExample {
    /** {@inheritDoc} */
    @Override protected Class<?> springApplicationConfiguration() {
        return IgniteClientConfigApplicationConfiguration.class;
    }
}
