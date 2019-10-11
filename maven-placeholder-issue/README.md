We noticed builds breaking started in `6.0-20190909220034+0000`.

Running `./gradlew build` results in:

```
❯ ./gradlew build
> Task :compileScala FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':compileScala'.
> Could not resolve all files for configuration ':compileClasspath'.
   > Could not resolve test:child_2.12:1.0.0.
     Required by:
         project :
      > Could not resolve test:child_2.12:1.0.0.
         > inconsistent module metadata found. Descriptor: test:child_${scala.binary.version}:1.0.0 Errors: bad module name: expected='child_2.12' found='child_${scala.binary.version}'

```
