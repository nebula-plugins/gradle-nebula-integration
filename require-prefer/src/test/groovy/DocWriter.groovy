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

    void writeProjectFiles(String optionalFolderDecorator = null, onlyWriteGradleFiles = false) {
        def childFolderName = optionalFolderDecorator == null ? 'input' : "input-$optionalFolderDecorator"
        def destinationDir = new File(depFolder, childFolderName)
        destinationDir.deleteDir()
        destinationDir.mkdirs()

        if (onlyWriteGradleFiles) {
            FileFilter gradleSuffixFilter = new SuffixFileFilter('.gradle')
            FileFilter notHiddenGradleFiles = new NotFileFilter(new NameFileFilter('.gradle'))
            def fileFilters = new ArrayList<IOFileFilter>()
            fileFilters.add(gradleSuffixFilter)
            fileFilters.add(notHiddenGradleFiles)

            FileFilter combinationFilter = new AndFileFilter(fileFilters)

            FileUtils.copyDirectory(projectDir, destinationDir, combinationFilter)
        } else {
            FileFilter notHiddenGradleFiles = new NotFileFilter(new NameFileFilter('.gradle'))
            FileFilter notUserHomeFiles = new NotFileFilter(new NameFileFilter('userHome'))
            FileFilter notBuildFiles = new NotFileFilter(new NameFileFilter('build'))
            def fileFilters = new ArrayList<IOFileFilter>()
            fileFilters.add(notHiddenGradleFiles)
            fileFilters.add(notUserHomeFiles)
            fileFilters.add(notBuildFiles)

            FileFilter combinationFilter = new AndFileFilter(fileFilters)

            FileUtils.copyDirectory(projectDir, destinationDir, combinationFilter)
        }

        def buildFile = new File(destinationDir, 'build.gradle')
        buildFile.text = buildFile.text.replaceAll("'.*/repo", "'../../../../repo")
                .replaceAll("'.*build/.*/ivy-repo", "'ivy-repo")
                .replaceAll("'.*/build/.*/maven-repo", "'maven-repo") //FIXME

        def directoryNames = destinationDir.list(DirectoryFileFilter.INSTANCE)
        for (String dirName : directoryNames) {
            def directory = new File(destinationDir, dirName)
            replacePathForRepo(directory)
            makeLastUpdatedTimestampConsistent(directory)
            makeBuildIdSha1AndMD5Consistent(directory)
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

    private static void makeBuildIdSha1AndMD5Consistent(File directory) {
        def moduleFiles = FileUtils.listFiles(directory, new SuffixFileFilter('.module'), TrueFileFilter.INSTANCE)
        for (File moduleFile : moduleFiles) {
            moduleFile.text = moduleFile.text
                    .replaceAll("\"buildId\": \".*\"", "\"buildId\": \"aaaaabbbbbcccccdddddeeeeef\"")
                    .replaceAll("\"sha1\": \".*\"", "\"sha1\": \"0123456789012345678901234567890123456789\"")
                    .replaceAll("\"md5\": \".*\"", "\"md5\": \"01234567890123456789012345678901\"")
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
