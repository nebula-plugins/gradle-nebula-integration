# Project where dependency resolution keeps changing

Run the following to see differences in resolution. The results are trackable because they are moved to a git-tracked folder and are then committed. 

If you see `nothing to commit, working tree clean`, then the current dependency resolution is the same as the previous one. The `interesting-plugin` changes between 2 versions, so this happens sometimes. In that case, run the following again, and you will see the still-changing dependency resolution once more.

```
./resolveAndStoreDependencies.sh && git commit -am "re-resolve dependencies" \
  && ./resolveAndStoreDependencies.sh && git commit -am "run resolveAndStoreDependencies and see a different resolution." \
  && ./resolveAndStoreDependencies.sh && git commit -am "run resolveAndStoreDependencies and see a different resolution." \
  && ./resolveAndStoreDependencies.sh && git commit -am "run resolveAndStoreDependencies and see a different resolution."
```