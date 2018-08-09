Example of difference when resolution strategy is applied.

With nebula implementation, group is aligned in the end an rule effect is overriden.

Call: `./gradlew dependencyInsight --configuration compile --dependency test.nebula`

```
test.nebula:a:1.0.0
   variant "default" [
      org.gradle.status = release (not requested)
   ]
   Selection reasons:
      - Was requested
      - Selected by rule : aligned to 1.0.0 by rules
                with reasons: nebula.resolution-rules uses: rules.json

test.nebula:a:1.0.0
\--- compile

test.nebula:b:1.0.0
   variant "default" [
      org.gradle.status = release (not requested)
   ]
   Selection reasons:
      - Was requested
      - Selected by rule : aligned to 1.0.0 by rules
                with reasons: nebula.resolution-rules uses: rules.json

test.nebula:b:0.15.0 -> 1.0.0
\--- compile

test.nebula:c:1.0.0
   variant "default" [
      org.gradle.status = release (not requested)
   ]

test.nebula:c:1.0.0
\--- compile
```

With gradle core based implementation module `a` is brought down by a rule and it causes missaligned group.

Call: `./gradlew dependencyInsight --configuration compile --dependency test.nebula -Dnebula.features.coreAlignmentSupport=true`

```
test.nebula:a:0.15.0
   variant "default" [
      org.gradle.status = release (not requested)
   ]
   Selection reasons:
      - Was requested
      - By constraint : belongs to platform aligned-platform:rules-0:1.0.0
      - Selected by rule

test.nebula:a:1.0.0 -> 0.15.0
\--- compile

test.nebula:b:1.0.0
   variant "default" [
      org.gradle.status = release (not requested)
   ]
   Selection reasons:
      - Was requested
      - By constraint : belongs to platform aligned-platform:rules-0:1.0.0
      - By conflict resolution : between versions 0.15.0 and 1.0.0

test.nebula:b:0.15.0 -> 1.0.0
\--- compile

test.nebula:c:1.0.0
   variant "default" [
      org.gradle.status = release (not requested)
   ]
   Selection reasons:
      - Was requested
      - By constraint : belongs to platform aligned-platform:rules-0:1.0.0

test.nebula:c:1.0.0
\--- compile
```