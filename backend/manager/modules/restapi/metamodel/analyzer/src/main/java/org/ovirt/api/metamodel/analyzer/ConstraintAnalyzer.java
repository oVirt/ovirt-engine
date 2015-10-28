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

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.ovirt.api.metamodel.concepts.ArrayExpression;
import org.ovirt.api.metamodel.concepts.Attribute;
import org.ovirt.api.metamodel.concepts.AttributeExpression;
import org.ovirt.api.metamodel.concepts.BinaryExpression;
import org.ovirt.api.metamodel.concepts.Constraint;
import org.ovirt.api.metamodel.concepts.Expression;
import org.ovirt.api.metamodel.concepts.Link;
import org.ovirt.api.metamodel.concepts.LinkExpression;
import org.ovirt.api.metamodel.concepts.ListType;
import org.ovirt.api.metamodel.concepts.LiteralExpression;
import org.ovirt.api.metamodel.concepts.Method;
import org.ovirt.api.metamodel.concepts.Model;
import org.ovirt.api.metamodel.concepts.Name;
import org.ovirt.api.metamodel.concepts.Operator;
import org.ovirt.api.metamodel.concepts.Parameter;
import org.ovirt.api.metamodel.concepts.ParameterExpression;
import org.ovirt.api.metamodel.concepts.StructType;
import org.ovirt.api.metamodel.concepts.Type;
import org.ovirt.api.metamodel.concepts.UnaryExpression;

/**
 * This class is responsible for analyzing the constraints used in the model language.
 */
public class ConstraintAnalyzer {
    // References to the model and the method where the constraint is declared:
    private Model model;
    private Method method;

    // The constraint that will be populated by this analyzer:
    private Constraint constraint;

    /**
     * Sets the model.
     */
    public void setModel(Model newModel) {
        model = newModel;
    }

    /**
     * Gets the method where the constraint is declared.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Sets the method where the constraint is declared.
     */
    public void setMethod(Method newMethod) {
        method = newMethod;
    }

    /**
     * Gets the constraint that is being populated by this analyzer.
     */
    public Constraint getConstraint(Constraint constraint) {
        return constraint;
    }

    /**
     * Sets the constraint that will be populated by this analyzer.
     */
    public void setConstraint(Constraint newConstraint) {
        constraint = newConstraint;
    }

    /**
     * Analyzes the given source code and populates the constraint.
     *
     * @param source the source code of the constraint
     */
    public void analyzeSource(String source) {
        // First analyze the expressions contained in the source:
        ExpressionAnalyzer expressionAnalyzer = new ExpressionAnalyzer();
        List<Expression> expressions = expressionAnalyzer.analyzeExpressions(source);

        // Transform all the expressions replacing methods and fields with parameters and attributes:
        expressions = expressions.stream().map(this::transform).collect(Collectors.toList());

        // Add the resulting expressions to the constraint:
        constraint.addExpressions(expressions);
    }

    private Expression transformMethod(MethodExpression expression) {
        // Transform the target:
        Expression target = expression.getTarget();
        if (target != null) {
            target = transform(target);
        }

        // Transform the parameters:
        List<Expression> parameters = expression.getParameters();
        if (!parameters.isEmpty()) {
            throw new IllegalArgumentException("The parameters in expression \"" + expression + "\" aren't supported");
        }

        // Get the name of the method:
        Name name = expression.getMethod();

        // Calls with no target object should correspond to method parameters, or to static methods that have been
        // statically imported:
        if (target == null) {
            Parameter parameter = method.getParameter(name);
            if (parameter != null) {
                ParameterExpression replacement = new ParameterExpression();
                replacement.setType(parameter.getType());
                replacement.setParameter(parameter);
                return replacement;
            }
        }

        // Calls with a target object should correspond to attributes of objects used as parameters:
        if (target != null) {
            Type type = target.getType();
            if (type instanceof StructType) {
                StructType struct = (StructType) type;
                Optional<Attribute> attribute = struct.getAttribute(name);
                if (attribute.isPresent()) {
                    AttributeExpression replacement = new AttributeExpression();
                    replacement.setType(attribute.get().getType());
                    replacement.setTarget(target);
                    replacement.setAttribute(attribute.get());
                    return replacement;
                }
                Optional<Link> link = struct.getLink(name);
                if (link.isPresent()) {
                    LinkExpression replacement = new LinkExpression();
                    replacement.setType(link.get().getType());
                    replacement.setTarget(target);
                    replacement.setLink(link.get());
                    return replacement;
                }
            }
        }

        // Not supported:
        throw new IllegalArgumentException("Don't know how to transform method call \"" + expression + "\"");
    }

    private Expression transformBinary(BinaryExpression expression) {
        // Transform the operands:
        Expression left = expression.getLeft();
        if (left != null) {
            left = transform(left);
        }
        Expression right = expression.getRight();
        if (right != null) {
            right = transform(right);
        }

        // Sort-cuts to the model primitive types:
        Type bool = model.getBooleanType();
        Type decimal = model.getDecimalType();
        Type integer = model.getIntegerType();

        // Compute the type of the expression:
        Operator operator = expression.getOperator();
        Type type = null;
        switch (operator) {
        case AND:
        case EQUAL:
        case GREATER_THAN:
        case GREATER_THAN_OR_EQUAL:
        case LESS_THAN:
        case LESS_THAN_OR_EQUAL:
        case NOT:
        case NOT_EQUAL:
        case OR:
            type = bool;
            break;
        case ADD:
        case DIVIDE:
        case MULTIPLY:
        case REMAINDER:
        case SUBTRACT:
            if (Objects.equals(left.getType(), decimal) || Objects.equals(right.getType(), decimal)) {
                type = decimal;
            }
            else {
                type = integer;
            }
            break;
        }

        // Return the result:
        BinaryExpression result = new BinaryExpression();
        result.setType(type);
        result.setOperator(operator);
        result.setLeft(left);
        result.setRight(right);
        return result;
    }

    private Expression transformUnary(UnaryExpression expression) {
        // Transform the operand:
        Expression operand = expression.getOperand();
        if (operand == null) {
            throw new IllegalArgumentException("The operand in expression \"" + expression + "\" is null");
        }
        operand = transform(operand);

        // Compute the type:
        Operator operator = expression.getOperator();
        Type type;
        switch (operator) {
        case NOT:
            type = model.getBooleanType();
            break;
        case SUBTRACT:
            type = operand.getType();
            break;
        default:
            throw new IllegalArgumentException(
                "The operator \"" + operator + "\" in expression \"" + expression + "\" isn't supported"
            );
        }

        // Return the result:
        UnaryExpression result = new UnaryExpression();
        result.setType(type);
        result.setOperator(operator);
        result.setOperand(operand);
        return result;
    }

    private Expression transformArray(ArrayExpression expression) {
        // Transform the array:
        Expression array = expression.getArray();
        if (array == null) {
            throw new IllegalArgumentException("The array in array expression \"" + expression + "\" is null");
        }
        array = transform(array);

        // Transform the index:
        Expression index = expression.getIndex();
        if (index == null) {
            throw new IllegalArgumentException("The index in array expression \"" + expression + "\" is null");
        }
        index = transform(index);

        // Compute the type:
        Type type = null;
        Type arrayType = array.getType();
        if (arrayType instanceof ListType) {
            ListType listType = (ListType) arrayType;
            type = listType.getElementType();
        }

        // Return the result:
        ArrayExpression result = new ArrayExpression();
        result.setType(type);
        result.setArray(array);
        result.setIndex(index);
        return result;
    }

    private Expression transformField(FieldExpression expression) {
        // Transform the target:
        Expression target = expression.getTarget();
        if (target != null) {
            target = transform(target);
        }

        // Get the field:
        Name field = expression.getField();

        // Return the result:
        FieldExpression result = new FieldExpression();
        result.setTarget(target);
        result.setField(field);
        return result;
    }

    private Expression transformLiteral(LiteralExpression expression) {
        // Compute the type:
        Object value = expression.getValue();
        Type type = null;
        if (value != null) {
            if (value instanceof Boolean) {
                type = model.getBooleanType();
            }
            else if (value instanceof BigInteger) {
                type = model.getIntegerType();
            }
        }

        // Return the result:
        LiteralExpression result = new LiteralExpression();
        result.setValue(value);
        result.setType(type);
        return result;
    }

    private Expression transform(Expression expression) {
        if (expression == null) {
            return null;
        }
        if (expression instanceof ArrayExpression) {
            return transformArray((ArrayExpression) expression);
        }
        if (expression instanceof BinaryExpression) {
            return transformBinary((BinaryExpression) expression);
        }
        if (expression instanceof FieldExpression) {
            return transformField((FieldExpression) expression);
        }
        if (expression instanceof LiteralExpression) {
            return transformLiteral((LiteralExpression) expression);
        }
        if (expression instanceof MethodExpression) {
            return transformMethod((MethodExpression) expression);
        }
        if (expression instanceof UnaryExpression) {
            return transformUnary((UnaryExpression) expression);
        }
        throw new IllegalArgumentException(
            "Don't know how to transform expressions of class \"" + expression.getClass().getName() + "\""
        );
    }
}
