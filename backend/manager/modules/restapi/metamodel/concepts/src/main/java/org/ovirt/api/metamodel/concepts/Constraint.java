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

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A constraint is a list of expressions that should all evaluate to {@code true} in order for the constraint to be
 * satisfied.
 */
public class Constraint extends Concept {
    // Reference to the method where this constraint is declared:
    private Method declaringMethod;

    // Flags indicating if the constraint applies to the input parameters of a method, to the output parameters or to
    // both:
    private boolean in = true;
    private boolean out = false;

    // The type of the constraint:
    private ConstraintKind kind = ConstraintKind.REQUIRED;

    // The list of expressions of this constraint:
    private List<Expression> expressions = new ArrayList<>(1);

    /**
     * Returns the method where this constraint is declared.
     */
    public Method getDeclaringMethod() {
        return declaringMethod;
    }

    /**
     * Sets the method where this constraint is declared.
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

    /**
     * Returns the kind of this constraint.
     */
    public ConstraintKind getKind() {
        return kind;
    }

    /**
     * Sets the type of this constraint.
     */
    public void setKind(ConstraintKind newKind) {
        kind = newKind;
    }

    /**
     * Returns the list of expressions of this constraint. The returned list is a copy of the one used internally, so it
     * is safe to modify it.
     */
    public List<Expression> getExpressions() {
        return new CopyOnWriteArrayList<>(expressions);
    }

    /**
     * Adds a new expression to this constraint.
     */
    public void addExpression(Expression newExpression) {
        expressions.add(newExpression);
    }

    /**
     * Adds a list of new expressions to this constraint.
     */
    public void addExpressions(List<Expression> newExpressions) {
        expressions.addAll(newExpressions);
    }

    @Override
    public String toString() {
        return getName().toString() + expressions.stream().map(Expression::toString).collect(joining(",", "[", "]"));
    }
}

