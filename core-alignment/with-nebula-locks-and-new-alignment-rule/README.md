# Project using core alignment, Nebula locks, & adding a new alignment rule shows that the locked version is not the final resolved version

## How to reproduce

When dependencies are first added to the build file:
```
dependencies {
    implementation 'test.nebula:a:1.0.0'
    implementation 'test.nebula:b:1.2.0'
}
```
and then they are locked via the Nebula locking plugin with `./gradlew generateLock saveLock` to produce a `dependencies.lock` file

Then we can see the following in the `dependencyInsight`

```
./gradlew dependencyInsight --dependency test.nebula
```

```
> Task :dependencyInsight
test.nebula:a:1.0.0
   Selection reasons:
      - Selected by rule : test.nebula:a locked to 1.0.0
                with reasons: nebula.dependency-lock locked with: dependencies.lock

test.nebula:a:1.0.0
\--- compileClasspath

test.nebula:b:1.2.0
   Selection reasons:
      - Selected by rule : test.nebula:b locked to 1.2.0
                with reasons: nebula.dependency-lock locked with: dependencies.lock

test.nebula:b:1.2.0
\--- compileClasspath
```

When we add a new alignment rule, however:

```
project.dependencies.components.all(AlignGroup.class)

class AlignGroup implements ComponentMetadataRule {
    void execute(ComponentMetadataContext ctx) {
        ctx.details.with { it ->
            if (it.getId().getGroup().startsWith("test.nebula")) {
                it.belongsTo("test.nebula:test.nebula:${it.getId().getVersion()}")
            }
        }
    }
}
```

then we can see the following in the dependencyInsight, which shows that the locked version is no longer the resolved version:

```
> Task :dependencyInsight
test.nebula:a:1.2.0
   Selection reasons:
      - By constraint : belongs to platform test.nebula:test.nebula:1.2.0
      - Selected by rule : test.nebula:a locked to 1.0.0
                with reasons: nebula.dependency-lock locked with: dependencies.lock
      - By conflict resolution : between versions 1.2.0 and 1.0.0

test.nebula:a:1.0.0 -> 1.2.0
\--- compileClasspath

test.nebula:b:1.2.0
   Selection reasons:
      - Selected by rule : test.nebula:b locked to 1.2.0
                with reasons: nebula.dependency-lock locked with: dependencies.lock
      - By constraint : belongs to platform test.nebula:test.nebula:1.2.0

test.nebula:b:1.2.0
\--- compileClasspath
```

The `conflict resolution : between versions 1.2.0 and 1.0.0` shows that the locked version of `1.0.0` conflicts with the aligned version `1.2.0` and resolves to the higher version.

## Gradle setup

Using Gradle 6.7-rc-1

## Scope of the issue

While resolution rules coming from <https://github.com/nebula-plugins/gradle-resolution-rules> have locked versions in our projects, folks can add new local-to-their-project rules which can then affect resolution