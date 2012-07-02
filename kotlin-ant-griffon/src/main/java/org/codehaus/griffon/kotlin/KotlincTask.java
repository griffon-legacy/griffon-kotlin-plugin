package org.codehaus.griffon.kotlin;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.GlobPatternMapper;
import org.apache.tools.ant.util.SourceFileScanner;
import org.jetbrains.jet.buildtools.core.BytecodeCompiler;
import org.jetbrains.jet.cli.common.messages.MessageCollector;
import org.jetbrains.jet.cli.jvm.compiler.*;
import org.jetbrains.jet.lang.resolve.java.CompilerDependencies;
import org.jetbrains.jet.lang.resolve.java.CompilerSpecialMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jetbrains.jet.buildtools.core.Util.getPath;

/**
 * Copy of {@code org.jetbrains.jet.buildtools.ant.BytecodeCompilerTask} with further customizations.
 *
 * @author Andres Almiray
 */
public class KotlincTask extends MatchingTask {
    protected final LoggingHelper log = new LoggingHelper(this);

    protected File output;
    protected File jar;
    protected File stdlib;
    protected Path src;
    protected File module;
    protected Path compileClasspath;
    protected boolean includeRuntime = true;
    protected boolean force;

    public void setOutput(File output) {
        this.output = output;
    }

    public void setJar(File jar) {
        this.jar = jar;
    }

    public void setStdlib(File stdlib) {
        this.stdlib = stdlib;
    }

    public void setModule(File module) {
        this.module = module;
    }

    public void setIncludeRuntime(boolean includeRuntime) {
        this.includeRuntime = includeRuntime;
    }

    public Path createSrc() {
        if (src == null) {
            src = new Path(getProject());
        }
        return src.createPath();
    }

    public void setSrcdir(final Path dir) {
        assert dir != null;

        if (src == null) {
            src = dir;
        } else {
            src.append(dir);
        }
    }

    public Path getSrcdir() {
        return src;
    }

    /**
     * Set the classpath to be used for this compilation.
     *
     * @param classpath an Ant Path object containing the compilation classpath.
     */
    public void setClasspath(Path classpath) {
        if (this.compileClasspath == null) {
            this.compileClasspath = classpath;
        } else {
            this.compileClasspath.append(classpath);
        }
    }


    /**
     * Adds a reference to a classpath defined elsewhere.
     *
     * @param ref a reference to a classpath.
     */
    public void setClasspathRef(Reference ref) {
        if (this.compileClasspath == null) {
            this.compileClasspath = new Path(getProject());
        }
        this.compileClasspath.createPath().setRefid(ref);
    }

    /**
     * Set the nested {@code <classpath>} to be used for this compilation.
     *
     * @param classpath an Ant Path object containing the compilation classpath.
     */
    public void addConfiguredClasspath(Path classpath) {
        setClasspath(classpath);
    }


    @Override
    public void execute() {
        GlobPatternMapper mapper = new GlobPatternMapper();
        mapper.setFrom("*.kt");
        mapper.setTo("*.class");

        int count = 0;
        String[] list = src.list();

        List<String> sources = new ArrayList<String>();

        for (int i = 0; i < list.length; i++) {
            File basedir = getProject().resolveFile(list[i]);

            if (!basedir.exists()) {
                throw new CompileEnvironmentException("Source directory does not exist: " + basedir);
            }

            DirectoryScanner scanner = getDirectoryScanner(basedir);
            String[] includes = scanner.getIncludedFiles();

            if (force) {
                log.debug("Forcefully including all files from: " + basedir);

                for (int j = 0; j < includes.length; j++) {
                    File file = new File(basedir, includes[j]);
                    log.debug("    " + file);

                    sources.add(file.getAbsolutePath());
                    count++;
                }
            } else {
                log.debug("Including changed files from: " + basedir);

                SourceFileScanner sourceScanner = new SourceFileScanner(this);
                File[] files = sourceScanner.restrictAsFiles(includes, basedir, output, mapper);

                for (int j = 0; j < files.length; j++) {
                    log.debug("    " + files[j]);

                    sources.add(files[j].getAbsolutePath());
                    count++;
                }
            }
        }

        if (count > 0) {
            log.info("Compiling " + count + " source file" + (count > 1 ? "s" : "") + " to " + output);
            compile(sources);
        } else {
            log.info("No sources found to compile");
        }
    }

    private void compile(List<String> sources) {
        System.out.println("==============");
        System.out.println(sources);
        System.out.println("==============");
        final BytecodeCompiler compiler = new BytecodeCompiler();
        final String stdlibPath = (this.stdlib != null ? getPath(this.stdlib) : null);
        final String[] classpath = (this.compileClasspath != null ? this.compileClasspath.list() : null);

        try {
            K2JVMCompileEnvironmentConfiguration configuration = env(compiler, stdlibPath, classpath);

            boolean success = KotlinToJVMBytecodeCompiler.compileBunchOfSourceDirectories(configuration, sources, null, output.getAbsolutePath(), false, true);
            if (!success) {
                throw new BuildException(errorMessage(false));
            }
        } catch (Exception e) {
            throw new BuildException(errorMessage(true), e);
        }
    }

    // -- From org.jetbrains.jet.buildtools.core.BytecodeCompiler

    /**
     * Creates new instance of {@link org.jetbrains.jet.cli.jvm.compiler.K2JVMCompileEnvironmentConfiguration} instance using the arguments specified.
     *
     * @param stdlib    path to "kotlin-runtime.jar", only used if not null and not empty
     * @param classpath compilation classpath, only used if not null and not empty
     * @return compile environment instance
     */
    private K2JVMCompileEnvironmentConfiguration env(BytecodeCompiler compiler, String stdlib, String[] classpath) {
        CompilerDependencies dependencies = CompilerDependencies.compilerDependenciesForProduction(CompilerSpecialMode.REGULAR);
        JetCoreEnvironment environment = new JetCoreEnvironment(CompileEnvironmentUtil.createMockDisposable(), dependencies);
        K2JVMCompileEnvironmentConfiguration
                env = new K2JVMCompileEnvironmentConfiguration(environment, MessageCollector.PLAIN_TEXT_TO_SYSTEM_ERR, false, Collections.<String>emptyList());

        if ((stdlib != null) && (stdlib.trim().length() > 0)) {
            File file = new File(stdlib);
            CompileEnvironmentUtil.addToClasspath(env.getEnvironment(), file);
        }

        if ((classpath != null) && (classpath.length > 0)) {
            CompileEnvironmentUtil.addToClasspath(env.getEnvironment(), classpath);
        }

        // lets register any compiler plugins
        env.getCompilerPlugins().addAll(compiler.getCompilerPlugins());

        return env;
    }

    /**
     * Retrieves compilation error message.
     *
     * @param exceptionThrown whether compilation failed due to exception thrown
     * @return compilation error message
     */
    private static String errorMessage(boolean exceptionThrown) {
        return String.format("Compilation failed" +
                (exceptionThrown ? "" : ", see \"ERROR:\" messages above for more details."));
    }
}