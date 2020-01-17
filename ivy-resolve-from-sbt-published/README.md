```
❯ ./gradlew build
> Task :compileJava FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':compileJava'.
> Could not resolve all files for configuration ':compileClasspath'.
   > Could not resolve foo:my-client:1.0.0.
     Required by:
         project :
      > Unable to find a matching configuration of foo:my-client:1.0.0:
          - Configuration 'compile':
              - Other attributes:
                  - Required org.gradle.dependency.bundling 'external' but no value provided.
                  - Required org.gradle.jvm.version '8' but no value provided.
                  - Required org.gradle.libraryelements 'classes' but no value provided.
                  - Found org.gradle.status 'integration' but wasn't required.
                  - Required org.gradle.usage 'java-api' but no value provided.
          - Configuration 'compile-internal':
              - Other attributes:
                  - Required org.gradle.dependency.bundling 'external' but no value provided.
                  - Required org.gradle.jvm.version '8' but no value provided.
                  - Required org.gradle.libraryelements 'classes' but no value provided.
                  - Found org.gradle.status 'integration' but wasn't required.
                  - Required org.gradle.usage 'java-api' but no value provided.
          - Configuration 'docs':
              - Other attributes:
                  - Required org.gradle.dependency.bundling 'external' but no value provided.
                  - Required org.gradle.jvm.version '8' but no value provided.
                  - Required org.gradle.libraryelements 'classes' but no value provided.
                  - Found org.gradle.status 'integration' but wasn't required.
                  - Required org.gradle.usage 'java-api' but no value provided.
          - Configuration 'optional':
              - Other attributes:
                  - Required org.gradle.dependency.bundling 'external' but no value provided.
                  - Required org.gradle.jvm.version '8' but no value provided.
                  - Required org.gradle.libraryelements 'classes' but no value provided.
                  - Found org.gradle.status 'integration' but wasn't required.
                  - Required org.gradle.usage 'java-api' but no value provided.
          - Configuration 'plugin':
              - Other attributes:
                  - Required org.gradle.dependency.bundling 'external' but no value provided.
                  - Required org.gradle.jvm.version '8' but no value provided.
                  - Required org.gradle.libraryelements 'classes' but no value provided.
                  - Found org.gradle.status 'integration' but wasn't required.
                  - Required org.gradle.usage 'java-api' but no value provided.
          - Configuration 'pom':
              - Other attributes:
                  - Required org.gradle.dependency.bundling 'external' but no value provided.
                  - Required org.gradle.jvm.version '8' but no value provided.
                  - Required org.gradle.libraryelements 'classes' but no value provided.
                  - Found org.gradle.status 'integration' but wasn't required.
                  - Required org.gradle.usage 'java-api' but no value provided.
          - Configuration 'provided':
              - Other attributes:
                  - Required org.gradle.dependency.bundling 'external' but no value provided.
                  - Required org.gradle.jvm.version '8' but no value provided.
                  - Required org.gradle.libraryelements 'classes' but no value provided.
                  - Found org.gradle.status 'integration' but wasn't required.
                  - Required org.gradle.usage 'java-api' but no value provided.
          - Configuration 'runtime':
              - Other attributes:
                  - Required org.gradle.dependency.bundling 'external' but no value provided.
                  - Required org.gradle.jvm.version '8' but no value provided.
                  - Required org.gradle.libraryelements 'classes' but no value provided.
                  - Found org.gradle.status 'integration' but wasn't required.
                  - Required org.gradle.usage 'java-api' but no value provided.
          - Configuration 'runtime-internal':
              - Other attributes:
                  - Required org.gradle.dependency.bundling 'external' but no value provided.
                  - Required org.gradle.jvm.version '8' but no value provided.
                  - Required org.gradle.libraryelements 'classes' but no value provided.
                  - Found org.gradle.status 'integration' but wasn't required.
                  - Required org.gradle.usage 'java-api' but no value provided.
          - Configuration 'scala-tool':
              - Other attributes:
                  - Required org.gradle.dependency.bundling 'external' but no value provided.
                  - Required org.gradle.jvm.version '8' but no value provided.
                  - Required org.gradle.libraryelements 'classes' but no value provided.
                  - Found org.gradle.status 'integration' but wasn't required.
                  - Required org.gradle.usage 'java-api' but no value provided.
          - Configuration 'sources':
              - Other attributes:
                  - Required org.gradle.dependency.bundling 'external' but no value provided.
                  - Required org.gradle.jvm.version '8' but no value provided.
                  - Required org.gradle.libraryelements 'classes' but no value provided.
                  - Found org.gradle.status 'integration' but wasn't required.
                  - Required org.gradle.usage 'java-api' but no value provided.
          - Configuration 'test':
              - Other attributes:
                  - Required org.gradle.dependency.bundling 'external' but no value provided.
                  - Required org.gradle.jvm.version '8' but no value provided.
                  - Required org.gradle.libraryelements 'classes' but no value provided.
                  - Found org.gradle.status 'integration' but wasn't required.
                  - Required org.gradle.usage 'java-api' but no value provided.
          - Configuration 'test-internal':
              - Other attributes:
                  - Required org.gradle.dependency.bundling 'external' but no value provided.
                  - Required org.gradle.jvm.version '8' but no value provided.
                  - Required org.gradle.libraryelements 'classes' but no value provided.
                  - Found org.gradle.status 'integration' but wasn't required.
                  - Required org.gradle.usage 'java-api' but no value provided.

* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.

* Get more help at https://help.gradle.org

BUILD FAILED in 539ms

```