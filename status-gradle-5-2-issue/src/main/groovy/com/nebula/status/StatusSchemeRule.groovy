package com.nebula.status

import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataDetails
import org.gradle.api.artifacts.ComponentMetadataRule

class StatusSchemeRule implements ComponentMetadataRule {
    @Override
    void execute(ComponentMetadataContext componentMetadataContext) {
        defineStatuses(componentMetadataContext.details, new ArrayList<>())
    }

    static void defineStatuses(ComponentMetadataDetails details, List<String> statusesOverride) {
        // Its detail.metadata is either ModuleDescriptorAdapter or DefaultBuildableModuleVersionMetaDataResolveResult
        // Both have a ModuleDescriptor moduleDescriptor
        def version = details.id.version
        if (version =~ StatusSchemePlugin.CANDIDATE_VERSION) {
            details.status = 'candidate'
        }
        if (version =~ StatusSchemePlugin.SNAPSHOT_VERSION) {
            details.status = 'snapshot'
        }
        // Seen coming from maven
        if (details.status == null) {
            details.status = 'release'
        }

        if (details.status == 'snapshot' || details.status == 'integration') {
            details.changing = true
        }

        details.statusScheme = statusesOverride != null && statusesOverride.size() != 0 ?
                statusesOverride :
                StatusSchemePlugin.DEFAULT_STATUS_SCHEME

    }
}
