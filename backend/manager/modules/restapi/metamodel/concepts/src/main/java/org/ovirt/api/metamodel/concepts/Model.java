/*
Copyright (c) 2015 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.api.metamodel.concepts;

import static org.ovirt.api.metamodel.concepts.Concept.named;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class Model {
    // The list of modules of the model:
    private List<Module> modules = new ArrayList<>();

    // The list of types of the model:
    private List<Type> types = new ArrayList<>();

    // The list of services of the model:
    private List<Service> services = new ArrayList<>();

    // The root of the tree of services:
    private Service root;

    // The builtin types:
    private Type booleanType;
    private Type stringType;
    private Type integerType;
    private Type dateType;
    private Type decimalType;

    public Model() {
        // Create the anonymous module:
        Module anonymousModule = new Module();
        anonymousModule.setModel(this);
        modules.add(anonymousModule);

        // Add the builtin types:
        booleanType = new PrimitiveType();
        booleanType.setName(NameParser.parseUsingCase("Boolean"));
        booleanType.setModule(anonymousModule);
        addType(booleanType);

        stringType = new PrimitiveType();
        stringType.setName(NameParser.parseUsingCase("String"));
        stringType.setModule(anonymousModule);
        addType(stringType);

        integerType = new PrimitiveType();
        integerType.setName(NameParser.parseUsingCase("Integer"));
        integerType.setModule(anonymousModule);
        addType(integerType);

        dateType = new PrimitiveType();
        dateType.setName(NameParser.parseUsingCase("Date"));
        dateType.setModule(anonymousModule);
        addType(dateType);

        decimalType = new PrimitiveType();
        decimalType.setName(NameParser.parseUsingCase("Decimal"));
        decimalType.setModule(anonymousModule);
        addType(decimalType);
    }

    /**
     * Adds a module to the list of modules of this model.
     */
    public void addModule(Module newModule) {
        modules.add(newModule);
    }

    /**
     * Returns the list of modules of this model. The returned list is a copy of the one used internally, so it is safe
     * to modify it in any way. If you aren't going to modify the list consider using the {@link #modules()} method
     * instead.
     */
    public List<Module> getModules() {
        return new CopyOnWriteArrayList<>(modules);
    }

    /**
     * Returns a stream that delivers the modules of this model.
     */
    public Stream<Module> modules() {
        return modules.stream();
    }

    /**
     * Returns the module that has the given name, or {@code null} if there is no such module.
     */
    public Module getModule(Name name) {
        return modules.stream().filter(named(name)).findFirst().orElse(null);
    }

    /**
     * Adds a type to the list of types of this model.
     */
    public void addType(Type newType) {
        types.add(newType);
    }

    /**
     * Returns the list of types of this model. The returned list is a copy of the one used internally, so it is safe to
     * modify it in any way. If you aren't going to modify the list consider using the {@link #types()} method instead.
     */
    public List<Type> getTypes() {
        return new CopyOnWriteArrayList<>(types);
    }

    /**
     * Returns a stream that delivers the types of this model.
     */
    public Stream<Type> types() {
        return types.stream();
    }

    /**
     * Returns the type that has the given name, or {@code null} if there is no such type.
     */
    public Type getType(Name name) {
        return types.stream().filter(named(name)).findFirst().orElse(null);
    }

    /**
     * Adds a service to the list of services of this model.
     */
    public void addService(Service newService) {
        services.add(newService);
    }

    /**
     * Returns the list of services of this model. The returned list is a copy of the one used internally, so it is safe
     * to modify it in any way. If you aren't going to modify the list consider using the {@link #types()} method
     * instead.
     */
    public List<Service> getServices() {
        return services;
    }

    /**
     * Returns a stream that delivers the services of this model.
     */
    public Service getService(Name name) {
        return services.stream().filter(named(name)).findFirst().orElse(null);
    }

    /**
     * Returns the root of the services tree of this model.
     */
    public Service getRoot() {
        return root;
    }

    /**
     * Sets the root of the services tree of this model.
     */
    public void setRoot(Service newRoot) {
        root = newRoot;
    }

    /**
     * Returns a reference to the built-in string type of this model.
     */
    public Type getStringType() {
        return stringType;
    }

    /**
     * Returns a referene to the built-in integer type of this model.
     */
    public Type getIntegerType() {
        return integerType;
    }

    /**
     * Returns a reference to the built-in date type of this model.
     */
    public Type getDateType() {
        return dateType;
    }

    /**
     * Returns a reference to the built-in boolean type of this model.
     */
    public Type getBooleanType() {
        return booleanType;
    }

    /**
     * Returns a reference to the built-in decimal type of this model.
     */
    public Type getDecimalType() {
        return decimalType;
    }
}

