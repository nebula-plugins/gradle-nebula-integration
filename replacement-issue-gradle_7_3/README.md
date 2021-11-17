# replacement-issue-gradle_7_3

Basically:

`common` requests `a` and `a` requests `b-standalone-original`
`data-interface` requests `b-standalone`

When this happens, this replacement rule triggers

```
"replace" : [
    {
      "module" : "netflix:b-standalone",
      "with" : "netflix:b-standalone-original",
      "reason": "test",
      "author": "test",
      "date": "9999-06-22"
    }
  ] 
```

which is good. However, the problem comes when `b-standalone` also asks for `b-standalone-original`

```
 <dependency>
            <groupId>netflix</groupId>
            <artifactId>b-standalone-original</artifactId>
            <version>1.0.0</version>
            <scope>compile</scope>
        </dependency>
```

When I try to generate my locks I can see

```
* What went wrong:
Execution failed for task ':server:generateLock'.
> Could not resolve all dependencies for configuration ':server:runtimeClasspath'.
   > Problems reading data from Binary store in /Users/rperezalcolea/.gradle/.tmp/gradle4057093498228885854.bin offset 1934 exists? true
```

and then looking at https://github.com/gradle/gradle/blob/master/subprojects/dependency-management/src/main/java/org/gradle/api/internal/artifacts/ivyservice/resolveengine/oldresult/DefaultResolvedConfigurationBuilder.java#L48 results in

```
this = {DefaultResolvedConfigurationBuilder@11455} 
parent = {NodeState@11453} "netflix:b-standalone:1.0.0(runtime)"
child = {NodeState@11454} "netflix:b-standalone-original:1.0.0(runtime)"
artifactsId = 3
```

So I think the problem surfaces when you have a replacement but also the module that you want to replace includes the replacement in the POM file which we have seen many times as part of basically a strategy to include the new module.
