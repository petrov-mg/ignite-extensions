Apache Ignite Spring Transactions Module
---------------------------

Apache Ignite Spring Transactions extension provides an integration with Spring Transactions framework.

To get started with Apache Ignite Spring Transactions, create instance of Apache Ignite Spring Transactions Manager as bean in your Spring application.

There are two implementations of Apache Ignite Spring Transactions Manager - org.apache.ignite.transactions.spring.SpringTransactionManager and org.apache.ignite.transactions.spring.IgniteClientSpringTransactionManager, that provide ability to use the Ignite thick or thin client to connect to the Ignite cluster and manage Ignite transactions, respectively.

Importing Spring Transactions extension In Maven Project
----------------------------------------

If you are using Maven to manage dependencies of your project, you can add Spring Transactions extension dependency like this (replace '${ignite-spring-transactions-ext.version}' with actual version of Ignite Spring Transactions extension you are interested in):

<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                        http://maven.apache.org/xsd/maven-4.0.0.xsd">
    ...
    <dependencies>
        ...
        <dependency>
            <groupId>org.apache.ignite</groupId>
            <artifactId>ignite-spring-transactions-ext</artifactId>
            <version>${ignite-spring-transactions.version}</version>
        </dependency>
        ...
    </dependencies>
    ...
</project>
