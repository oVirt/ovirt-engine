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

import static java.util.stream.Collectors.toList;

import java.math.BigInteger;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.ArithmeticAtomArrayContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.ArithmeticAtomCallContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.ArithmeticAtomIdContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.ArithmeticAtomLiteralContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.ArithmeticAtomParenthesizedContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.ArithmeticBinaryFactorContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.ArithmeticBinaryTermContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.ArithmeticExpressionContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.ArithmeticPrimaryAtomContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.ArithmeticPrimarySignContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.ArithmeticUnaryFactorContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.ArithmeticUnaryTermContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.BooleanAtomContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.BooleanBinaryFactorContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.BooleanBinaryTermContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.BooleanExpressionContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.BooleanPrimaryAtomContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.BooleanPrimaryNegationContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.BooleanUnaryFactorContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.BooleanUnaryTermContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.CallContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.CallParametersContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.ExpressionContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.IdentifierContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.IntegerContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.LiteralBooleanContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.LiteralNumericContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.RelationalExpressionContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.StatementAssertContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.StatementCallContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.StatementReturnContext;
import org.ovirt.api.metamodel.analyzer.ExpressionParser.StatementsContext;
import org.ovirt.api.metamodel.concepts.NameParser;
import org.ovirt.api.metamodel.concepts.ArrayExpression;
import org.ovirt.api.metamodel.concepts.BinaryExpression;
import org.ovirt.api.metamodel.concepts.Expression;
import org.ovirt.api.metamodel.concepts.LiteralExpression;
import org.ovirt.api.metamodel.concepts.Operator;
import org.ovirt.api.metamodel.concepts.UnaryExpression;

/**
 * This class is responsible for analyzing the expressions used in the model language for default values and for
 * constraints.
 */
public class ExpressionAnalyzer extends ExpressionBaseListener {
    /**
     * Analyzes the given source code and returns the contained expressions. The source may contain multiple
     * expressions, each terminated with a semicolon and optionally preceded by the {@code return} or {@code assert}
     * reserved words.
     *
     * @param source the source code of the constraint
     * @throws IllegalArgumentException if something fails while analyzing the constraint
     */
    public List<Expression> analyzeExpressions(String source) {
        ANTLRInputStream input = new ANTLRInputStream(source);
        ExpressionLexer lexer = new ExpressionLexer(input);
        CommonTokenStream stream = new CommonTokenStream(lexer);
        ExpressionParser parser = new ExpressionParser(stream);
        ParseTreeWalker walker = new ParseTreeWalker();
        StatementsContext statements = parser.statements();
        walker.walk(this, statements);
        return statements.result;
    }

    /**
     * Analyzes the given source code and returns the contained expression. Only one expression is expected, terminated
     * with a semicolon and preceded by the {@code return} or {@code assert} reserved words.
     *
     * @param source the source code of the constraint
     * @throws IllegalArgumentException if something fails while analyzing the constraint
     */
    public Expression analyzeExpression(String source) {
        List<Expression> expressions = analyzeExpressions(source);
        if (expressions.isEmpty()) {
            throw new IllegalArgumentException(
                "Exactly one expresison was expected inside source \"" + source + "\" but none was found"
            );
        }
        if (expressions.size() > 1) {
            throw new IllegalArgumentException(
                "Exactly one expresison was expected inside source \"" + source + "\" " +
                "but " + expressions.size() + " were found"
            );
        }
        return expressions.get(0);
    }

    @Override
    public void exitStatements(StatementsContext context) {
        context.result = context.statement().stream()
            .map(x -> x.result)
            .filter(x -> x != null)
            .collect(toList());
    }

    @Override
    public void exitStatementAssert(StatementAssertContext context) {
        context.result = context.booleanExpression().result;
    }

    @Override
    public void exitStatementReturn(StatementReturnContext context) {
        context.result = context.expression().result;
    }

    @Override
    public void exitStatementCall(StatementCallContext context) {
        context.result = context.expression().result;
    }

    @Override
    public void exitExpression(ExpressionContext context) {
        if (context.arithmeticExpression() != null) {
            context.result = context.arithmeticExpression().result;
        }
        if (context.booleanExpression() != null) {
            context.result = context.booleanExpression().result;
        }
    }

    @Override
    public void exitBooleanExpression(BooleanExpressionContext context) {
        context.result = context.booleanTerm().result;
    }

    @Override
    public void exitBooleanUnaryTerm(BooleanUnaryTermContext context) {
        context.result = context.booleanFactor().result;
    }

    @Override
    public void exitBooleanBinaryTerm(BooleanBinaryTermContext context) {
        BinaryExpression result = new BinaryExpression();
        result.setOperator(Operator.OR);
        result.setLeft(context.left.result);
        result.setRight(context.right.result);
        context.result = result;
    }

    @Override
    public void exitBooleanUnaryFactor(BooleanUnaryFactorContext context) {
        context.result = context.booleanPrimary().result;
    }

    @Override
    public void exitBooleanBinaryFactor(BooleanBinaryFactorContext context) {
        BinaryExpression result = new BinaryExpression();
        result.setOperator(Operator.AND);
        result.setLeft(context.left.result);
        result.setRight(context.right.result);
        context.result = result;
    }

    @Override
    public void exitBooleanPrimaryAtom(BooleanPrimaryAtomContext context) {
        context.result = context.booleanAtom().result;
    }

    @Override
    public void exitBooleanPrimaryNegation(BooleanPrimaryNegationContext context) {
        UnaryExpression result = new UnaryExpression();
        result.setOperator(Operator.NOT);
        result.setOperand(context.booleanPrimary().result);
        context.result = result;
    }

    @Override
    public void exitBooleanAtom(BooleanAtomContext context) {
        if (context.arithmeticAtom() != null) {
            context.result = context.arithmeticAtom().result;
        }
        if (context.relationalExpression() != null) {
            context.result = context.relationalExpression().result;
        }
    }

    @Override
    public void exitRelationalExpression(RelationalExpressionContext context) {
        BinaryExpression result = new BinaryExpression();
        Operator operator = null;
        switch (context.operator.getText()) {
        case "==":
            operator = Operator.EQUAL;
            break;
        case "!=":
            operator = Operator.NOT_EQUAL;
            break;
        case ">":
            operator = Operator.GREATER_THAN;
            break;
        case ">=":
            operator = Operator.GREATER_THAN_OR_EQUAL;
            break;
        case "<":
            operator = Operator.LESS_THAN;
            break;
        case "<=":
            operator = Operator.LESS_THAN_OR_EQUAL;
            break;
        default:
            throw new IllegalArgumentException("The string \"" + operator + "\" isn't a valid relational operator");
        }
        result.setOperator(operator);
        result.setLeft(context.left.result);
        result.setRight(context.right.result);
        context.result = result;
    }

    @Override
    public void exitArithmeticExpression(ArithmeticExpressionContext context) {
        context.result = context.arithmethicTerm().result;
    }

    @Override
    public void exitArithmeticUnaryTerm(ArithmeticUnaryTermContext context) {
        context.result = context.arithmethicFactor().result;
    }

    @Override
    public void exitArithmeticBinaryTerm(ArithmeticBinaryTermContext context) {
        BinaryExpression result = new BinaryExpression();
        Operator operator = null;
        switch (context.operator.getText()) {
        case "+":
            operator = Operator.ADD;
            break;
        case "-":
            operator = Operator.SUBTRACT;
            break;
        default:
            throw new IllegalArgumentException("The string \"" + operator + "\" isn't valid additive operator");
        }
        result.setOperator(operator);
        result.setLeft(context.left.result);
        result.setRight(context.right.result);
        context.result = result;
    }

    @Override
    public void exitArithmeticUnaryFactor(ArithmeticUnaryFactorContext context) {
        context.result = context.arithmeticPrimary().result;
    }

    @Override
    public void exitArithmeticBinaryFactor(ArithmeticBinaryFactorContext context) {
        BinaryExpression result = new BinaryExpression();
        Operator operator = null;
        switch (context.operator.getText()) {
        case "*":
            operator = Operator.MULTIPLY;
            break;
        case "/":
            operator = Operator.DIVIDE;
            break;
        case "%":
            operator = Operator.REMAINDER;
            break;
        default:
            throw new IllegalArgumentException("The string \"" + operator + "\" isn't valid multiplicative operator");
        }
        result.setOperator(operator);
        result.setLeft(context.left.result);
        result.setRight(context.right.result);
        context.result = result;
    }

    @Override
    public void exitArithmeticPrimaryAtom(ArithmeticPrimaryAtomContext context) {
        context.result = context.arithmeticAtom().result;
    }

    @Override
    public void exitArithmeticPrimarySign(ArithmeticPrimarySignContext context) {
        if ("-".equals(context.sign.getText())) {
            UnaryExpression result = new UnaryExpression();
            result.setOperator(Operator.SUBTRACT);
            result.setOperand(context.arithmeticPrimary().result);
            context.result = result;
        }
        else {
            context.result = context.arithmeticPrimary().result;
        }
    }

    @Override
    public void exitArithmeticAtomLiteral(ArithmeticAtomLiteralContext context) {
        LiteralExpression result = new LiteralExpression();
        result.setValue(context.literal().result);
        context.result = result;
    }

    @Override
    public void exitArithmeticAtomId(ArithmeticAtomIdContext context) {
        FieldExpression result = new FieldExpression();
        result.setField(context.identifier().name);
        if (context.arithmeticAtom() != null) {
            result.setTarget(context.arithmeticAtom().result);
        }
        context.result = result;
    }

    @Override
    public void exitArithmeticAtomCall(ArithmeticAtomCallContext context) {
        MethodExpression result = (MethodExpression) context.call().result;
        if (context.arithmeticAtom() != null) {
            result.setTarget(context.arithmeticAtom().result);
        }
        context.result = result;
    }

    @Override
    public void exitArithmeticAtomArray(ArithmeticAtomArrayContext context) {
        ArrayExpression result = new ArrayExpression();
        result.setArray(context.arithmeticAtom().result);
        result.setIndex(context.arithmeticExpression().result);
        context.result = result;
    }

    @Override
    public void exitArithmeticAtomParenthesized(ArithmeticAtomParenthesizedContext context) {
        context.result = context.expression().result;
    }

    @Override
    public void exitLiteralBoolean(LiteralBooleanContext context) {
        Boolean result = null;
        switch (context.getText()) {
        case "false":
            result = Boolean.FALSE;
            break;
        case "true":
            result = Boolean.TRUE;
            break;
        default:
            throw new IllegalArgumentException(
                "The string \"" + context.getText() + "\" isn't a valid boolean literal"
            );
        }
        context.result = result;
    }

    @Override
    public void exitLiteralNumeric(LiteralNumericContext context) {
        context.result = context.integer().value;
    }

    @Override
    public void exitCall(CallContext context) {
        MethodExpression result = new MethodExpression();
        result.setMethod(context.identifier().name);
        if (context.callParameters() != null) {
            result.addParameters(context.callParameters().result);
        }
        context.result = result;
    }

    @Override
    public void exitCallParameters(CallParametersContext context) {
        context.result = context.expression().stream()
            .map(x -> x.result)
            .filter(x -> x != null)
            .collect(toList());
    }

    @Override
    public void exitIdentifier(IdentifierContext context) {
        context.name = NameParser.parseUsingCase(context.IDENTIFIER().getText());
    }

    @Override
    public void exitInteger(IntegerContext context) {
        context.value = new BigInteger(context.INTEGER().getText());
    }
}
