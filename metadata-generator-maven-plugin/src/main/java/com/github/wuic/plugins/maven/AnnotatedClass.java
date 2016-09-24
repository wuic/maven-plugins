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

import com.github.wuic.config.ObjectBuilder;
import com.github.wuic.config.ObjectBuilderFactory;

import java.util.Map;

/**
 * This class represents an annotated class: its properties, its package and its type.
 *
 * @author Francois Clety
 * @author Guillaume Drouet
 * @since 0.5.2
 */
public class AnnotatedClass {

    /**
     * All properties with their default values.
     */
    private Map<String, Object> properties;

    /**
     * The package where the classes are located.
     */
    private String packageOfTheClass;

    /**
     * The scanner.
     */
    private ObjectBuilderFactory.KnownType type;

    /**
     * Main constructor for the annotated class.
     *
     * @param packageOfTheClass the package to scan
     * @param knownType the known type
     */
    public AnnotatedClass(final String packageOfTheClass,
                          final ObjectBuilderFactory.KnownType knownType) {
        this.packageOfTheClass = packageOfTheClass;
        this.type = knownType;
    }

    /**
     * Getter for the properties.
     *
     * @return the properties and their default values
     */
    public Map<String, Object> getProperties() {
        return this.properties;
    }

    /**
     * Getter for the type.
     *
     * @return the scanned type
     */
    public ObjectBuilderFactory.KnownType getType() {
        return this.type;
    }

    /**
     * Getter for the package.
     *
     * @return The scanned package
     */
    public String getPackageOfTheClass() {
        return this.packageOfTheClass;
    }

    /**
     * <p>
     * Method which fill the properties of the class thanks to the {@code builder}.
     * </p>
     *
     * @param objectBuilder the object builder
     */
    public void fillProperties(final ObjectBuilder<ObjectBuilderFactory.KnownType> objectBuilder) {
        this.properties = objectBuilder.getProperties();
    }
}