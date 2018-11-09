import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier
import org.gradle.util.VersionNumber

/**
 *
 *  Copyright 2018 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */


class Coordinate {
    String moduleIdentifier
    String version
    String moduleIdentifierWithVersion

    Coordinate(String moduleIdentifier, String version) {
        this.moduleIdentifier = moduleIdentifier
        this.version = version
        this.moduleIdentifierWithVersion = "$moduleIdentifier:$version"
    }

    Coordinate(String moduleIdentifierWithVersion) {
        this.moduleIdentifierWithVersion = moduleIdentifierWithVersion
        def split = moduleIdentifierWithVersion.split(':')
        this.moduleIdentifier = "${split[0]}:${split[1]}"
        this.version = split[2]
    }

    @Override
    String toString() {
        if (moduleIdentifierWithVersion != null) {
            return moduleIdentifierWithVersion
        }

        if (version != null) {
            return moduleIdentifier + ':' + version
        } else {
            return moduleIdentifier
        }
    }

    def toModuleVersionIdentifier() {
        if (moduleIdentifierWithVersion != null) {
            def split = moduleIdentifierWithVersion.split(':')
            return new DefaultModuleVersionIdentifier(split[0], split[1], split[2])
        } else {
            def split = moduleIdentifier.split(':')
            return new DefaultModuleVersionIdentifier(split[0], split[1], version)
        }

    }

    public final static Comparator<ModuleVersionIdentifier> DEPENDENCY_COMPARATOR = new Comparator<ModuleVersionIdentifier>() {
        @Override
        int compare(ModuleVersionIdentifier m1, ModuleVersionIdentifier m2) {
            if (m1.group != m2.group)
                return m1.group.compareTo(m2.group)
            else if (m1.name != m2.name)
                return m1.name.compareTo(m2.name)
            else
                return VersionNumber.parse(m2.version).compareTo(VersionNumber.parse(m1.version))
        }
    }

    static def returnLowerOf(String v1, String v2) {
        def compareTo = VersionNumber.parse(v1).compareTo(VersionNumber.parse(v2))
        if (compareTo < 0) {
            return v1
        }
        return v2
    }
}
