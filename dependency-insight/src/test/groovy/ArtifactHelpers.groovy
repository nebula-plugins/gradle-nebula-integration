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


import groovy.transform.PackageScope
import nebula.test.dependencies.maven.Pom

import java.util.jar.Attributes
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import java.util.zip.ZipEntry

@PackageScope
class ArtifactHelpers {
    protected static File setupSamplePomWith(File repo, Pom pom, String sampleFileContents) {
        def sample = new File(repo, getPathFromPom(pom))
        sample.mkdirs()
        def sampleFile = new File(sample, pom.getArtifact().getArtifact() + '-' + pom.getArtifact().getVersion() + '.pom')
        sampleFile << sampleFileContents
    }

    private static String getPathFromPom(Pom pom) {
        def pomArtifact = pom.getArtifact()
        pomArtifact.getGroup().replace('.', File.separator) +
                File.separator +
                pomArtifact.getArtifact().replace('.', File.separator) +
                File.separator +
                pomArtifact.getVersion()
    }

    protected static setupSampleJar(File repo, Pom pom) {
        def pathFromPom = getPathFromPom(pom)

        def sample = new File(repo, pathFromPom)
        sample.mkdirs()
        def pomArtifact = pom.getArtifact()
        def sampleJarClasspath = repo.absolutePath + File.separator +
                pathFromPom + File.separator +
                pomArtifact.getArtifact().replace('.', File.separator) + '-' + pomArtifact.getVersion() + '.jar'
        def jar = new File(sampleJarClasspath)
        jar.createNewFile()
        createJar(jar, manifestWithClasspath(sampleJarClasspath))
    }

    protected static setupSampleJarInLibsDir(File projectDir, String jarName) {
        def libsDir = new File(projectDir, 'libs')
        libsDir.mkdirs()

        def jarClasspath = libsDir.absolutePath + File.separator + jarName + '.jar'
        def jar = new File(jarClasspath)
        jar.createNewFile()
        createJar(jar, manifestWithClasspath(jarClasspath))
    }

    private static def createJar(File jarFile, Manifest manifest = null) throws IOException {
        def jarOutputStream

        if (manifest == null) {
            jarOutputStream = new JarOutputStream(new FileOutputStream(jarFile))
        } else {
            jarOutputStream = new JarOutputStream(new FileOutputStream(jarFile), manifest)
        }

        jarOutputStream.putNextEntry(new ZipEntry("META-INF/"))
        jarOutputStream.close()
    }

    private static Manifest manifestWithClasspath(def manifestClasspath) {
        Manifest manifest = new Manifest()
        Attributes attributes = manifest.getMainAttributes()
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0")
        if (manifestClasspath != null) {
            attributes.putValue("Class-Path", manifestClasspath)
        }
        return manifest
    }
}
