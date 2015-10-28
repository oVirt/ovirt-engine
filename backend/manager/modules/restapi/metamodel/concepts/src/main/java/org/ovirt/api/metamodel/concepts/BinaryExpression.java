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
 * This class represents an expression that applies a binary operator too a pair of operands.
 */
public class BinaryExpression extends Expression {
    private Operator operator;
    private Expression left;
    private Expression right;

    public void setOperator(Operator newOperator) {
        operator = newOperator;
    }

    public Operator getOperator() {
        return operator;
    }

    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression newLeft) {
        left = newLeft;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression newRight) {
        right = newRight;
    }

    @Override
    public String toString(boolean protect) {
        StringBuilder buffer = new StringBuilder();
        if (protect) {
            buffer.append("(");
        }
        buffer.append(left.toString(true));
        buffer.append(" ");
        buffer.append(operator);
        buffer.append(" ");
        buffer.append(right.toString(true));
        if (protect) {
            buffer.append(")");
        }
        return buffer.toString();
    }
}
