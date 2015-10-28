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

public class ArrayExpression extends Expression {
    private Expression array;
    private Expression index;

    public void setArray(Expression newArray) {
        array = newArray;
    }

    public Expression getArray() {
        return array;
    }

    public void setIndex(Expression newIndex) {
        index = newIndex;
    }

    public Expression getIndex() {
        return index;
    }

    @Override
    public String toString(boolean protect) {
        StringBuilder buffer = new StringBuilder();
        if (protect) {
            buffer.append("(");
        }
        if (array != null) {
            buffer.append(array.toString(true));
        }
        buffer.append("[");
        if (index != null) {
            buffer.append(index.toString(false));
        }
        buffer.append("]");
        if (protect) {
            buffer.append(")");
        }
        return buffer.toString();
    }
}
