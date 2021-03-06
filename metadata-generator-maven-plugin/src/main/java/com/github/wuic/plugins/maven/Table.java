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
        private final Map<String, String> properties;

        /**
         * The annotated type.
         */
        private final Class<?> type;

        /**
         * <p>
         * Builds a new annotation info.
         * </p>
         *
         * @param type the annotated type
         */
        AnnotationInfo(final Class<?> type) {
            this.type = type;
            this.properties = new TreeMap<String, String>();
        }

        /**
         * <p>
         * Gets the properties.
         * </p>
         *
         * @return the properties
         */
        Map<String, String> getProperties() {
            return properties;
        }

        /**
         * <p>
         * Gets the annotated class.
         * </p>
         *
         * @return the annotated class
         */
        Class<?> getType() {
            return type;
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
     * Gets the collected property names. The common prefix of all properties will be replaced by a '*' character.
     * </p>
     *
     * @return the property names
     */
    public Set<String> getCollectedPropertyNames() {
        final Set<String> retval = new TreeSet<String>();

        for (final String property : collectedPropertyNames) {
            retval.add('*' + property.substring(ApplicationConfig.PREFIX.length()));
        }

        return retval;
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
    public void newCouple(final AnnotatedClass annotatedClass, final String propertyKey, final String value) {
        collectedPropertyNames.add(propertyKey);
        AnnotationInfo annotationInfo = annotationInfoMap.get(annotatedClass.getType().getTypeName());

        // First property for this class
        if (annotationInfo == null) {
            annotationInfo = new AnnotationInfo(annotatedClass.getType().getClassType());
            annotationInfoMap.put(annotatedClass.getType().getTypeName(), annotationInfo);
        }

        annotationInfo.properties.put(propertyKey, value);
    }
}

