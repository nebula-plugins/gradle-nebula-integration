# Issue with component metadata rules and build script classpath


#### With Gradle 4.8:

When I have a dependency in project and the same dependency in buildscript classpath component metadata rules are not invoked for that dependency

Run `gw dependencies --info` You won't see `Hello from junit:junit:4.12`

#### With Gradle 5.1-20181031000036+0000

The message is seen.
