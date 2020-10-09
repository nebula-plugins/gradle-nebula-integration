# Issue with VERSION_ORDERING_V2

When using Gradle 6.6.1 or 6.7-rc-4, if we run `./gradlew dI --dependency spymemcached`, we get

```
> Task :dependencyInsight
net.spy:spymemcached:2.11.4+9
   variant "compile" [
      org.gradle.status              = release (not requested)
      org.gradle.usage               = java-api
      org.gradle.libraryelements     = jar (compatible with: classes)
      org.gradle.category            = library

      Requested attributes not found in the selected variant:
         org.gradle.dependency.bundling = external
         org.gradle.jvm.version         = 8
   ]
   Selection reasons:
      - By conflict resolution : between versions 2.11.4+9 and 2.11.4

net.spy:spymemcached:2.11.4+9
\--- netflix:foo-client:1.0.0
     \--- compileClasspath

net.spy:spymemcached:2.11.4 -> 2.11.4+9
\--- compileClasspath

A web-based, searchable dependency report is available by adding the --scan option.

```

Notice that `2.11.4+9` wins on this case

If we use 6.8 snapshot, in particular `6.8-20201008220039+0000`, executing the same command results in:

```

> Task :dependencyInsight
net.spy:spymemcached:2.11.4
   variant "compile" [
      org.gradle.status              = release (not requested)
      org.gradle.usage               = java-api
      org.gradle.libraryelements     = jar (compatible with: classes)
      org.gradle.category            = library

      Requested attributes not found in the selected variant:
         org.gradle.dependency.bundling = external
         org.gradle.jvm.version         = 8
   ]
   Selection reasons:
      - By conflict resolution : between versions 2.11.4+9 and 2.11.4

net.spy:spymemcached:2.11.4
\--- compileClasspath

net.spy:spymemcached:2.11.4+9 -> 2.11.4
\--- netflix:foo-client:1.0.0
     \--- compileClasspath

```

Where 2.11.4 wins in conflict resolution now