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

/**
 * This class represents a concept of the metamodel.
 */
public abstract class Concept {
    private Name name;
    private String doc;
    private String source;

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String newDoc) {
        doc = newDoc;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String newSource) {
        source = newSource;
    }

    @Override
    public String toString() {
        return name != null? name.toString(): "";
    }
}

