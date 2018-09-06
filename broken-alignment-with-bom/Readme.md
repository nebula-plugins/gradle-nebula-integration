# Broken alignment

Dependencies which are marked to belong to a same group and forced to downgrade have are not aligned in this case.

You can see this output `./gradlew dependencies --configuration compile`

```
compile - Dependencies for source set 'main' (deprecated, use 'implementation' instead).
+--- example:bom:1.0
|    +--- example:platform:2.0
|    |    \--- example:transitive1:1.0
|    |         \--- example:transitive2:1.0
|    |              \--- com.fasterxml.jackson.datatype:jackson-datatype-guava:2.2.0 -> 2.9.6
|    +--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.9.6 -> 2.8.6
|    \--- com.fasterxml.jackson.datatype:jackson-datatype-guava:2.9.6
+--- example:dependency1:1.0
|    \--- example:platform:1.0 -> 2.0 (*)
\--- example:dependency2:1.0
     +--- com.fasterxml.jackson.datatype:jackson-datatype-joda:2.9.6 -> 2.8.6
     \--- com.fasterxml.jackson.datatype:jackson-datatype-guava:2.9.6
```

I would expect all dependencies at the 2.8.6 version.