Contributing to the Elasticsearch webdav plugin
-----------------------------------------------

**Repository:** [https://github.com/mitallast/elasticsearch-webdav-plugin](https://github.com/mitallast/elasticsearch-webdav-plugin)

Make sure you have [Maven](http://maven.apache.org) installed, as Elasticsearch uses it as its build system.
Integration with IntelliJ and Eclipse should work out of the box.

Please follow these formatting guidelines:

 * Java indent is 4 spaces
 * Line width is 140 characters
 * The rest is left to Java coding standards

To create a distribution from the source, simply run:

```sh
cd /elasticsearch-webdav-plugin
mvn clean package -DskipTests
```

You will find the newly built packages under: `./target/releases/`.

Before submitting your changes, run the test suite to make sure that nothing is broken, with:

```sh
mvn clean test
```