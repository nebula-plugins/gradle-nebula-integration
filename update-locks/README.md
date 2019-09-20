### Core locking issue with update-locks

The repo shows a case where a transitive dependency is not being updated when an direct dependency is updated via `./gradlew dependencies --update-locks group:artifact`

Specifically, this showcases that the transitive guava is not updated in the `--update-locks`

Please run the tests to see the behavior, then look at the results in `build/nebulatest/<file>/<testname>`

#### Original dependency version: [7.3.0](https://bintray.com/nebula/gradle-plugins/download_file?file_path=com%2Fnetflix%2Fnebula%2Fnebula-bintray-plugin%2F7.3.0%2Fnebula-bintray-plugin-7.3.0.pom)

- This brings in guava via `build-info-extractor-gradle` 

####    Updated dependency version: [7.4.1](https://bintray.com/nebula/gradle-plugins/download_file?file_path=com%2Fnetflix%2Fnebula%2Fnebula-bintray-plugin%2F7.4.1%2Fnebula-bintray-plugin-7.4.1.pom)

- This excludes the version of guava brought in from `build-info-extractor-gradle` and adds a direct dependency to a different version of guava

```
<dependency>
  <groupId>org.jfrog.buildinfo</groupId>
  <artifactId>build-info-extractor-gradle</artifactId>
  <version>4.9.9</version>
  <scope>runtime</scope>
  <exclusions>
    <exclusion>
      <artifactId>groovy-all</artifactId>
      <groupId>*</groupId>
    </exclusion>
    <exclusion>
      <artifactId>guava</artifactId>
      <groupId>com.google.guava</groupId>
    </exclusion>
  </exclusions>
</dependency>
<dependency>
  <groupId>com.google.guava</groupId>
  <artifactId>guava</artifactId>
  <version>23.1-jre</version>
  <scope>runtime</scope>
</dependency>
```