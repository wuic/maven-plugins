/*
 * "Copyright (c) 2015   Capgemini Technology Services (hereinafter "Capgemini")
 *
 * License/Terms of Use
 * Permission is hereby granted, free of charge and for the term of intellectual
 * property rights on the Software, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to use, copy, modify and
 * propagate free of charge, anywhere in the world, all or part of the Software
 * subject to the following mandatory conditions:
 *
 * -   The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Any failure to comply with the above shall automatically terminate the license
 * and be construed as a breach of these Terms of Use causing significant harm to
 * Capgemini.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, PEACEFUL ENJOYMENT,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Capgemini shall not be used in
 * advertising or otherwise to promote the use or other dealings in this Software
 * without prior written authorization from Capgemini.
 *
 * These Terms of Use are subject to French law.
 *
 * IMPORTANT NOTICE: The WUIC software implements software components governed by
 * open source software licenses (BSD and Apache) of which CAPGEMINI is not the
 * author or the editor. The rights granted on the said software components are
 * governed by the specific terms and conditions specified by Apache 2.0 and BSD
 * licenses."
 */


package com.github.wuic.plugins.maven;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class represents a model for the annotation: it will contain the annotated class, the property and its
 * default value.
 *
 * @author Francois Clety
 * @author Guillaume Drouet
 * @since 0.5.2
 */
public class Table {

    /**
     * <p>
     * Internal class that just wrap all properties with their default value.
     * </p>
     *
     * @author Francois Clety
     * @author Guillaume Drouet
     * @since 0.5.3
     */
    static class AnnotationInfo {

        /**
         * The properties.
         */
        private Map<String, String> properties = new TreeMap<String, String>();

        /**
         * <p>
         * Gets the properties.
         * </p>
         *
         * @return the properties
         */
        public Map<String, String> getProperties() {
            return properties;
        }
    }

    /**
     * The name of the annotation.
     */
    private String name;

    /**
     * The property names.
     */
    private final Set<String> collectedPropertyNames;

    /**
     * The map which will contain the couple class->property, and the default value of this property.
     */
    private final Map<String, AnnotationInfo> annotationInfoMap;

    /**
     * simple constructor which establish the name and instanciate the map
     *
     * @param name the name
     */
    public Table(final String name) {
        this.collectedPropertyNames = new TreeSet<String>();
        this.name = name;
        this.annotationInfoMap = new TreeMap<String, AnnotationInfo>();
    }

    /**
     * Getter for the name.
     *
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * <p>
     * Gets the collected property names.
     * </p>
     *
     * @return the property names
     */
    public Set<String> getCollectedPropertyNames() {
        return collectedPropertyNames;
    }

    /**
     * <p>
     * Gets all properties with their default value per component.
     * </p>
     *
     * @return the map
     */
    public Map<String, AnnotationInfo> getAnnotationInfoMap() {
        return annotationInfoMap;
    }

    /**
     * <p>
     * Method which fill the map.
     * </p>
     *
     * @param propertyKey the key
     * @param annotatedClass the class
     * @param value the default value
     */
    public void newCouple(final String annotatedClass, final String propertyKey, final String value) {
        collectedPropertyNames.add(propertyKey);
        AnnotationInfo annotationInfo = annotationInfoMap.get(annotatedClass);

        // First property for this class
        if (annotationInfo == null) {
            annotationInfo = new AnnotationInfo();
            annotationInfoMap.put(annotatedClass, annotationInfo);
        }

        annotationInfo.properties.put(propertyKey, value);
    }
}

