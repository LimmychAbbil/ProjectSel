### Namely tests

This project is used to execute tests for https://namely.com.ua either for web or local versions.

#### Prerequisites

* Java (JDK) version 17 or higher
* Maven 3.8.8 or higher
* JAVA_HOME env variable set

#### How to start

- In project root execute `mvn clean test` to run all tests on web version or `mvn clean test -P local` to use the local version.
- Add profile flag `-P sel` to execute selenium browser tests. Can be combined with `-P local` to execute tests on the local version. 
