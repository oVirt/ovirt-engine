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
import java.util.List;

public class StructType extends Type {
    private Type base;
    private List<Attribute> attributes = new ArrayList<>();
    private List<Link> links = new ArrayList<>();

    public Type getBase() {
        return base;
    }

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
     * Returns all the attributes of this type, including the ones declared in base types. The returned list is a sorted
     * copy of the one used internally, so it is safe to modify it.
     */
    public List<Attribute> getAttributes() {
        List<Attribute> result = new ArrayList<>(attributes);
        if (base != null && base instanceof StructType) {
            result.addAll(((StructType) base).getAttributes());
        }
        result.sort(comparing(Attribute::getName));
        return result;
    }

    /**
     * Returns the list of attributes that are declared directly in this type, not including the ones that are declared
     * in the base types. The returned list is a sorted copy of the one used internally, so it is safe to modify it.
     */
    public List<Attribute> getDeclaredAttributes() {
        return new ArrayList<>(attributes);
    }

    /**
     * Adds a new attribute to this type.
     */
    public void addAttribute(Attribute newAttribute) {
        attributes.add(newAttribute);
        attributes.sort(comparing(Attribute::getName));
    }

    /**
     * Adds a list of new attributes to this type.
     */
    public void addAttributes(List<Attribute> newAttributes) {
        attributes.addAll(attributes);
        attributes.sort(comparing(Attribute::getName));
    }

    /**
     * Returns all the links of this type, including the ones declared in base types. The returned list is a sorted copy
     * of the one used internally, so it is safe to modify it.
     */
    public List<Link> getLinks() {
        List<Link> result = new ArrayList<>(links);
        if (base != null && base instanceof StructType) {
            result.addAll(((StructType) base).getLinks());
        }
        result.sort(comparing(Link::getName));
        return result;
    }

    /**
     * Returns the list of links that are declared directly in this type, not including the ones that are declared
     * in the base types. The returned list is a sorted copy of the one used internally, so it is safe to modify it.
     */
    public List<Link> getDeclaredLinks() {
        return new ArrayList<>(links);
    }
    /**
     * Adds a new link to this type.
     */
    public void addLink(Link newLink) {
        links.add(newLink);
        links.sort(comparing(Link::getName));
    }

    /**
     * Adds a list of new links to this type.
     */
    public void addLinks(List<Link> newLinks) {
        links.addAll(newLinks);
        links.sort(comparing(Link::getName));
    }
}

