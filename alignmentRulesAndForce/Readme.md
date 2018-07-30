# Difference between contraints based alignment and nebula plugin alignment

# Nebula plugin based dependencies output

`cd nebulaPlugin`
`./gradlew dependencies --configuration runtimeClasspath`

```
runtimeClasspath - Runtime classpath of source set 'main'.
+--- com.fasterxml.jackson.core:jackson-core:2.9.4 -> 2.7.9
+--- com.fasterxml.jackson.core:jackson-databind:2.7.9
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.7.0 -> 2.7.9
|    \--- com.fasterxml.jackson.core:jackson-core:2.7.9
\--- com.fasterxml.jackson.module:jackson-module-kotlin:2.9.4.1 -> 2.7.9
     +--- com.fasterxml.jackson.core:jackson-databind:2.7.9 (*)
     +--- com.fasterxml.jackson.core:jackson-annotations:2.7.9
     \--- org.jetbrains.kotlin:kotlin-reflect:1.0.2
          \--- org.jetbrains.kotlin:kotlin-stdlib:1.0.2
               \--- org.jetbrains.kotlin:kotlin-runtime:1.0.2
```

# Gradle dependency artificial platform based alignment

`cd gradleCore`
`./gradlew dependencies --configuration runtimeClasspath`

```
runtimeClasspath - Runtime classpath of source set 'main'.
+--- com.fasterxml.jackson.core:jackson-core:2.9.4
+--- com.fasterxml.jackson.core:jackson-databind:2.7.9
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.7.0 -> 2.9.4
|    \--- com.fasterxml.jackson.core:jackson-core:2.7.9 -> 2.9.4
\--- com.fasterxml.jackson.module:jackson-module-kotlin:2.9.4.1
     +--- com.fasterxml.jackson.core:jackson-databind:2.9.4 -> 2.7.9 (*)
     +--- com.fasterxml.jackson.core:jackson-annotations:2.9.0 -> 2.9.4
     \--- org.jetbrains.kotlin:kotlin-reflect:1.2.21
          \--- org.jetbrains.kotlin:kotlin-stdlib:1.2.21
               \--- org.jetbrains:annotations:13.0
```

com.fasterxml.jackson.core:jackson-databind is being forced to 2.7.9 but rest of the group is still upgrading.
