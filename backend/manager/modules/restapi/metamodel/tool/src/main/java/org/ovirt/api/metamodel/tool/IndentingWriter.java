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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

public class IndentingWriter extends FilterWriter {
    /**
     * This indicates the number of spaces to use for each indentation level.
     */
    public int size = 2;

    /**
     * This indicates the number of indents that should be added to the next line.
     */
    private int level = 0;

    /**
     * This indicates if the next line should be indented.
     */
    private boolean indent = false;

    public IndentingWriter(Writer out) {
        super(out);
    }

    @Override
    public void write(int c) throws IOException {
        if (c == '\n') {
            indent = true;
        }
        else if (indent) {
            for (int i = 0; i < level; i++) {
                for (int j = 0; j < size; j++) {
                    super.write(' ');
                }
            }
            indent = false;
        }
        super.write(c);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        for (int i = off; i < off + len && i < cbuf.length; i++) {
            write(cbuf[i]);
        }
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        for (int i = off; i < off + len && i < str.length(); i++) {
            write(str.charAt(i));
        }
    }

    public void push() {
        level++;
    }

    public void pop() {
        if (level > 0) {
            level--;
        }
    }
}
