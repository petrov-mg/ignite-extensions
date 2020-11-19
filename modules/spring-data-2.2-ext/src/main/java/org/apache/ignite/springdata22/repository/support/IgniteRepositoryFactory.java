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

import java.util.Optional;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.springdata.proxy.ClosableIgniteClientProxy;
import org.apache.ignite.springdata.proxy.ClosableIgniteProxy;
import org.apache.ignite.springdata.proxy.IgniteCacheProxy;
import org.apache.ignite.springdata.proxy.IgniteClientProxy;
import org.apache.ignite.springdata.proxy.IgniteProxy;
import org.apache.ignite.springdata.proxy.IgniteProxyImpl;
import org.apache.ignite.springdata22.repository.config.DynamicQueryConfig;
import org.apache.ignite.springdata22.repository.config.Query;
import org.apache.ignite.springdata22.repository.config.RepositoryConfig;
import org.apache.ignite.springdata22.repository.query.IgniteQuery;
import org.apache.ignite.springdata22.repository.query.IgniteQueryGenerator;
import org.apache.ignite.springdata22.repository.query.IgniteRepositoryQuery;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.AbstractEntityInformation;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Crucial for spring-data functionality class. Create proxies for repositories.
 * <p>
 * Supports multiple Ignite Instances on same JVM.
 * <p>
 * This is pretty useful working with Spring repositories bound to different Ignite intances within same application.
 *
 * @author Apache Ignite Team
 * @author Manuel Núñez (manuel.nunez@hawkore.com)
 */
public class IgniteRepositoryFactory extends RepositoryFactorySupport {
    /** Spring application context */
    private final ApplicationContext ctx;

    /** Spring application expression resolver */
    private final BeanExpressionResolver resolver = new StandardBeanExpressionResolver();

    /** */
    private BeanFactory beanFactory;

    /** Spring application bean expression context */
    private BeanExpressionContext beanExpressionContext;

    /** */
    private IgniteCacheProxy<?, ?> cache;

    /** */
    private IgniteProxy ignite;

    /** {@inheritDoc} */
    @Override public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        super.setBeanFactory(beanFactory);

        this.beanFactory = beanFactory;
    }

    /**
     * Creates the factory with initialized {@link Ignite} instance.
     *
     * @param ctx the ctx
     */
    public IgniteRepositoryFactory(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    /** */
    public void afterPropertiesSet(Class<?> repoInterface) {
        beanExpressionContext = new BeanExpressionContext(new DefaultListableBeanFactory(beanFactory), null);

        RepositoryConfig repoCfg = repoInterface.getAnnotation(RepositoryConfig.class);

        Assert.notNull(repoCfg, "");

        String cacheNameSpelExpression = repoCfg.cacheName();

        Assert.hasText(cacheNameSpelExpression, "Set a name of an Apache Ignite cache using @RepositoryConfig" +
            " annotation to map this repository to the underlying cache.");

        String cacheName = evaluateExpression(cacheNameSpelExpression);

        ignite = igniteForRepoConfig(repoCfg);

        cache = repoCfg.autoCreateCache() ? ignite.getOrCreateCache(cacheName) : ignite.cache(cacheName);

        if (cache == null) {
            throw new IllegalStateException(
                "Cache '" + cacheName + "' not found for repository interface " + repoInterface.getName()
                    + ". Please, add a cache configuration to ignite configuration"
                    + " or pass autoCreateCache=true to org.apache.ignite.springdata22"
                    + ".repository.config.RepositoryConfig annotation.");
        }
    }

    /** */
    private IgniteProxy igniteForRepoConfig(RepositoryConfig cfg) {
        try {
            Object igniteInstanceBean = ctx.getBean(evaluateExpression(cfg.igniteInstance()));

            if (igniteInstanceBean instanceof Ignite)
                return new IgniteProxyImpl((Ignite)igniteInstanceBean);
            else if (igniteInstanceBean instanceof IgniteClient)
                return new IgniteClientProxy((IgniteClient)igniteInstanceBean);

            throw new IllegalStateException("Invalid repository configuration. The Spring Bean corresponding to the" +
                " \"igniteInstance\" property of repository configuration must be one of the following types: " +
                Ignite.class.getName() + ", " + IgniteClient.class.getName());
        }
        catch (BeansException ex) {
            try {
                Object igniteCfgBean = ctx.getBean(evaluateExpression(cfg.igniteCfg()));

                if (igniteCfgBean instanceof IgniteConfiguration) {
                    try {
                        // first try to attach to existing ignite instance
                        return new IgniteProxyImpl(Ignition.ignite(((IgniteConfiguration)igniteCfgBean).getIgniteInstanceName()));
                    }
                    catch (Exception ignored) {
                        // nop
                    }
                    return new ClosableIgniteProxy(Ignition.start((IgniteConfiguration)igniteCfgBean));
                }
                else if (igniteCfgBean instanceof ClientConfiguration)
                    return new ClosableIgniteClientProxy(Ignition.startClient((ClientConfiguration)igniteCfgBean));

                throw new IllegalStateException("Invalid repository configuration. The Spring Bean corresponding to" +
                    " the \"igniteCfg\" property of repository configuration must be one of the following types: [" +
                    IgniteConfiguration.class.getName() + ", " + ClientConfiguration.class.getName() + ']');

            }
            catch (BeansException ex2) {
                try {
                    String igniteSpringCfgPath = evaluateExpression(cfg.igniteSpringCfgPath());
                    String path = (String)ctx.getBean(igniteSpringCfgPath);
                    return new ClosableIgniteProxy(Ignition.start(path));
                }
                catch (BeansException ex3) {
                    throw new IgniteException("Failed to initialize Ignite repository factory. No beans required for" +
                        " repository configuration were found. Check \"igniteInstance\", \"igniteCfg\"," +
                        " \"igniteSpringCfgPath\" parameters of " + RepositoryConfig.class.getName() + "class.");
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        return new AbstractEntityInformation<T, ID>(domainClass) {
            /** {@inheritDoc} */
            @Override public ID getId(T entity) {
                return null;
            }

            /** {@inheritDoc} */
            @Override public Class<ID> getIdType() {
                return null;
            }
        };
    }

    /** {@inheritDoc} */
    @Override protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return IgniteRepositoryImpl.class;
    }

    /**
     * Evaluate the SpEL expression
     *
     * @param spelExpression SpEL expression
     * @return the result of execution of the SpEL expression
     */
    private String evaluateExpression(String spelExpression) {
        return (String)resolver.evaluate(spelExpression, beanExpressionContext);
    }

    /** {@inheritDoc} */
    @Override protected Object getTargetRepository(RepositoryInformation metadata) {
        return getTargetRepositoryViaReflection(metadata, ignite, cache);
    }

    /** {@inheritDoc} */
    @Override protected Optional<QueryLookupStrategy> getQueryLookupStrategy(final QueryLookupStrategy.Key key,
        QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return Optional.of((mtd, metadata, factory, namedQueries) -> {
            final Query annotation = mtd.getAnnotation(Query.class);
            if (annotation != null && (StringUtils.hasText(annotation.value()) || annotation.textQuery() || annotation
                .dynamicQuery())) {

                String qryStr = annotation.value();

                boolean annotatedIgniteQuery = !annotation.dynamicQuery() && (StringUtils.hasText(qryStr) || annotation
                    .textQuery());

                IgniteQuery query = annotatedIgniteQuery ? new IgniteQuery(qryStr,
                    !annotation.textQuery() && (isFieldQuery(qryStr) || annotation.forceFieldsQuery()),
                    annotation.textQuery(), false, IgniteQueryGenerator.getOptions(mtd)) : null;

                if (key != QueryLookupStrategy.Key.CREATE) {
                    return new IgniteRepositoryQuery(metadata, query, mtd, factory, cache,
                        annotatedIgniteQuery ? DynamicQueryConfig.fromQueryAnnotation(annotation) : null,
                        evaluationContextProvider);
                }
            }

            if (key == QueryLookupStrategy.Key.USE_DECLARED_QUERY) {
                throw new IllegalStateException("To use QueryLookupStrategy.Key.USE_DECLARED_QUERY, pass "
                    + "a query string via org.apache.ignite.springdata22.repository"
                    + ".config.Query annotation.");
            }

            return new IgniteRepositoryQuery(metadata, IgniteQueryGenerator.generateSql(mtd, metadata), mtd,
                factory, cache, DynamicQueryConfig.fromQueryAnnotation(annotation), evaluationContextProvider);
        });
    }

    /**
     * @param qry Query string.
     * @return {@code true} if query is SqlFieldsQuery.
     */
    public static boolean isFieldQuery(String qry) {
        String qryUpperCase = qry.toUpperCase();

        return isStatement(qryUpperCase) && !qryUpperCase.matches("^SELECT\\s+(?:\\w+\\.)?+\\*.*");
    }

    /**
     * Evaluates if the query starts with a clause.<br>
     * <code>SELECT, INSERT, UPDATE, MERGE, DELETE</code>
     *
     * @param qryUpperCase Query string in upper case.
     * @return {@code true} if query is full SQL statement.
     */
    private static boolean isStatement(String qryUpperCase) {
        return qryUpperCase.matches("^\\s*SELECT\\b.*") ||
            // update
            qryUpperCase.matches("^\\s*UPDATE\\b.*") ||
            // delete
            qryUpperCase.matches("^\\s*DELETE\\b.*") ||
            // merge
            qryUpperCase.matches("^\\s*MERGE\\b.*") ||
            // insert
            qryUpperCase.matches("^\\s*INSERT\\b.*");
    }

    /**
     * Closes the factory and releases the Ignite resources.
     * Note, that automatically generated caches will not be closed.
     */
    public void destroy() throws Exception {
        if (ignite instanceof AutoCloseable)
            ((AutoCloseable)ignite).close();
    }
}
