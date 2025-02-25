[![CircleCI](https://dl.circleci.com/status-badge/img/gh/LimmychAbbil/ProjectSel/tree/master.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/LimmychAbbil/ProjectSel/tree/master)

### Namely tests

This project is used to execute tests for https://namely.com.ua either for web or local versions.

#### Prerequisites

* Java (JDK) version 17 or higher
* Maven 3.8.8 or higher
* JAVA_HOME env variable set

#### How to start

- In project root execute `mvn clean test` to run all tests on web version or `mvn clean test -P local` to use the local version.
- Add profile flag `-P sel` to execute selenium browser tests. Can be combined with `-P local` to execute tests on the local version.
- When using `-P sel` and you want to run browser in visibility mode, add `-Dbrowser.visible=true` to your command. By default, browser runs in a headless mode.
