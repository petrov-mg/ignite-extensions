Apache Ignite Spring Module
---------------------------

Apache Ignite Spring Data module provides an integration with Spring Data framework.

Importing Spring Data Module In Maven Project
----------------------------------------

If you are using Maven to manage dependencies of your project, you can add Spring module
dependency like this (replace '${ignite-spring-data-ext.version}' with actual version of Ignite Spring Data integration
you are interested in):

<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                        http://maven.apache.org/xsd/maven-4.0.0.xsd">
    ...
    <dependencies>
        ...
        <dependency>
            <groupId>org.apache.ignite</groupId>
            <artifactId>ignite-spring-data</artifactId>
            <version>${ignite-spring-data-ext.version}</version>
        </dependency>
        ...
    </dependencies>
    ...
</project>
