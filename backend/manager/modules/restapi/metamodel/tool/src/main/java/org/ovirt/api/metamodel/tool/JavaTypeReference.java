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

package org.ovirt.api.metamodel.tool;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a reference to a Java type, including all the imports that are necessary to use it. For
 * example, if the type is an array like {@code BigDecimal[]} then the text of the reference will be
 * {@code BigDecimal[]} and the list of imports will contain {@code java.math.BigDecimal}.
 */
public class JavaTypeReference {
    private String text;
    private List<JavaClassName> imports = new ArrayList<>(1);

    public String getText() {
        return text;
    }

    public void setText(String newText) {
        text = newText;
    }

    public List<JavaClassName> getImports() {
        return new ArrayList<>(imports);
    }

    public void setImports(List<JavaClassName> newImports) {
        imports.clear();
        imports.addAll(newImports);
    }

    public void addImport(JavaClassName newImport) {
        imports.add(newImport);
    }

    public void addImport(String packageName, String className) {
        JavaClassName newImport = new JavaClassName();
        newImport.setPackageName(packageName);
        newImport.setSimpleName(className);
        imports.add(newImport);
    }
}

