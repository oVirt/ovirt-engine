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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
import java.util.Optional;

public class StructType extends Type {
    // Reference to the base of this type:
    private Type base;

    // The list of attributes declared by this type directly:
    private List<Attribute> attributes = new ArrayList<>();

    // The list of links declared by this type directly:
    private List<Link> links = new ArrayList<>();

    /**
     * Returns the base of this type, or {@code null} if this type doesn't have a base type.
     */
    public Type getBase() {
        return base;
    }

    /**
     * Sets the base of this type.
     */
    public void setBase(Type newType) {
        base = newType;
    }

    /**
     * Check if this type is an extension of the given type.
     */
    public boolean isExtension(Type type) {
        if (type == this) {
            return true;
        }
        if (base != null && base instanceof StructType) {
            return ((StructType) base).isExtension(type);
        }
        return false;
    }

    /**
     * Returns all the attributes of this type, including the ones declared in base types. The returned list is a copy
     * of the one used internally, so it is safe to modify it. If you aren't going to modify the list consider using the
     * {@link #attributes()} method instead.
     */
    public List<Attribute> getAttributes() {
        List<Attribute> result = new ArrayList<>(attributes);
        if (base != null && base instanceof StructType) {
            result.addAll(((StructType) base).getAttributes());
        }
        return result;
    }

    /**
     * Returns a stream that delivers all the attributes of this type, including the ones declared in base types.
     */
    public Stream<Attribute> attributes() {
        Stream<Attribute> result = declaredAttributes();
        if (base != null && base instanceof StructType) {
            result = concat(result, ((StructType) base).attributes());
        }
        return result;
    }

    /**
     * Returns the attribute that has the given name, or an empty {@link Optional} if no such attribute exists.
     */
    public Optional<Attribute> getAttribute(Name name) {
        return getAttributes().stream().filter(named(name)).findFirst();
    }

    /**
     * Returns the list of attributes that are declared directly in this type, not including the ones that are declared
     * in the base types. The returned list is a copy of the one used internally, so it is safe to modify it. If you
     * aren't going to modify the list consider using the {@link #declaredAttributes()} method instead.
     */
    public List<Attribute> getDeclaredAttributes() {
        return new CopyOnWriteArrayList<>(attributes);
    }

    /**
     * Returns a stream that delivers the attributes that are declared directly in this type, not including the ones
     * that are declared in the base types.
     */
    public Stream<Attribute> declaredAttributes() {
        return attributes.stream();
    }

    /**
     * Adds a new attribute to this type.
     */
    public void addAttribute(Attribute newAttribute) {
        attributes.add(newAttribute);
    }

    /**
     * Adds a list of new attributes to this type.
     */
    public void addAttributes(List<Attribute> newAttributes) {
        attributes.addAll(attributes);
    }

    /**
     * Returns all the links of this type, including the ones declared in base types. The returned list is a copy of the
     * one used internally, so it is safe to modify it. If you aren't going to modify the list consider using the
     * {@link #links()} method instead.
     */
    public List<Link> getLinks() {
        List<Link> result = new ArrayList<>(links);
        if (base != null && base instanceof StructType) {
            result.addAll(((StructType) base).getLinks());
        }
        return result;
    }

    /**
     * Returns a stream that delivers all the links of this type, including the ones declared in base types.
     */
    public Stream<Link> links() {
        Stream<Link> result = declaredLinks();
        if (base != null && base instanceof StructType) {
            result = concat(((StructType) base).links(), result);
        }
        return result;
    }

    /**
     * Returns the link that has the given name, or an empty {@link Optional} if no such link exists.
     */
    public Optional<Link> getLink(Name name) {
        return getLinks().stream().filter(named(name)).findFirst();
    }

    /**
     * Returns the list of links that are declared directly in this type, not including the ones that are declared
     * in the base types. The returned list is a copy of the one used internally, so it is safe to modify it. If you
     * aren't going to modify the list consider using the {@link #declaredLinks()} method instead.
     */
    public List<Link> getDeclaredLinks() {
        return new CopyOnWriteArrayList<>(links);
    }

    /**
     * Returns a stream that delivers the links that are declared directly in this type, not including the ones that are
     * declared in the base types.
     */
    public Stream<Link> declaredLinks() {
        return links.stream();
    }

    /**
     * Adds a new link to this type.
     */
    public void addLink(Link newLink) {
        links.add(newLink);
    }

    /**
     * Adds a list of new links to this type.
     */
    public void addLinks(List<Link> newLinks) {
        links.addAll(newLinks);
    }
}
