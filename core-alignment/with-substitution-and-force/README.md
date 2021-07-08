# Core alignment with a substitution and a force

There are a few cases that show different behavior that I'm interested in:

* When a substitution is triggered from a direct dependency
* When a substitution is triggered from a transitive dependency

and when

* The force is on one dependency in an aligned group
* The force is on all dependencies in an aligned group
* `useVersion` is used to make the aligned group use the same version

In each case, a dependency substitution is triggered. In each case, there is a `configurations.all { dependencyResolution {` in place.

I see that some combinations resolve successfully while others fail with a multiple forces exception. And for the cases that resolve successfully, some use the forced version and some used the substituted version.

Examples with multiple-forces exceptions:

* When a substitution is triggered from a direct dependency and forces are on all aligned dependencies
* When a substitution is triggered from a transitive dependency and forces are on one dependency (`test.nebula:a`)
* When a substitution is triggered from a transitive dependency and forces are on all aligned dependencies

I'm wondering if this is also bringing us back to understanding the intent of a `resolutionStrategy` block as in <https://github.com/nebula-plugins/gradle-nebula-integration/issues/11>
