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

import com.github.wuic.ApplicationConfig;
import com.github.wuic.config.ObjectBuilderFactory;
import com.github.wuic.context.ContextBuilder;
import com.github.wuic.engine.EngineService;
import com.github.wuic.nut.dao.NutDaoService;
import com.github.wuic.nut.filter.NutFilterService;
import com.github.wuic.util.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * <p>
 * This MOJO's goal is to generate HTML table which represent the properties extracted from the constructor's
 * param of annotated classes.
 * </p>
 *
 * @author Francois Clety
 * @author Guillaume Drouet
 * @since 0.5.2
 */
@Mojo(name = "scan", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresDependencyResolution = ResolutionScope.COMPILE)
public class AnnotationToHtmlMojo extends AbstractMojo {

    /**
     * The logger.
     */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Directory where process result should be written.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}")
    private String output;

    /**
     * The list of annotations to scan in the associated package.
     */
    private final Map<Class<? extends Annotation>, String> annotationToScan = new LinkedHashMap<Class<? extends Annotation>, String>() {
        {
            put(EngineService.class, EngineService.DEFAULT_SCAN_PACKAGE);
            put(NutDaoService.class, NutDaoService.DEFAULT_SCAN_PACKAGE);
            put(NutFilterService.class, NutFilterService.DEFAULT_SCAN_PACKAGE);
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws MojoExecutionException {
        final List<AnnotatedClass> listOfAnnotatedClasses = new ArrayList<AnnotatedClass>();

        for (final Map.Entry<Class<? extends Annotation>, String> entry : annotationToScan.entrySet()) {
            log.info("Scanning {} annotation in package '{}'", entry.getKey().getName(), entry.getValue());
            final ObjectBuilderFactory object = new ObjectBuilderFactory(entry.getKey(), entry.getValue());

            // scan the concerned package to retrieve all the annotated class
            final List<ObjectBuilderFactory.KnownType> listOfKnownTypes = object.knownTypes();

            for (final ObjectBuilderFactory.KnownType knownType : listOfKnownTypes) {
                final AnnotatedClass component = new AnnotatedClass(entry.getValue(), knownType);
                component.fillProperties(object.create(knownType.toString()));
                listOfAnnotatedClasses.add(component);
            }
        }

        try {
            write(createModelForHtmlTabs(listOfAnnotatedClasses));
        } catch (IOException ioe) {
            log.error("Can't generate HTML file", ioe);
            throw new MojoExecutionException(ioe.getMessage());
        }
    }

    /**
     * <p>
     * Sets the output.
     * </p>
     *
     * @param output the output
     */
    public void setOutput(final String output) {
        this.output = output;
    }

    /**
     * <p>
     * Writes the given table as an HTML file to disk.
     * </p>
     *
     * @param table the table
     * @throws IOException if I/O error occurs
     */
    private void write(final Map<String, Table> table) throws IOException {

        // Write a new table for each entry
        for (final Table t : table.values()) {
            final Set<String> propertyNames = t.getCollectedPropertyNames();

            PrintWriter pw = null;

            try {
                pw = new PrintWriter(new FileOutputStream(new File(output, t.getName() + ".html")));

                pw.println("<h3>Components discovered under package " + t.getName() + "</h3>");
                pw.println("<span><b>Note:</b>");
                pw.println("In property name, <code>*</code> at the beginning of the name should be replaced by <code>");
                pw.println(ApplicationConfig.PREFIX + "</code></span>");
                pw.println("<table>");
                pw.println("<tr><td>Class/Property</td><td>Default ID</td>");

                // The headers is the list fo detected property
                for (final String property : propertyNames) {
                    pw.println("<td>" + property + "</td>");
                }

                pw.print("</tr>");

                // Add a line for each component
                for (final Map.Entry<String, Table.AnnotationInfo> e : t.getAnnotationInfoMap().entrySet()) {
                    pw.print("<tr>");
                    pw.print("<td>" + e.getKey() + "</td>");
                    pw.print("<td>" + ContextBuilder.getDefaultBuilderId(e.getValue().getType()) + "</td>");

                    // For each property, indicate the default value if applicable
                    for (final String property : propertyNames) {
                        final String val = e.getValue().getProperties().get(ApplicationConfig.PREFIX + property.substring(1));

                        pw.print("<td>");

                        // Display the default value as it is except for empty strings
                        if (val != null) {
                            pw.print(val.isEmpty() ? "<i>Empty String</i>" : val);
                        } else {
                            // Property not managed by this component
                            pw.print("<i>N/A</i>");
                        }

                        pw.println("</td>");
                    }

                    pw.println("</tr>");
                }

                pw.println("</table>");
            } finally {
                IOUtils.close(pw);
            }
        }
    }

    /**
     * <p>
     * This method returns a model in order to have a better data structure to then construct the HTML tables.
     * </p>
     *
     * @param listOfAnnotatedClasses the list of annotated classes
     * @return a {@code Map} corresponding to the tables to display
     */
    private Map<String, Table> createModelForHtmlTabs(final List<AnnotatedClass> listOfAnnotatedClasses) {
        final Map<String, Table> table = new TreeMap<String, Table>();

        // Put the class information in the right table
        for (final AnnotatedClass annotatedClass : listOfAnnotatedClasses) {
            final Table tableOfThePackage;

            // if the annotatedClass is in a non-treated package, we create the associated table
            if (!table.containsKey(annotatedClass.getPackageOfTheClass())) {
                tableOfThePackage = new Table(annotatedClass.getPackageOfTheClass());
                table.put(annotatedClass.getPackageOfTheClass(), tableOfThePackage);
            } else {
                tableOfThePackage = table.get(annotatedClass.getPackageOfTheClass());
            }

            for (final Map.Entry<String, Object> entry : annotatedClass.getProperties().entrySet()) {
                final String cle = entry.getKey();
                tableOfThePackage.newCouple(annotatedClass, cle, toString(entry.getValue()));
            }
        }

        return table;
    }

    /**
     * <p>
     * Returns a {@code String} representation from the given object, picking the value at index 0 in case of array
     * and the class name if not {@code null}, not {@code String}, not {@code Boolean} and not {@code Integer}.
     * </p>
     *
     * @param value the object
     * @return the string representation
     */
    private String toString(final Object value) {
        if (value == null || value instanceof String || value instanceof Boolean || value instanceof Integer) {
            return String.valueOf(value);
        } else if (value instanceof Object[]) {
            return toString(((Object[]) value)[0]);
        } else {
            return value.getClass().getName();
        }
    }
}
