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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.List;

import org.junit.Test;
import org.ovirt.api.metamodel.concepts.NameParser;
import org.ovirt.api.metamodel.concepts.ArrayExpression;
import org.ovirt.api.metamodel.concepts.BinaryExpression;
import org.ovirt.api.metamodel.concepts.Expression;
import org.ovirt.api.metamodel.concepts.LiteralExpression;
import org.ovirt.api.metamodel.concepts.Operator;
import org.ovirt.api.metamodel.concepts.UnaryExpression;

/**
 * This test check basic capabilities of the expression analyzer.
 */
public class ExpressionAnalyzerTest {
    @Test
    public void testReturnNull() {
        Expression result = assertOne("return null;", "null");
        assertLiteral(result, null);
    }

    @Test
    public void testReturnFalse() {
        Expression result = assertOne("return false;", "false");
        assertLiteral(result, Boolean.FALSE);
    }

    @Test
    public void testReturnTrue() {
        Expression result = assertOne("return true;", "true");
        assertLiteral(result, Boolean.TRUE);
    }

    @Test
    public void testReturnZero() {
        Expression result = assertOne("return 0;", "0");
        assertLiteral(result, BigInteger.ZERO);
    }

    @Test
    public void testAssertFalse() {
        Expression result = assertOne("assert false;", "false");
        assertLiteral(result, Boolean.FALSE);
    }

    @Test
    public void testAssertTrue() {
        Expression result = assertOne("assert true;", "true");
        assertLiteral(result, Boolean.TRUE);
    }

    @Test
    public void testSimpleFieldAccess() {
        Expression result = assertOne("return my;", "my");
        assertField(result, "my");
    }

    @Test
    public void testFieldAccessChain2() {
        Expression result = assertOne("return a.b;", "a.b");
        FieldExpression b = assertField(result, "b");
        assertField(b.getTarget(), "a");
    }

    @Test
    public void testFieldAccessChain3() {
        Expression result = assertOne("return a.b.c;", "(a.b).c");
        FieldExpression c = assertField(result, "c");
        FieldExpression b = assertField(c.getTarget(), "b");
        assertField(b.getTarget(), "a");
    }

    @Test
    public void testFieldAccessChain4() {
        Expression result = assertOne("return a.b.c.d;", "((a.b).c).d");
        FieldExpression d = assertField(result, "d");
        FieldExpression c = assertField(d.getTarget(), "c");
        FieldExpression b = assertField(c.getTarget(), "b");
        assertField(b.getTarget(), "a");
    }

    @Test
    public void testSimpleMethodCall() {
        Expression result = assertOne("my();", "my()");
        assertCall(result, "my", 0);
    }

    @Test
    public void testCallChain2() {
        Expression result = assertOne("return a().b();", "(a()).b()");
        MethodExpression b = assertCall(result, "b", 0);
        assertCall(b.getTarget(), "a", 0);
    }

    @Test
    public void testCallChain3() {
        Expression result = assertOne("return a().b().c();", "((a()).b()).c()");
        MethodExpression c = assertCall(result, "c", 0);
        MethodExpression b = assertCall(c.getTarget(), "b", 0);
        assertCall(b.getTarget(), "a", 0);
    }

    @Test
    public void testCallChain4() {
        Expression result = assertOne("return a().b().c().d();", "(((a()).b()).c()).d()");
        MethodExpression d = assertCall(result, "d", 0);
        MethodExpression c = assertCall(d.getTarget(), "c", 0);
        MethodExpression b = assertCall(c.getTarget(), "b", 0);
        assertCall(b.getTarget(), "a", 0);
    }

    @Test
    public void testParenthesisRemoved() {
        Expression result = assertOne("return (0);", "0");
        assertLiteral(result, BigInteger.ZERO);
    }

    @Test
    public void testMultipleParenthesisRemoved() {
        Expression result = assertOne("return (((((0)))));", "0");
        assertLiteral(result, BigInteger.ZERO);
    }

    @Test
    public void testSimpleArrayAccess() {
        Expression result = assertOne("return my[0];", "my[0]");
        ArrayExpression my = assertArray(result);
        assertField(my.getArray(), "my");
        assertLiteral(my.getIndex(), BigInteger.ZERO);
    }

    @Test
    public void testPositiveSignRemoved() {
        Expression result = assertOne("return +1;", "1");
        assertLiteral(result, BigInteger.ONE);
    }

    @Test
    public void testSignChange() {
        Expression result = assertOne("return -x;", "-x");
        UnaryExpression x = assertUnary(result, Operator.SUBTRACT);
        assertField(x.getOperand(), "x");
    }

    @Test
    public void testSignChangeWithSpace() {
        Expression result = assertOne("return - x;", "-x");
        UnaryExpression x = assertUnary(result, Operator.SUBTRACT);
        assertField(x.getOperand(), "x");
    }

    @Test
    public void testAdd2() {
        Expression result = assertOne("return x + y;", "x + y");
        BinaryExpression xy = assertBinary(result, Operator.ADD);
        assertField(xy.getLeft(), "x");
        assertField(xy.getRight(), "y");
    }

    @Test
    public void testAdd2WithoutSpaces() {
        Expression result = assertOne("return x+y;", "x + y");
        BinaryExpression xy = assertBinary(result, Operator.ADD);
        assertField(xy.getLeft(), "x");
        assertField(xy.getRight(), "y");
    }

    @Test
    public void testAdd3() {
        Expression result = assertOne("return x + y + z;", "(x + y) + z");
        BinaryExpression xyz = assertBinary(result, Operator.ADD);
        BinaryExpression xy = assertBinary(xyz.getLeft(), Operator.ADD);
        assertField(xy.getLeft(), "x");
        assertField(xy.getRight(), "y");
        assertField(xyz.getRight(), "z");
    }

    @Test
    public void testChangeAddAssociativity() {
        Expression result = assertOne("return x + (y + z);", "x + (y + z)");
        BinaryExpression xyz = assertBinary(result, Operator.ADD);
        assertField(xyz.getLeft(), "x");
        BinaryExpression yz = assertBinary(xyz.getRight(), Operator.ADD);
        assertField(yz.getLeft(), "y");
        assertField(yz.getRight(), "z");
    }

    @Test
    public void testMultiplyBeforeAdd() {
        Expression result = assertOne("return x + y * z;", "x + (y * z)");
        BinaryExpression xyz = assertBinary(result, Operator.ADD);
        assertField(xyz.getLeft(), "x");
        BinaryExpression yz = assertBinary(xyz.getRight(), Operator.MULTIPLY);
        assertField(yz.getLeft(), "y");
        assertField(yz.getRight(), "z");
    }

    @Test
    public void testMultiplyBeforeSubtract() {
        Expression result = assertOne("return x - y * z;", "x - (y * z)");
        BinaryExpression xyz = assertBinary(result, Operator.SUBTRACT);
        assertField(xyz.getLeft(), "x");
        BinaryExpression yz = assertBinary(xyz.getRight(), Operator.MULTIPLY);
        assertField(yz.getLeft(), "y");
        assertField(yz.getRight(), "z");
    }

    @Test
    public void testDivideBeforeAdd() {
        Expression result = assertOne("return x + y / z;", "x + (y / z)");
        BinaryExpression xyz = assertBinary(result, Operator.ADD);
        assertField(xyz.getLeft(), "x");
        BinaryExpression yz = assertBinary(xyz.getRight(), Operator.DIVIDE);
        assertField(yz.getLeft(), "y");
        assertField(yz.getRight(), "z");
    }

    @Test
    public void testDivideBeforeSubtract() {
        Expression result = assertOne("return x - y / z;", "x - (y / z)");
        BinaryExpression xyz = assertBinary(result, Operator.SUBTRACT);
        assertField(xyz.getLeft(), "x");
        BinaryExpression yz = assertBinary(xyz.getRight(), Operator.DIVIDE);
        assertField(yz.getLeft(), "y");
        assertField(yz.getRight(), "z");
    }

    @Test
    public void testChangePrecedence() {
        Expression result = assertOne("return (x + y) * z;", "(x + y) * z");
        BinaryExpression xyz = assertBinary(result, Operator.MULTIPLY);
        BinaryExpression xy = assertBinary(xyz.getLeft(), Operator.ADD);
        assertField(xy.getLeft(), "x");
        assertField(xy.getRight(), "y");
        assertField(xyz.getRight(), "z");
    }

    @Test
    public void testSimpleSubtract() {
        Expression result = assertOne("return x - y;", "x - y");
        BinaryExpression xy = assertBinary(result, Operator.SUBTRACT);
        assertField(xy.getLeft(), "x");
        assertField(xy.getRight(), "y");
    }

    @Test
    public void testSimpleMultiply() {
        Expression result = assertOne("return x * y;", "x * y");
        BinaryExpression xy = assertBinary(result, Operator.MULTIPLY);
        assertField(xy.getLeft(), "x");
        assertField(xy.getRight(), "y");
    }

    @Test
    public void testSimpleDivide() {
        Expression result = assertOne("return x / y;", "x / y");
        BinaryExpression xy = assertBinary(result, Operator.DIVIDE);
        assertField(xy.getLeft(), "x");
        assertField(xy.getRight(), "y");
    }

    @Test
    public void testSimpleRemainder() {
        Expression result = assertOne("return x % y;", "x % y");
        BinaryExpression operation = assertBinary(result, Operator.REMAINDER);
        assertField(operation.getLeft(), "x");
        assertField(operation.getRight(), "y");
    }

    @Test
    public void testSimpleNot() {
        Expression result = assertOne("return !x;", "not x");
        UnaryExpression x = assertUnary(result, Operator.NOT);
        assertField(x.getOperand(), "x");
    }

    @Test
    public void testSimpleAnd() {
        Expression result = assertOne("return x && y;", "x and y");
        BinaryExpression xy = assertBinary(result, Operator.AND);
        assertField(xy.getLeft(), "x");
        assertField(xy.getRight(), "y");
    }

    @Test
    public void testSimpleOr() {
        Expression result = assertOne("return x || y;", "x or y");
        BinaryExpression xy = assertBinary(result, Operator.OR);
        assertField(xy.getLeft(), "x");
        assertField(xy.getRight(), "y");
    }

    @Test
    public void testSimpleLineComment() {
        Expression result = assertOne("return x + // hello\n y;", "x + y");
        BinaryExpression xy = assertBinary(result, Operator.ADD);
        assertField(xy.getLeft(), "x");
        assertField(xy.getRight(), "y");
    }

    @Test
    public void testSimpleBlockComment() {
        Expression result = assertOne("return x + /* hello */ y;", "x + y");
        BinaryExpression xy = assertBinary(result, Operator.ADD);
        assertField(xy.getLeft(), "x");
        assertField(xy.getRight(), "y");
    }

    @Test
    public void testSimpleBlockCommentWithoutSpaces() {
        Expression result = assertOne("return x +/*hello*/y;", "x + y");
        BinaryExpression xy = assertBinary(result, Operator.ADD);
        assertField(xy.getLeft(), "x");
        assertField(xy.getRight(), "y");
    }

    @Test
    public void testMutipleLinesBlockComment() {
        Expression result = assertOne("return x + /* first\nsecond\nthird\n */ y;", "x + y");
        BinaryExpression xy = assertBinary(result, Operator.ADD);
        assertField(xy.getLeft(), "x");
        assertField(xy.getRight(), "y");
    }

    @Test
    public void testMultiple() {
        List<Expression> results = assertMultiple(
            "assert false; assert foo; bar(); return null;",
            "false",
            "foo",
            "bar()",
            "null"
        );
        assertLiteral(results.get(0), Boolean.FALSE);
        assertField(results.get(1), "foo");
        assertCall(results.get(2), "bar", 0);
        assertLiteral(results.get(3), null);
    }

    /**
     * Analyzes a expression and checks that converting it to a string gives the expected value.
     *
     * @param source the source code of the expression
     * @param expected the expected result of converting the expression to string
     * @return the expression object
     */
    private Expression assertOne(String source, String expected) {
        ExpressionAnalyzer analyzer = new ExpressionAnalyzer();
        Expression expression = analyzer.analyzeExpression(source);
        assertEquals(expected, expression.toString());
        return expression;
    }

    /**
     * Analyzes multiple expressions and checks that converting them to strings gives the expected values.
     *
     * @param source the source code of the expression
     * @param expected the expected results of converting the expressions to strings
     * @return the list of expression objects
     */
    private List<Expression> assertMultiple(String source, String... expected) {
        ExpressionAnalyzer analyzer = new ExpressionAnalyzer();
        List<Expression> expressions = analyzer.analyzeExpressions(source);
        assertEquals(expected.length, expressions.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], expressions.get(i).toString());
        }
        return expressions;
    }

    /**
     * Checks that the given expression is a literal and that it has the given value.
     *
     * @param expression the expression to check
     * @param value the expected value
     * @return the checked expression
     */
    private LiteralExpression assertLiteral(Expression expression, Object value) {
        assertTrue(expression instanceof LiteralExpression);
        LiteralExpression literal = (LiteralExpression) expression;
        assertEquals(value, literal.getValue());
        return literal;
    }

    /**
     * Checks that the given expression is a field access with the given field name.
     *
     * @param expression the expression to check
     * @param name the expected field name
     * @return the checked expression
     */
    private FieldExpression assertField(Expression expression, String name) {
        assertTrue(expression instanceof FieldExpression);
        FieldExpression field = (FieldExpression) expression;
        assertEquals(NameParser.parseUsingCase(name), field.getField());
        return field;
    }

    /**
     * Checks that the given expression is a call with the given method name and number of parameters.
     *
     * @param expression the expression to check
     * @param name the expected method name
     * @param count the expected number of parameters
     * @return the checked expression
     */
    private MethodExpression assertCall(Expression expression, String name, int count) {
        assertTrue(expression instanceof MethodExpression);
        MethodExpression call = (MethodExpression) expression;
        assertEquals(NameParser.parseUsingCase(name), call.getMethod());
        assertEquals(count, call.getParameters().size());
        return call;
    }

    /**
     * Checks that the given expression is an array access.
     *
     * @param expression the expression to check
     * @return the checked expression
     */
    private ArrayExpression assertArray(Expression expression) {
        assertTrue(expression instanceof ArrayExpression);
        ArrayExpression array = (ArrayExpression) expression;
        return array;
    }

    /**
     * Checks that the given expression is an unary operator.
     *
     * @param expression the expression to check
     * @param operator the expected operator
     * @return the checked expression
     */
    private UnaryExpression assertUnary(Expression expression, Operator operator) {
        assertTrue(expression instanceof UnaryExpression);
        UnaryExpression operation = (UnaryExpression) expression;
        assertEquals(operator, operation.getOperator());
        return operation;
    }

    /**
     * Checks that the given expression is a binary operator.
     *
     * @param expression the expression to check
     * @param operator the expected operator
     * @return the checked expression
     */
    private BinaryExpression assertBinary(Expression expression, Operator operator) {
        assertTrue(expression instanceof BinaryExpression);
        BinaryExpression operation = (BinaryExpression) expression;
        assertEquals(operator, operation.getOperator());
        return operation;
    }
}
