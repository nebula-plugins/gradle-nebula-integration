`./gradlew compileJava` fails with this lock file

```
{
    "compileClasspath": {
        "com.google.guava:guava": {
            "locked": "19.0"
        },
        "netflix:platform": {
            "locked": "1.0.0",
            "requested": "1.0.0"
        }
    },
    "runtimeClasspath": {
        "com.google.guava:guava": {
            "locked": "19.0"
        },
                 "netflix:platform": {
                     "locked": "1.0.0",
                     "requested": "1.0.0"
                 }
    },
    "testCompileClasspath": {
        "com.google.guava:guava": {
            "locked": "19.0"
        },
        "netflix:platform": {
            "locked": "1.0.0",
            "requested": "1.0.0"
        }
    },
    "testRuntimeClasspath": {
        "com.google.guava:guava": {
            "locked": "19.0"
        },
        "netflix:platform": {
            "locked": "1.0.0",
            "requested": "1.0.0"
        }
    }
}
```

it works with

```
{
    "compileClasspath": {
        "com.google.guava:guava": {
            "locked": "19.0"
        }
    },
    "runtimeClasspath": {
        "com.google.guava:guava": {
            "locked": "19.0"
        }
    },
    "testCompileClasspath": {
        "com.google.guava:guava": {
            "locked": "19.0"
        }
    },
    "testRuntimeClasspath": {
        "com.google.guava:guava": {
            "locked": "19.0"
        }
    }
}
```