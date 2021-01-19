Apache Ignite Spring Cache Module
---------------------------

Apache Ignite Spring Cache extension provides an integration with Spring Cache framework.

Importing Spring Cache extension In Maven Project
----------------------------------------

If you are using Maven to manage dependencies of your project, you can add Spring Cache extension dependency like this (replace '${ignite-spring-cache-ext.version}' with actual version of Ignite Spring Cache extension you are interested in):

<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                        http://maven.apache.org/xsd/maven-4.0.0.xsd">
    ...
    <dependencies>
        ...
        <dependency>
            <groupId>org.apache.ignite</groupId>
            <artifactId>ignite-spring-cache-ext</artifactId>
            <version>${ignite-spring-cache.version}</version>
        </dependency>
        ...
    </dependencies>
    ...
</project>
