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

public class Service extends Concept {
    private Module module;
    private List<Method> methods = new ArrayList<>();
    private List<Locator> locators = new ArrayList<>();
    private List<Constraint> constraints = new ArrayList<>();

    public Module getModule() {
        return module;
    }

    public void setModule(Module newModule) {
        module = newModule;
    }

    public List<Locator> getLocators() {
        return locators;
    }

    public void addLocator(Locator newLocator) {
        locators.add(newLocator);
    }

    public void addLocators(List<Locator> newLocators) {
        this.locators.addAll(newLocators);
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void addMethod(Method action) {
        methods.add(action);
    }

    public void addMethods(List<Method> actions) {
        this.methods.addAll(actions);
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void addConstraint(Constraint constraint) {
        constraints.add(constraint);
    }

    public void addConstraints(List<Constraint> constraints) {
        this.constraints.addAll(constraints);
    }
}

