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

import static java.util.stream.Stream.concat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

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
     * copy of the one used internally, so it is safe to modify it. If you aren't going to modify the list consider
     * using the {@link #locators()} method instead.
     */
    public List<Locator> getLocators() {
        List<Locator> result = new ArrayList<>(locators);
        if (base != null) {
            result.addAll(base.getLocators());
        }
        return result;
    }

    /**
     * Returns a stream that delivers all the locators of this service, including the ones declared in base services.
     */
    public Stream<Locator> locators() {
        Stream<Locator> result = declaredLocators();
        if (base != null) {
            result = concat(base.locators(), result);
        }
        return result;
    }

    /**
     * Returns the list of locators that are declared directly in this service, not included the ones that are declared
     * in the base services. The returned list is a copy of the one used internally, so it is safe to modify it. If you
     * aren't going to modify the list consider using the {@link #declaredLocators()} method instead.
     */
    public List<Locator> getDeclaredLocators() {
        return new CopyOnWriteArrayList<>(locators);
    }

    /**
     * Returns a stream that delivers the locators that are declared directly in this service, not including the ones
     * that are declared in the base services.
     */
    public Stream<Locator> declaredLocators() {
        return locators.stream();
    }

    /**
     * Adds a new locator to this service.
     */
    public void addLocator(Locator newLocator) {
        locators.add(newLocator);
    }

    /**
     * Adds a list of new locators to this service.
     */
    public void addLocators(List<Locator> newLocators) {
        locators.addAll(newLocators);
    }

    /**
     * Returns all the methods of this service, including the ones declared in base types. The returned list is a copy
     * of the one used internally, so it is safe to modify it.
     */
    public List<Method> getMethods() {
        List<Method> result = new ArrayList<>(methods);
        if (base != null) {
            result.addAll(base.getMethods());
        }
        return result;
    }

    /**
     * Returns a stream that delivers all the methods of this service, including the ones declared in base services.
     */
    public Stream<Method> methods() {
        Stream<Method> result = declaredMethods();
        if (base != null) {
            result = concat(base.methods(), result);
        }
        return result;
    }

    /**
     * Returns the list of methods that are declared directly in this service, not included the ones that are declared
     * in the base services. The returned list is a copy of the one used internally, so it is safe to modify it. If you
     * aren't going to modify the list consider using the {@link #declaredMethods()} method instead.
     */
    public List<Method> getDeclaredMethods() {
        return new CopyOnWriteArrayList<>(methods);
    }

    /**
     * Returns a stream that delivers the methods that are declared directly in this service, not including the ones
     * that are declared in the base services.
     */
    public Stream<Method> declaredMethods() {
        return methods.stream();
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
        Optional<Method> method = methods.stream().filter(named(name)).findFirst();
        if (method.isPresent()) {
            return method.get();
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
        return methods().anyMatch(named(name));
    }

    /**
     * Adds a new method to this service.
     */
    public void addMethod(Method newMethod) {
        methods.add(newMethod);
    }

    /**
     * Adds a new list of methods to this service.
     */
    public void addMethods(List<Method> newMethod) {
        methods.addAll(newMethod);
    }

    /**
     * Returns all the constraints of this service, including the ones declared in base services. The returned list is
     * a copy of the one used internally, so it is safe to modify it. If you aren't going to modify the list consider
     * using the {@link #constraints()} method instead.
     */
    public List<Constraint> getConstraints() {
        List<Constraint> result = new ArrayList<>(constraints);
        if (base != null) {
            result.addAll(base.getConstraints());
        }
        return result;
    }

    /**
     * Returns a stream that delivers all the constraints of this service, including the ones declared in base services.
     */
    public Stream<Constraint> constraints() {
        Stream<Constraint> result = declaredConstraints();
        if (base != null) {
            result = concat(base.constraints(), result);
        }
        return result;
    }

    /**
     * Returns the list of constraints that are declared directly in this service, not including the ones that are
     * declared in the base services. The returned list is a copy of the one used internally, so it is safe to
     * modify it. If you aren't going to modify the list consider using the {@link #declaredConstraints()} method
     * instead.
     */
    public List<Constraint> getDeclaredConstraints() {
        return new CopyOnWriteArrayList<>(constraints);
    }

    /**
     * Returns a stream that delivers the constraints that are declared directly in this service, not including the ones
     * that are declared in the base services.
     */
    public Stream<Constraint> declaredConstraints() {
        return constraints.stream();
    }

    /**
     * Adds a new constraint to this service.
     */
    public void addConstraint(Constraint newConstraint) {
        constraints.add(newConstraint);
    }

    /**
     * Adds a list of new constraints to this service.
     */
    public void addConstraints(List<Constraint> newConstraints) {
        constraints.addAll(newConstraints);
    }

    public Model getModel() {
        return module.getModel();
    }
}

