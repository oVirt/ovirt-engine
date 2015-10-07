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

import org.ovirt.api.metamodel.concepts.Concept;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class contains methods useful for several different kinds of classes that generate Java source code.
 */
public class JavaGenerator {
    // Reference tot the buffer used to generate Java code:
    @Inject private JavaClassBuffer javaBuffer;

    // The directory were the output will be generated:
    protected File outDir;

    /**
     * Set the directory were the output will be generated.
     */
    public void setOutDir(File newOutDir) {
        outDir = newOutDir;
    }

    protected void generateDoc(Concept concept) {
        List<String> lines = new ArrayList<>();
        String doc = concept.getDoc();
        if (doc != null) {
            Collections.addAll(lines, doc.split("\n"));
        }
        if (!lines.isEmpty()) {
            javaBuffer.addDocComment(lines);
        }
    }
}

