/*
 * Copyright (c) 2016   The authors of WUIC
 *
 * License/Terms of Use
 * Permission is hereby granted, free of charge and for the term of intellectual
 * property rights on the Software, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to use, copy, modify and
 * propagate free of charge, anywhere in the world, all or part of the Software
 * subject to the following mandatory conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, PEACEFUL ENJOYMENT,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


package com.github.wuic.plugins.maven;

import com.github.wuic.WuicTask;
import com.github.wuic.exception.WuicException;
import com.github.wuic.util.IOUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * <p>
 * This MOJO helps to process nuts for static usage. "Static" means that nuts can't be processed at runtime so we have
 * to invoke engine's chain of responsibility at build-time. This plugin supports only configuration from XML file.
 * An optional 'wuic.properties' file location could be also specified.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.4.1
 */
@Mojo(name = "process", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresDependencyResolution = ResolutionScope.COMPILE)
public class StaticHelperMojo extends AbstractMojo {

    /**
     * Fail message to display when an error occurs.
     */
    private static final String FAIL_MESSAGE = String.format("Unable to run %s", StaticHelperMojo.class.getName());

    /**
     * Maven project.
     */
    @Component
    private MavenProject project;

    /**
     * <p>
     * Used to make addition of resources simpler.
     * </p>
     */
    @Component
    private MavenProjectHelper projectHelper;

    /**
     * The "xml" configuration parameter.
     */
    @Parameter
    private String xml;

    /**
     * The "properties" configuration parameter.
     */
    @Parameter
    private String properties;

    /**
     * Includes the transformed XML file as source file named 'wuic.xml'.
     */
    @Parameter(defaultValue = "false")
    private Boolean relocateTransformedXml;

    /**
     * Directory where process result should be written.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}")
    private String output;

    /**
     * Base path where every processed statics referenced by HTML will be served.
     */
    @Parameter(defaultValue = "/")
    private String contextPath;

    /**
     * Pattern matching name of files to keep on the top of directory structure.
     */
    @Parameter
    private String moveToTopDirPattern;

    /**
     * Base directory to scan.
     */
    @Parameter
    private String baseDir;

    /**
     * The path to detect.
     */
    @Parameter
    private String path;

    /**
     * The task name used to configure heap ID resolving the configured paths and base directory.
     */
    @Parameter(defaultValue = "wuic-task")
    private String taskName;

    /**
     * Consider the path as a regex instead of a regex.
     */
    @Parameter(defaultValue = "false")
    private String useRegex;

    /**
     * Package the output as a JAR file. The JAR is written to{@link #output}.
     */
    @Parameter(defaultValue = "true")
    private String packageAsJar;

    /**
     * <p>
     * Adds into the classpath the project's resources.
     * </p>
     *
     * @throws MojoExecutionException if an error occurs
     * @throws IOException if an I/O error occurs
     */
    @SuppressWarnings("unchecked")
    private void setClasspath() throws MojoExecutionException, IOException {
        final URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();

        try {
            for (final Resource artifact : (List<Resource>) project.getResources()) {
                getLog().info(artifact.getDirectory());
                final Class urlClass = URLClassLoader.class;
                final Method method = urlClass.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                method.invoke(urlClassLoader, new File(artifact.getDirectory()).toURI().toURL());
            }
        } catch (NoSuchMethodException nsme) {
            throw new MojoExecutionException(FAIL_MESSAGE, nsme);
        } catch (IllegalAccessException iae) {
            throw new MojoExecutionException(FAIL_MESSAGE, iae);
        } catch (InvocationTargetException ite) {
            throw new MojoExecutionException(FAIL_MESSAGE, ite);
        }
    }

    /**
     * <p>
     * Loads the active profiles.
     * </p>
     *
     * @return the active profiles
     */
    private String loadProfiles() {
        if (project.getActiveProfiles() == null || project.getActiveProfiles().isEmpty()) {
            return null;
        }

        final StringBuilder profiles = new StringBuilder();

        for (final Object profile : project.getActiveProfiles()) {
            profiles.append(',').append(String.valueOf(profile));
        }

        return profiles.substring(1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws MojoExecutionException {
        try {
            setClasspath();

            final Build b = project.getBuild();
            final String o = b.getOutputDirectory().equals(output) ? output : IOUtils.mergePath(b.getDirectory(), output);
            final WuicTask task = new WuicTask();
            task.setXml(xml);
            task.setContextPath(contextPath);
            task.setProperties(properties);
            task.setProfiles(loadProfiles());
            task.setOutput(o);
            task.setMoveToTopDirPattern(moveToTopDirPattern);
            task.setBaseDir(baseDir);
            task.setPath(path);
            task.setTaskName(taskName);
            task.setUseRegex(Boolean.parseBoolean(useRegex));
            task.setPackageAsJar(Boolean.parseBoolean(packageAsJar));

            if (relocateTransformedXml) {
                final File temp = File.createTempFile("tempXml", Long.toString(System.nanoTime()));

                if (!temp.delete() || !temp.mkdirs()) {
                    throw new IOException(String.format("Could not delete temp '%s' directory for transformed XML configuration file",
                            temp.getAbsolutePath()));
                }

                task.setRelocateTransformedXmlTo(temp.toString());
                final List<String> relocated = task.executeTask();
                projectHelper.addResource(project, temp.toString(), relocated, null);
            } else {
                task.execute();
            }
        } catch (WuicException we) {
            throw new MojoExecutionException(FAIL_MESSAGE, we);
        } catch (MalformedURLException mue) {
            throw new MojoExecutionException(FAIL_MESSAGE, mue);
        } catch (JAXBException je) {
            throw new MojoExecutionException(FAIL_MESSAGE, je);
        } catch (IOException ioe) {
            throw new MojoExecutionException(FAIL_MESSAGE, ioe);
        }
    }

    /**
     * <p>
     * Sets the "xml" configuration parameter.
     * </p>
     *
     * @param x the parameter
     */
    public void setXml(final String x) {
        xml = x;
    }

    /**
     * <p>
     * Sets the "properties" configuration parameter.
     * </p>
     *
     * @param properties the properties
     */
    public void setProperties(final String properties) {
        this.properties = properties;
    }

    /**
     * <p>
     * Sets the relocate transformed XML flag.
     * </p>
     *
     * @param relocateTransformedXml the new flag
     */
    public void setRelocateTransformedXml(final Boolean relocateTransformedXml) {
        this.relocateTransformedXml = relocateTransformedXml;
    }

    /**
     * <p>
     * Sets the maven project.
     * </p>
     *
     * @param mp the maven project
     */
    public void setMavenProject(final MavenProject mp) {
        project = mp;
    }

    /**
     * <p>
     * Sets the helper.
     * </p>
     *
     * @param projectHelper the helper
     */
    public void setProjectHelper(final MavenProjectHelper projectHelper) {
        this.projectHelper = projectHelper;
    }

    /**
     * <p>
     * Sets the output directory.
     * </p>
     *
     * @param output the output directory
     */
    public void setOutput(final String output) {
        this.output = output;
    }

    /**
     * <p>
     * Sets the context path.
     * </p>
     *
     * @param contextPath the context path
     */
    public void setContextPath(final String contextPath) {
        this.contextPath = contextPath;
    }
}