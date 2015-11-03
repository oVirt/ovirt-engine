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

import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Service extends Concept {
    private Module module;
    private Service base;
    private List<Method> methods = new ArrayList<>();
    private List<Locator> locators = new ArrayList<>();
    private List<Constraint> constraints = new ArrayList<>();

    public Module getModule() {
        return module;
    }

    public void setModule(Module newModule) {
        module = newModule;
    }

    public Service getBase() {
        return base;
    }

    public void setBase(Service newBase) {
        base = newBase;
    }

    /**
     * Returns all the locators of this service, including the ones declared in base services. The returned list is a
     * copy of the one used internally, so it is safe to modify it.
     */
    public List<Locator> getLocators() {
        List<Locator> result = new ArrayList<>(locators);
        if (base != null) {
            result.addAll(base.getLocators());
        }
        result.sort(comparing(Locator::getName));
        return result;
    }

    /**
     * Returns the list of locators that are declared directly in this servic3, not included the ones that are declared
     * in the base services. The returned list is a sorted copy of the one used internally, so it is safe to modify it.
     */
    public List<Locator> getDeclaredLocators() {
        return new ArrayList<>(locators);
    }

    /**
     * Adds a new locator to this service.
     */
    public void addLocator(Locator newLocator) {
        locators.add(newLocator);
        locators.sort(comparing(Locator::getName));
    }

    /**
     * Adds a list of new locators to this service.
     */
    public void addLocators(List<Locator> newLocators) {
        locators.addAll(newLocators);
        locators.sort(comparing(Locator::getName));
    }

    /**
     * Returns all the methods of this service, including the ones declared in base types. The returned list is a sorted
     * copy of the one used internally, so it is safe to modify it.
     */
    public List<Method> getMethods() {
        List<Method> result = new ArrayList<>(methods);
        if (base != null) {
            result.addAll(base.getMethods());
        }
        result.sort(comparing(Method::getName));
        return result;
    }

    /**
     * Returns the list of methods that are declared directly in this service, not included the ones that are declared
     * in the base services. The returned list is a sorted copy of the one used internally, so it is safe to modify it.
     */
    public List<Method> getDeclaredMethods() {
        return new ArrayList<>(methods);
    }

    /**
     * Finds a method of this service with the given name. The search will be performed in the set of methods of this
     * service and its base services. If a method with the given name is declared directly in this service it will have
     * preference over other methods with the same name declared in the base services.
     *
     * @param name the name of the method to find
     * @return the method with the given name or {@code null if no such method exists}
     */
    public Method getMethod(Name name) {
        for (Method method : methods) {
            if (Objects.equals(method.getName(), name)) {
                return method;
            }
        }
        if (base != null) {
            return base.getMethod(name);
        }
        return null;
    }

    /**
     * Checks if this service or any of its base services have a method with the given name.
     *
     * @param name the name of the method to check
     * @return {@code true} if the method exists, {@code false} otherwise
     */
    public boolean hasMethod(Name name) {
        for (Method method : methods) {
            if (Objects.equals(method.getName(), name)) {
                return true;
            }
        }
        if (base != null) {
            return base.hasMethod(name);
        }
        return false;
    }

    /**
     * Adds a new method to this service.
     */
    public void addMethod(Method newMethod) {
        methods.add(newMethod);
        methods.sort(comparing(Method::getName));
    }

    /**
     * Returns all the constraints of this service, including the ones declared in base services. The returned list is
     * a sorted copy of the one used internally, so it is safe to modify it.
     * @return
     */
    public List<Constraint> getConstraints() {
        List<Constraint> result = new ArrayList<>(constraints);
        if (base != null) {
            result.addAll(base.getConstraints());
        }
        result.sort(comparing(Constraint::getName));
        return result;
    }

    /**
     * Returns the list of constraints that are declared directly in this service, not including the ones that are
     * declared in the base services. The returned list is a sorted copy of the one used internally, so it is safe to
     * modify it.
     */
    public List<Constraint> getDeclaredConstraints() {
        return new ArrayList<>(constraints);
    }

    /**
     * Adds a new constraint to this service.
     */
    public void addConstraint(Constraint newConstraint) {
        constraints.add(newConstraint);
        constraints.sort(comparing(Constraint::getName));
    }

    /**
     * Adds a list of new constraints to this service.
     */
    public void addConstraints(List<Constraint> newConstraints) {
        constraints.addAll(newConstraints);
        constraints.sort(comparing(Constraint::getName));
    }

    public Model getModel() {
        return module.getModel();
    }
}

