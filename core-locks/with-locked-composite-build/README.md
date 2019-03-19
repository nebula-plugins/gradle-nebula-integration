This is an example with a `composite` project and a `dependency` project.

Given that I am using Gradle core locks in the project `composite`
And I have locked my dependencies
When I define a composite build via `settings.gradle`
Then I expect my composite build to work without error

Actual behavior: I receive an error:

```
Could not determine the dependencies of task ':compileJava'.
> Could not resolve all task dependencies for configuration ':compileClasspath'.
   > Did not resolve 'gradle.nebula.integration:dependency-project:0.1.0' which is part of the dependency lock state
```
