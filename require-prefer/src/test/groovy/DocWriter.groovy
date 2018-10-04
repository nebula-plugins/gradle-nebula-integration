import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.AndFileFilter
import org.apache.commons.io.filefilter.DirectoryFileFilter
import org.apache.commons.io.filefilter.IOFileFilter
import org.apache.commons.io.filefilter.NameFileFilter
import org.apache.commons.io.filefilter.NotFileFilter
import org.apache.commons.io.filefilter.SuffixFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter

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

    private File projectDir
    private File depFolder
    private File outputFile

    DocWriter(String title, File projectDir, String group) {
        this.projectDir = projectDir
        this.title = title

        File docs = new File("docs")
        docs.mkdirs()

        File groupDir = new File(docs, group)
        groupDir.mkdirs()

        depFolder = new File(groupDir, title)
        depFolder.mkdirs()

        outputFile = new File(depFolder, 'output.txt')
        outputFile.delete()
        outputFile.createNewFile()
    }

    void writeCleanedUpBuildOutput(String output) {
        outputFile << output.replaceAll('BUILD SUCCESSFUL in .*s', 'BUILD SUCCESSFUL')
                .replaceAll('publication=".*"', 'publication="recently"')

        outputFile << """
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

        def buildFile = new File(destinationDir, 'build.gradle')
        buildFile.text = buildFile.text.replaceAll("'.*/repo", "'../../../../repo")
                .replaceAll("'.*build/.*/ivy-repo", "'ivy-repo")
                .replaceAll("'.*/build/.*/maven-repo", "'maven-repo") //FIXME

        def directoryNames = destinationDir.list(DirectoryFileFilter.INSTANCE)
        for (String dirName : directoryNames) {
            def directory = new File(destinationDir, dirName)
            replacePathForRepo(directory)
            makeLastUpdatedTimestampConsistent(directory)
            makeBuildIdConsistent(directory)
        }
    }

    private static void replacePathForRepo(File directory) {
        def innerBuildFileNames = directory.list(new NameFileFilter('build.gradle'))
        for (String buildFileName : innerBuildFileNames) {
            def innerBuildFile = new File(directory, buildFileName)
            innerBuildFile.text = innerBuildFile.text.replaceAll("'.*/repo", "'../../../../../repo")
        }
    }

    private static void makeLastUpdatedTimestampConsistent(File directory) {
        def xmlFiles = FileUtils.listFiles(directory, new SuffixFileFilter('.xml'), TrueFileFilter.INSTANCE)
        for (File xmlFile : xmlFiles) {
            xmlFile.text = xmlFile.text.replaceAll("<lastUpdated>.*</lastUpdated>", "<lastUpdated>20181001020304</lastUpdated>")
                    .replaceAll("publication=\".*\"", "publication=\"20181001020304\"")
        }
    }

    private static void makeBuildIdConsistent(File directory) {
        def moduleFiles = FileUtils.listFiles(directory, new SuffixFileFilter('.module'), TrueFileFilter.INSTANCE)
        for (File moduleFile : moduleFiles) {
            moduleFile.text = moduleFile.text.replaceAll("\"buildId\": \".*\"", "\"buildId\": \"aaaaabbbbbcccccdddddeeeeef\"")
        }
    }

    def addAssertionToDoc(String message) {
        outputFile << "- $message\n"
    }

    def writeGradleVersion(String message) {
        outputFile << "=== Using Gradle version ===\n$message\n\n"
    }

    void writeFooter(String first) {
        outputFile << "\n$first\n"
    }
}
