package com.github.wuic.plugins.maven;

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

import com.github.wuic.config.ObjectBuilder;
import com.github.wuic.config.ObjectBuilderFactory;
import com.github.wuic.engine.EngineService;
import com.github.wuic.nut.dao.NutDaoService;
import com.github.wuic.nut.filter.NutFilterService;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 *  <p>
 * This MOJO's goal is to generated asciidoctor arrays which represents the properties of the constructor's param of
 * annotated classes.
 * </p>
 */
@Mojo(name = "scan", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresDependencyResolution = ResolutionScope
        .TEST)
public class AnnoToAdocMojo extends AbstractMojo {

    /**
     * Fail message to display when an error occurs.
     */
    private static final String FAIL_MESSAGE = String.format("Unable to run %s", AnnoToAdocMojo.class.getName());

    /**
     * The logger.
     */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * List of annotated classes
     */
    private ArrayList<AnnotatedClass> ListOfAnnotatedClasses;

    /**
     * Maven project.
     */
    @Component
    private MavenProject project;


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws MojoExecutionException {
        this.ListOfAnnotatedClasses = new ArrayList<AnnotatedClass>();

        /* scan the concerned package to retrieve all the annotated class */
        List<ObjectBuilderFactory.KnownType> listOfEngineKnownTypes = this.getListOfKnownTypes(EngineService.class, EngineService.DEFAULT_SCAN_PACKAGE);
        for(final ObjectBuilderFactory.KnownType knownType: listOfEngineKnownTypes){
            AnnotatedClass engine = new AnnotatedClass(EngineService.class, EngineService.DEFAULT_SCAN_PACKAGE);
            engine.fillProperties(knownType); //fillProperties fill the ListOfAnnotatedClass
        }

        List<ObjectBuilderFactory.KnownType> listOfNutDaoKnownTypes = this.getListOfKnownTypes(NutDaoService.class,
                NutDaoService.DEFAULT_SCAN_PACKAGE);
        for(final ObjectBuilderFactory.KnownType knownType: listOfNutDaoKnownTypes){
            AnnotatedClass nutDao = new AnnotatedClass(NutDaoService.class, NutDaoService.DEFAULT_SCAN_PACKAGE);
            nutDao.fillProperties(knownType);
        }

        List<ObjectBuilderFactory.KnownType> listOfNutFilterKnownTypes = this.getListOfKnownTypes(NutFilterService
                .class, NutFilterService.DEFAULT_SCAN_PACKAGE);
        for(final ObjectBuilderFactory.KnownType knownType: listOfNutFilterKnownTypes){
            AnnotatedClass nutFilter = new AnnotatedClass(NutFilterService.class, NutFilterService.DEFAULT_SCAN_PACKAGE);
            nutFilter.fillProperties(knownType);
        }

        Map<String, Table> table = createModelForAsciidocTabs(ListOfAnnotatedClasses);
    }

    public List<ObjectBuilderFactory.KnownType> getListOfKnownTypes(final Class<? extends Annotation>
                                                                            relatedAnnotation, final String
                                                                            packageOfTheClass){
        ObjectBuilderFactory object = new ObjectBuilderFactory(relatedAnnotation, packageOfTheClass);
        List<ObjectBuilderFactory.KnownType> listOfKnownTypes = object.knownTypes();
        return listOfKnownTypes;
    }

    /**
     * This class represents a model for the annotation : it will contains the annotated class, the property and its
     * default value
     * */
    private class Table {

        /**
         * the name of the annotation
         */
        private String name;

        /**
         * the map which will contain the couple class->property, and the default value of this property
         */
        private Map<Map<String,String>,String> table;

        /**
         * setter for the name
         * @param name
         */
        public void setName(String name){
            this.name = name;
        }

        /**
         * getter for the name
         * @return (String) name
         */
        public String getName(){
            return this.name;
        }

        /**
         * getter for the table
         * @return (Map<Map<String,String>,String> table
         */
        public Map<Map<String,String>,String> getTable(){
            return this.table;
        }

        /**
         * simple constructor which establish the name and instanciate the map
         * @param name
         */
        public Table (String name){
            this.name = name;
            this.table = new HashMap<Map<String, String>, String>();
        }

        /**
         * method which fill the map
         * @param propertyKey
         * @param annotatedClass
         * @param value
         */
        public void newCouple(String propertyKey, String annotatedClass, String value){
            Map<String,String> couple = new HashMap<String, String>();
            couple.put(propertyKey,annotatedClass);
            this.table.put(couple,value);
        }
    }

    /**
     * This class represents an annotated class : its properties, its package, its related annotation, its type
     */
    private class AnnotatedClass {

        private Map<String,Object> properties;

        private String packageOfTheClass;

        private Class<? extends Annotation> relatedAnnotation;

        private ObjectBuilderFactory.KnownType type;

        /**
         *  Main constructor for the annotated class
         **/
        public AnnotatedClass(final Class<? extends Annotation>
                                      relatedAnnotation, final String
                                      packageOfTheClass){

            this.packageOfTheClass = packageOfTheClass;
            this.relatedAnnotation = relatedAnnotation;
        }

        /**
         * getter for the properties
         * @return (Map<String,Object>) properties
         */
        public Map<String,Object> getProperties(){
            return this.properties;
        }

        /**
         * getter for the type
         * @return (KnownType) type
         */
        public ObjectBuilderFactory.KnownType getType(){
            return this.type;
        }

        /**
         * getter for the package
         * @return (String) package
         */
        public String getPackageOfTheClass(){ return this.packageOfTheClass; }

        /**
         * method which fill the properties of the class thanks to the knownType, and add the class to the
         * listOfAnnotatedClass
         * @param knownType
         */
        public void fillProperties(ObjectBuilderFactory.KnownType knownType){
            this.type = knownType;
            ObjectBuilderFactory object = new ObjectBuilderFactory(this.relatedAnnotation,this.packageOfTheClass);
            ObjectBuilder<ObjectBuilderFactory.KnownType> objectBuilder = object.create(knownType.toString());
            this.properties = objectBuilder.getProperties();
            ListOfAnnotatedClasses.add(this);
        }
    }

    /**
     * this method return a model, in order to have a better data structure to then construct the asciidoctor arrays
     * @param ListOfAnnotatedClasses
     * @return
     */
    public Map<String,Table> createModelForAsciidocTabs(ArrayList<AnnotatedClass> ListOfAnnotatedClasses){

        Map<String,Table> table = new HashMap<String,Table>();

        for(AnnotatedClass annotatedClass : ListOfAnnotatedClasses){

            /* if the annotatedClass is in a non-treated package, we create the associated table */
            if(!table.containsKey(annotatedClass.getPackageOfTheClass())){
                Table tableOfThePackage = new Table(annotatedClass.getPackageOfTheClass());
                for(Map.Entry<String,Object> entry : annotatedClass.getProperties().entrySet()){
                    String cle = entry.getKey();
                    String value;
                    /* we have to test if the value is null, in order not to throw a NullPointerException */
                    if(entry.getValue() != null){
                        value = entry.getValue().toString();
                    }
                    else{
                        value = "null";
                    }
                    // TODO : modify the behaviour of what value should be if it's an ObjectConfigParam
                    tableOfThePackage.newCouple(annotatedClass.getType().getTypeName(), cle, value);
                }
                table.put(annotatedClass.getPackageOfTheClass(), tableOfThePackage);
            }

            /* else we have to retrieve the existant to test and add in the correct table */
            else {
                Collection values = table.values();
                Iterator valuesIt = values.iterator();
                while(valuesIt.hasNext()){
                    Table existant = (Table) valuesIt.next();
                    for(Map.Entry<String,Object> entry : annotatedClass.getProperties().entrySet()){
                        if(existant.getName().equals(annotatedClass.getPackageOfTheClass())){
                            String cle = entry.getKey();
                            String value;
                            /* we have to test if the value is null, in order not to throw a NullPointerException */
                            if(entry.getValue() != null){
                                value = entry.getValue().toString();
                            }
                            else{
                                value = "null";
                            }
                            // TODO : modify the behaviour of what value should be if it's an ObjectConfigParam
                            existant.newCouple(annotatedClass.getType().getTypeName(), cle, value);
                        }
                    }
                }
            }
        }
        return table;
    }
}
