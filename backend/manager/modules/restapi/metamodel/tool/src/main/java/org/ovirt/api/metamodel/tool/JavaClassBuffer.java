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

import org.apache.commons.io.FileUtils;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is a buffer intended to simplify generation of Java source code. It stores the name of the package, the
 * list of imports and the rest of the source separately, so that imports can be added on demand while generating the
 * rest of the source.
 */
@ApplicationScoped
public class JavaClassBuffer {
    // The names of the package and the class:
    private JavaClassName className;

    // The things to be imported, without the "import" prefix and without the ending semicolon:
    private Set<String> imports = new HashSet<>();

    // The lines of the body of the class:
    private List<String> lines = new ArrayList<>();

    // The current indentation level:
    private int level;

    /**
     * Sets the class name.
     */
    public void setClassName(JavaClassName newClassName) {
        className = newClassName;
    }

    public JavaClassName getClassName() {
        return className;
    }

    /**
     * Adds an import for the given class name.
     *
     * @param newClassName the fully qualified class name
     */
    public void addImport(JavaClassName newClassName) {
        addImport(newClassName.getPackageName(), newClassName.getSimpleName());
    }

    /**
     * Adds an import for the given package and class.
     *
     * @param newPackageName the name of the package
     * @param newClassName the simple name of the class, without the package name
     */
    public void addImport(String newPackageName, String newClassName) {
        if (!newPackageName.equals(className.getPackageName())) {
            String newImport = newPackageName + "." + newClassName;
            imports.add(newImport);
        }
    }

    /**
     * Adds an import. The given parameter should be the thing to import, without the {@code import} prefix or the
     * trailing semicolon.
     */
    public void addImport(String newImport) {
        String newImportPackage = newImport.substring(0, newImport.lastIndexOf('.'));
        if (!newImportPackage.equals(className.getPackageName())) {
            imports.add(newImport);
        }
    }

    /**
     * Adds an import for the given class.
     */
    public void addImport(Class newImport) {
        imports.add(newImport.getCanonicalName());
    }

    /**
     * Adds multiple imports.
     *
     * @param newImports the fully qualified class names
     */
    public void addImports(List<JavaClassName> newImports) {
        newImports.forEach(this::addImport);
    }

    /**
     * Adds a line to the body of the class.
     */
    public void addLine(String line) {
        // Check if the line is the end of a block, and reduce the indentation level accordingly:
        if (line.endsWith("}")) {
            if (level > 0) {
                level--;
            }
        }

        // Indent the line and add it to the list:
        StringBuilder buffer = new StringBuilder(level * 4 + line.length());
        for (int i = 0; i < level; i++) {
            buffer.append("    ");
        }
        buffer.append(line);
        line = buffer.toString();
        lines.add(line);

        // Check if the line is the beginning of a block, and increase the indentation level accordingly:
        if (line.endsWith("{")) {
            level++;
        }
    }

    /**
     * Adds an empty line to the body of the class.
     */
    public void addLine() {
        addLine("");
    }

    /**
     * Adds a formatted line to the body of the class. The given {@code args} are formatted using the
     * provided {@code format} using the {@link String#format(String, Object...)} method.
     */
    public void addLine(String format, Object ... args) {
        StringBuilder buffer = new StringBuilder();
        Formatter formatter = new Formatter(buffer);
        formatter.format(format, args);
        String line = buffer.toString();
        addLine(line);
    }

    /**
     * Adds a line comment to the body of the class.
     */
    public void addLineComment(String line) {
        addLine("// " + line);
    }

    /**
     * Adds a Javadoc comment to the body of the class.
     */
    public void addDocComment(List<String> lines) {
        addLine("/**");
        for (String line : lines) {
            addLine(" * %s", line);
        }
        addLine(" */");
    }

    /**
     * Adds a Javadoc comment to the body of the class.
     */
    public void addDocComment(String... lines) {
        addLine("/**");
        for (String line : lines) {
            addLine(" * %s", line);
        }
        addLine(" */");
    }

    /**
     * Generates the complete source code of the class.
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        // License:
        buffer.append("/*\n");
        buffer.append("Copyright (c) 2015 Red Hat, Inc.\n");
        buffer.append("Licensed under the Apache License, Version 2.0 (the \"License\");\n");
        buffer.append("you may not use this file except in compliance with the License.\n");
        buffer.append("You may obtain a copy of the License at\n");
        buffer.append("\n");
        buffer.append("  http://www.apache.org/licenses/LICENSE-2.0\n");
        buffer.append("\n");
        buffer.append("Unless required by applicable law or agreed to in writing, software\n");
        buffer.append("distributed under the License is distributed on an \"AS IS\" BASIS,\n");
        buffer.append("WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n");
        buffer.append("See the License for the specific language governing permissions and\n");
        buffer.append("limitations under the License.\n");
        buffer.append("*/\n");
        buffer.append("\n");

        // Package:
        buffer.append("package ");
        buffer.append(className.getPackageName());
        buffer.append(";\n\n");

        // Imports:
        List<String> importsList = new ArrayList<>(imports);
        Collections.sort(importsList);
        for (String importsItem : importsList) {
            buffer.append("import ");
            buffer.append(importsItem);
            buffer.append(";\n");
        }
        buffer.append("\n");

        // Body:
        for (String line : lines) {
            buffer.append(line);
            buffer.append("\n");
        }

        return buffer.toString();
    }

    /**
     * Creates a {@code .java} source file for this class taking into account the package name, and writes the source
     * code of the class to that file. The required intermediate directories will be created if they don't exist.
     *
     * @param outDir the base directory for the source code
     * @throws IOException if something fails while creating or writing the file
     */
    public void write(File outDir) throws IOException {
        // Create the package directory and all its parent if needed:
        File packageDir = new File(outDir, className.getPackageName().replace('.', File.separatorChar));
        FileUtils.forceMkdir(packageDir);
        if (!packageDir.exists()) {
            if (!packageDir.mkdirs()) {
                throw new IOException("Can't create directory \"" + packageDir.getAbsolutePath() + "\"");
            }
        }

        // Write the class file:
        File classFile = new File(packageDir, className.getSimpleName() + ".java");
        System.out.println("Writing class file \"" + classFile.getAbsolutePath() + "\".");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(classFile), Charset.forName("UTF-8"))) {
            writer.write(toString());
        }
    }

    /**
     * Clears all the state of the buffer so that it can be reused.
     */
    public void clear() {
        className = null;
        imports.clear();;
        lines.clear();
        level = 0;
    }
}
