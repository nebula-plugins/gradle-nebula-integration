package gradle5.rc.issue

import org.gradle.api.artifacts.CacheableRule
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule

@CacheableRule
class CacheableStatusRule implements ComponentMetadataRule {
    static final CANDIDATE_VERSION = ~/(?i).+(-|\.)(BETA|CANDIDATE|CR|RC).*/

    static final List<String> DEFAULT_STATUS_SCHEME = ['candidate', 'release']

    @Override
    void execute(ComponentMetadataContext componentMetadataContext) {
        println("Executing CacheableStatusRule")
        def details = componentMetadataContext.details
        def version = details.id.version
        if (version =~ CANDIDATE_VERSION) {
            details.status = 'candidate'
        }

        if (details.status == null) {
            details.status = 'release'
        }

        details.statusScheme = DEFAULT_STATUS_SCHEME
    }
}

