import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.AndFileFilter
import org.apache.commons.io.filefilter.IOFileFilter
import org.apache.commons.io.filefilter.NameFileFilter
import org.apache.commons.io.filefilter.NotFileFilter

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


class DocWriter {
    private String title
    private String insightSource

    private File projectDir
    private File depFolder

    DocWriter(String title, String insightSource, File projectDir, String group) {
        this.projectDir = projectDir
        this.title = title
        this.insightSource = insightSource

        File docs = new File("docs")
        docs.mkdirs()

        File groupDir = new File(docs, group)
        docs.mkdirs()

        depFolder = new File(groupDir, title)
        depFolder.mkdirs()
    }

    void writeCleanedUpBuildOutput(String output) {
        def file = new File(depFolder, 'output.txt')
        file.delete()
        file.createNewFile()

        file << output.replaceAll("nebula.dependency-recommender uses a properties file: .*/investigate-insight",
                "nebula.dependency-recommender uses a properties file: ./investigate-insight")
                .replaceAll('BUILD SUCCESSFUL in .*s', 'BUILD SUCCESSFUL')

        file << """
=== Asserting on... ===
""".stripIndent()
    }

    void writeProjectFiles() {
        def destinationDir = new File(depFolder, 'input')
        destinationDir.deleteDir()
        destinationDir.mkdirs()

        FileFilter notGradleFiles = new NotFileFilter(new NameFileFilter('.gradle'))
        FileFilter notUserHomeFiles = new NotFileFilter(new NameFileFilter('userHome'))
        def fileFilters = new ArrayList<IOFileFilter>()
        fileFilters.add(notGradleFiles)
        fileFilters.add(notUserHomeFiles)

        FileFilter combinationFilter = new AndFileFilter(fileFilters)

        FileUtils.copyDirectory(projectDir, destinationDir, combinationFilter)
    }

    def addAssertionToDoc(String message) {
        def file = new File(depFolder, 'output.txt')

        file << "- $message\n"
    }

    void writeFooter(String first) {
        def file = new File(depFolder, 'output.txt')

        file << "\n$first\n"
    }
}
