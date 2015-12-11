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

import java.util.Objects;
import java.util.function.Predicate;

/**
 * This class represents a concept of the metamodel.
 */
public abstract class Concept implements Comparable<Concept> {
    private Name name;
    private String doc;
    private String source;

    /**
     * Returns the name of this concept.
     */
    public Name getName() {
        return name;
    }

    /**
     * Sets the name of this concept.
     */
    public void setName(Name name) {
        this.name = name;
    }

    /**
     * Returns the documentation of this concet, which may be {@code null} if the concept isn't documented.
     */
    public String getDoc() {
        return doc;
    }

    /**
     * Sets the documentation of this concept.
     */
    public void setDoc(String newDoc) {
        doc = newDoc;
    }

    /**
     * Returns the source code of this concept. Usually the source code will be the Java text that represents the
     * concept, but it may be {@code null} if the concept has been created manually and not as the result of
     * associated, for example if it has been created manually.
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the source code of this concept.
     */
    public void setSource(String newSource) {
        source = newSource;
    }

    @Override
    public String toString() {
        return name != null? name.toString(): "";
    }

    /**
     * Checks if the given object is equal to this concept. Only the name is taken into account in this comparison, the
     * documentation and the source are ignored.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof Concept) {
            Concept that = (Concept) object;
            return Objects.equals(this.name, that.name);
        }
        return false;
    }

    /**
     * Computes a hash code for this concept. Only the name is taken into account for this computation, the
     * documentation and the source are ignored.
     */
    @Override
    public int hashCode() {
        if (name != null) {
            return name.hashCode();
        }
        return super.hashCode();
    }

    /**
     * Compares this concept to another concept. Only the name is taken into account for this comparison, and the
     * result is intended only for sorting concepts by name. In particular if the result is 0 it only means that
     * both concepts have the same name, not that they are equal.
     */
    @Override
    public int compareTo(Concept that) {
        return this.getName().compareTo(that.getName());
    }

    /**
     * This method creates a predicate useful for filtering streams of concepts and keeping only the ones that have a
     * given name. For example, if you need to find the a parameter of a method that has a given name you can do the
     * following:
     *
     * <pre>
     * Name name = ...;
     * Optional<Parameter> parameter = method.getParameters().stream()
     *     .filter(named(name))
     *     .findFirst();
     * </pre>
     *
     * @param name the name that the predicate will accept
     * @return a predicate that accepts concepts with the given name
     */
    public static Predicate<Concept> named(Name name) {
        return x -> Objects.equals(x.getName(), name);
    }
}

