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

package org.ovirt.api.metamodel.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ovirt.api.metamodel.concepts.Name;
import org.ovirt.api.metamodel.concepts.Expression;

/**
 * This class represents a call to a method.
 */
public class MethodExpression extends Expression {
    private Expression target;
    private Name method;
    private List<Expression> parameters = new ArrayList<>(0);

    public Expression getTarget() {
        return target;
    }

    public void setTarget(Expression newTarget) {
        target = newTarget;
    }

    public void setMethod(Name newMethod) {
        method = newMethod;
    }

    public Name getMethod() {
        return method;
    }

    public List<Expression> getParameters() {
        return new CopyOnWriteArrayList<>(parameters);
    }

    public void addParameter(Expression newParameter) {
        parameters.add(newParameter);
    }

    public void addParameters(List<Expression> newParameters) {
        parameters.addAll(newParameters);
    }

    @Override
    public String toString(boolean protect) {
        StringBuilder buffer = new StringBuilder();
        if (protect) {
            buffer.append("(");
        }
        if (target != null) {
            buffer.append(target.toString(true));
            buffer.append(".");
        }
        if (method != null) {
            buffer.append(method);
        }
        buffer.append("(");
        boolean first = true;
        for (Expression parameter : parameters) {
            if (!first) {
                buffer.append(", ");
            }
            buffer.append(parameter.toString(false));
            first = false;
        }
        buffer.append(")");
        if (protect) {
            buffer.append(")");
        }
        return buffer.toString();
    }
}
