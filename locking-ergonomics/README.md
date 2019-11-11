# Locking ergonomics when using `write-locks` and `update-locks` flags

Given a project is created
And the project uses Gradle core locking
And locks are in use,
When a dependency resolves to a new version
And `--write-locks` and `--update-locks` with a group & artifact are both invoked,
Then the user should be informed that these should not be run at the same time


Example build file dependency declaration:

```
dependencies {
    implementation 'org.apache.commons:commons-lang3:3.0'
    implementation 'commons-logging:commons-logging:1.0'
}
```

upon locking dependencies via `./gradlew dependencies --write-locks`, these resolve to:

```
commons-logging:commons-logging:1.0
org.apache.commons:commons-lang3:3.0
```

When updating the versions to a dynamic selector (`latest.release`) to mimic the release of a newer version:

```
dependencies {
    implementation 'org.apache.commons:commons-lang3:latest.release'
    implementation 'commons-logging:commons-logging:latest.release'
}
```

then these dependencies resolve to the same versions with `dependencies --configuration compileClasspath`:

```
compileClasspath - Compile classpath for source set 'main'.
+--- org.apache.commons:commons-lang3:latest.release -> 3.0
+--- commons-logging:commons-logging:latest.release -> 1.0
+--- commons-logging:commons-logging:{strictly 1.0} -> 1.0 (c)
\--- org.apache.commons:commons-lang3:{strictly 3.0} -> 3.0 (c)
```
