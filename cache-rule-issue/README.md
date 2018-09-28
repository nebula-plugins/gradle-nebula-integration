# cache-rule-issue

We implemented a CacheableStatusRule that is `CacheableRule`.

However, when we run our test to execute the build twice, we see the println from the rule "Executing CacheableStatusRule".


Already made sure our project has `project.gradle.startParameter.buildCacheEnable` enabled