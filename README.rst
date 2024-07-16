About
=====

The interfaces of linear optimization packages like GLPK are not easy to use.
The Linear Optimization Wrapper for Java provides classes that wrap the solver
and provide an intiutive interface.

This release is based on GLPK for Java 1.7.0.

Maven
=====

For using this library in your Maven project enter the following repository and
dependency in your pom.xml:

.. code-block:: xml

    <repositories>
        <repository>
            <id>XypronRelease</id>
            <name>Xypron Release</name>
            <url>https://www.xypron.de/repository</url>
            <layout>default</layout>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>de.xypron.linopt</groupId>
            <artifactId>linopt</artifactId>
            <version>1.17</version>
        </dependency>
    </dependencies>

When testing with Maven it may be necessary to indicate the installation path of
the GLPK for Java shared library (.so or .dll).

.. code-block:: bash

    mvn clean install -DargLine='-Djava.library.path=/usr/local/lib/jni:/usr/lib/jni'

The exec:java target may require to indicate the installation path of the GLPK for Java shared library in MAVEN_OPTS, e.g.

.. code-block:: bash

    export MAVEN_OPTS="-Djava.library.path=/usr/local/lib/jni:/usr/lib/jni"
    mvn exec:java
