## A simplified example that creates a binary store exception

To re-create the binary store exception:

Run `./gradlew dependencies`

Here is some of the output:

Dependency 1:
```
compileClasspath - Compile classpath for source set 'main'.
\--- a:a:1.0.0
     \--- b:b:1.0.0
          \--- c:c:1.0.0
               \--- from:this:1.0.0 -> to:this:1.0.0
```

Dependency 2:
```
myConfiguration
\--- m:m:1.0.0
     \--- n:n:1.0.0
          \--- o:o:1.0.0
               \--- p:p:1.0.0
                    \--- q:q:1.0.0
                         \--- a:a:2.0.0
                              \--- b:b:1.0.0
                                   \--- c:c:1.0.0
                                        \--- from:this:1.0.0 -> to:this:1.0.0
```
