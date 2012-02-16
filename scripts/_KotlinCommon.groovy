/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Andres Almiray
 */

import griffon.util.GriffonExceptionHandler

includePluginScript('lang-bridge', '_Commons')

target(name: 'compileKotlinSrc', description: 'Compiles Kotlin sources', prehook: null, posthook: null) {
    depends(classpath, compileCommons)

    ant.taskdef(resource: 'org/jetbrains/jet/buildtools/ant/antlib.xml')

    String classpathId = 'kotlin.compile.classpath'
    ant.path(id: classpathId) {
        path(refid: 'griffon.compile.classpath')
        pathElement(location: projectMainClassesDir)
        for (File f in griffonSettings.buildDependencies) {
            if (f && f.exists()) {
                pathelement(location: f.absolutePath)
                addUrlIfNotPresent rootLoader, f
            }
        }
    }

    if (argsMap.compileTrace) {
        println('-' * 80)
        println "[GRIFFON] '${classpathId}' entries"
        ant.project.getReference(classpathId).list().each {println("  $it")}
        println('-' * 80)
    }
    
    if (isDebugEnabled()) {
        debug "=== RootLoader urls === "
        rootLoader.URLs.each {debug("  $it")}
    }

    def kotlinSrc = "${basedir}/src/kotlin"
    ant.mkdir(dir: kotlinSrc)
    def kotlinSrcDir = new File(kotlinSrc)
    // def kotlinArtifactSrc = resolveResources("file:/${basedir}/griffon-app/**/*.kt")

    if(/*!kotlinArtifactSrc &&*/ !kotlinSrcDir.list().size()) {
        ant.echo(message: "[kotlin] No Kotlin sources were found.")
        return
    }
    
    // TODO check sources are up to date

    try {
        ant.kotlinc(
            output: projectMainClassesDir,
            src: kotlinSrc,
            classpathref: classpathId
        )
    } catch(Exception e) {
        if(argsMap.compileTrace) {
            GriffonExceptionHandler.sanitize(e)
            e.printStackTrace(System.err)
        }
        event("StatusFinal", ["Compilation error: ${e.message}"])
        exit(1)
    }
}
