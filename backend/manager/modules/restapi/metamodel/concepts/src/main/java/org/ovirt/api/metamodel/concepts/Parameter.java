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

public class Parameter extends Concept {
    // The method that declared this parameter:
    private Method declaringMethod;

    // The direction of this parameter:
    private boolean in = false;
    private boolean out = false;

    // The type of this parameter:
    private Type type;

    // The default value of this parameter:
    private Expression defaultValue;

    /**
     * Returns the method where this parameter is declared.
     */
    public Method getDeclaringMethod() {
        return declaringMethod;
    }

    /**
     * Sets the method that declared this parameter.
     */
    public void setDeclaringMethod(Method newDeclaringMethod) {
        declaringMethod = newDeclaringMethod;
    }

    public boolean isIn() {
        return in;
    }

    public void setIn(boolean in) {
        this.in = in;
    }

    public boolean isOut() {
        return out;
    }

    public void setOut(boolean out) {
        this.out = out;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Expression getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Expression value) {
        this.defaultValue = value;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        if (declaringMethod != null) {
            buffer.append(declaringMethod);
            buffer.append(":");
        }
        buffer.append(getName());
        return buffer.toString();
    }
}

