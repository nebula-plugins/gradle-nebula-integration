# Aligned group with replacement and Nebula resolution rules

When using a replacement group and alignment rule, then the replaced group should be aligned as well.

```
./gradlew dependencyInsight -Dnebula.features.coreAlignmentSupport=true --dependency aws-java-sdk --singlepath
```

I expect to see all `com.amazonaws` libraries at an aligned version, and I do not see this.

The output contains information on the replaced dependency:

```
com.amazonaws:aws-java-sdk:1.3.11
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
      - Selected by rule : replacement com.amazon:aws-java-sdk -> com.amazonaws:aws-java-sdk
                with reasons: nebula.resolution-rules uses: rules.json

com.amazon:aws-java-sdk:1.3.11 -> com.amazonaws:aws-java-sdk:1.3.11
\--- compileClasspath
```

but there is nothing in here about aligned groups, as seen in the subsequent dependencies as `belongs to platform aligned-platform:rules-0-for-com.amazonaws:1.11.702`:

```
com.amazonaws:aws-java-sdk-autoscaling:1.11.702
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
      - By constraint : belongs to platform aligned-platform:rules-0-for-com.amazonaws:1.11.702
      - By conflict resolution : between versions 1.11.702 and 1.11.521

com.amazonaws:aws-java-sdk-autoscaling:1.11.521 -> 1.11.702
\--- compileClasspath

com.amazonaws:aws-java-sdk-cloudwatch:1.11.702
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
      - By constraint : belongs to platform aligned-platform:rules-0-for-com.amazonaws:1.11.702

com.amazonaws:aws-java-sdk-cloudwatch:1.11.702
\--- compileClasspath

com.amazonaws:aws-java-sdk-core:1.11.702
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
      - By constraint : belongs to platform aligned-platform:rules-0-for-com.amazonaws:1.11.702
      - By conflict resolution : between versions 1.11.702 and 1.11.521

com.amazonaws:aws-java-sdk-core:1.11.702
\--- com.amazonaws:aws-java-sdk-autoscaling:1.11.702
     \--- compileClasspath (requested com.amazonaws:aws-java-sdk-autoscaling:1.11.521)
```
