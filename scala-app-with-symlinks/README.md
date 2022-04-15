# scala-app-with-symlinks

## Context

Users trying to find ways to do cross compilation and cross publishing in Scala + Gradle

## Setup

* `app_2.13.8` uses scala 2.13.8 
* `app` 2.11.12

Catch: `src` in `app_2.13.8` is a symlink to `src` in `app`

User(s) setup this way to re-use the component, expecting Gradle to re-use the source and compile accordingly in each submodule (`build` folder)

## Behavior

When running something like `./gradlew :app_2.13.8:run`, you can see

```
> Task :app_2.13.8:run
*****lalalalalalala****
*****lalalalalalala****
*****lalalalalalala****
*****lalalalalalala****
*****lalalalalalala****
hello

BUILD SUCCESSFUL in 3s
```

Then, if you modify `app/src/main/scala/test/scala/app/App.scala` and add more `println`s, 
it results in `compileScala` being `UP-TO-DATE`. 

In this case, the user expected Gradle to detect the change in the source code and re-compile.
