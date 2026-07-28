[![CircleCI](https://dl.circleci.com/status-badge/img/gh/LimmychAbbil/ProjectSel/tree/master.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/LimmychAbbil/ProjectSel/tree/master)

### Namely tests

This project is used to execute tests for https://namely.com.ua either for web or local versions.

#### Prerequisites

* Java (JDK) version 17 or higher
* Maven 3.8.8 or higher
* JAVA_HOME env variable set

#### How to start

- In project root execute `mvn clean test` to run all tests on web version or `mvn clean test -P local` to use the local version.
- If you want to overwrite the default URL, you can use parameter `-Dsite.main.url=<your_url>` to specify a different URL.
- You may also change the `<site.main.url>` property value in the pom.xml file for your local tests. 
- Add profile flag `-P sel` to execute selenium browser tests. Can be combined with `-P local` to execute tests on the local version.
- When using `-P sel` and you want to run browser in visibility mode, add `-Dbrowser.visible=true` to your command. By default, browser runs in a headless mode.
- You also may specify the amount of random name pages to be checked for site map tests using `-Dtesting.names.number=<number>` parameter. By default, it is 15.


in case of errors update a version
            <artifactId>selenium-java</artifactId>
            <version>4.35.0</version>