package gradle5.rc.issue

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.internal.project.ProjectInternal

class StatusAttribute {

    static void configureStatusAttribute(Project project) {
        project.ext.release = {
            withStatus('release')
        }

        project.ext.candidate = {
            withStatus('candidate')
        }

        project.buildscript {
            configureDefaultStatusAttribute(it.configurations)
        }
    }

    static void configureDefaultStatusAttribute(configurations) {
        configurations.all {
            attributes(withStatus('release'))
        }
    }

    static Action<AttributeContainer> withStatus(String requestedStatus) {
        return new Action<AttributeContainer>() {
            void execute(AttributeContainer attributes) {
                attributes.attribute(ProjectInternal.STATUS_ATTRIBUTE, requestedStatus)
            }
        }
    }

}