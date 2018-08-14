#Unexpected result of resolution when the same dependency is requested multiple times with different attributes

I have a direct dependency on `org.junit:junit-engine-api:5.+` with `release` requested status attribute. I have another direct dependency `org.junit:junit4-engine:5.+` with `integration` requested status attribute. `junit4-engine` will transitively bring `junit-engine-api`. I would assume that each leaf in dependency graph will pick a candidate version considering requested attribute. Those versions will then conflict resolve. Now my resolution fails because the winner of conflict resolution is not able to fulfill conflicting requested attributes.

Run `./gradlew dependencies` to see this result:

```
+--- org.junit:junit4-engine:5.+ -> 5.0.0-SNAPSHOT
|    +--- org.junit:junit-engine-api:5.0.0-SNAPSHOT
|    |    +--- org.junit:junit-commons:5.0.0-SNAPSHOT
|    |    \--- org.opentest4j:opentest4j:1.0.0-M1
|    \--- junit:junit:4.12
|         \--- org.hamcrest:hamcrest-core:1.3
\--- org.junit:junit-engine-api:5.+ FAILED
```