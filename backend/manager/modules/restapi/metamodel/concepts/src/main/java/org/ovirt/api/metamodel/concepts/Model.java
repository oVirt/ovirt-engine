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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        modules.add(anonymousModule);

        // Add the builtin types:
        booleanType = new PrimitiveType();
        booleanType.setName(NameParser.parseUsingCase("Boolean"));
        addType(booleanType);

        stringType = new PrimitiveType();
        stringType.setName(NameParser.parseUsingCase("String"));
        addType(stringType);

        integerType = new PrimitiveType();
        integerType.setName(NameParser.parseUsingCase("Integer"));
        addType(integerType);

        dateType = new PrimitiveType();
        dateType.setName(NameParser.parseUsingCase("Date"));
        addType(dateType);

        decimalType = new PrimitiveType();
        decimalType.setName(NameParser.parseUsingCase("Decimal"));
        addType(decimalType);

    }

    public void addModule(Module newModule) {
        modules.add(newModule);
    }

    public List<Module> getModules() {
        return modules;
    }

    public Module getModule(Name name) {
        for (Module module : modules) {
            if (Objects.equals(name, module.getName())) {
                return module;
            }
        }
        return null;
    }

    public void addType(Type newType) {
        types.add(newType);
    }

    public List<Type> getTypes() {
        return types;
    }

    public Type getType(Name name) {
        return types.stream()
            .filter(x -> x != null && x.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    public void addService(Service newService) {
        services.add(newService);
    }

    public List<Service> getServices() {
        return services;
    }

    public Service getService(Name name) {
        return services.stream()
            .filter(x -> x != null && x.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    public Service getRoot() {
        return root;
    }

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

