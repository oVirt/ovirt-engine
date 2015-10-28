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
 * This class represents an expression that applies an unary operator too an operand.
 */
public class UnaryExpression extends Expression {
    private Operator operator;
    private Expression operand;

    public void setOperator(Operator newOperator) {
        operator = newOperator;
    }

    public Operator getOperator() {
        return operator;
    }

    public Expression getOperand() {
        return operand;
    }

    public void setOperand(Expression newOperand) {
        operand = newOperand;
    }

    @Override
    public String toString(boolean protect) {
        StringBuilder buffer = new StringBuilder();
        if (protect) {
            buffer.append("(");
        }
        if (operator != null) {
            buffer.append(operator);
            if (operator == Operator.NOT) {
                buffer.append(" ");
            }
        }
        if (operand != null) {
            buffer.append(operand.toString(true));
        }
        if (protect) {
            buffer.append(")");
        }
        return buffer.toString();
    }
}
