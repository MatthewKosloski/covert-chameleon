package me.mtk.covertchameleon;

import java.util.ArrayList;
import java.util.List;

// The Interpreter is the part of the interpreter that, well,
// interprets the source code. It takes in an abstract syntax
// tree (AST) as input and outputs a list of strings representing
// the output of the program.
public class Interpreter implements Expr.Visitor<Object>
{

    /**
     * Interprets the source program by walking, or traversing,
     * the given AST in post-order. 
     * 
     * @param expressions A list of expressions to interpret.
     * @return A corresponding list of values of the provided expressions.
     */
    public List<String> interpret(List<Expr> expressions) throws RuntimeError
    {
        List<String> values = new ArrayList<>();

        for (Expr expr : expressions)
            values.add(stringify(evaluate(expr)));
        
        return values;
    }

    @Override
    public Object visitNumberExpr(Expr.Number expr)
    {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr)
    {
        Token operator = expr.operator;
        Object right = evaluate(expr.right);

        validateNumberOperand(operator, right);

        if (operator.type == TokenType.MINUS)
            return - (double) right;
        else
            return (double) right;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr)
    {
        Token operator = expr.operator;
        Object first = evaluate(expr.first);
        Object second = evaluate(expr.second);

        validateNumberOperands(operator, first, second);

        switch (operator.type)
        {
            case PLUS:
                return (double) first + (double) second;
            case MINUS:
                return (double) first - (double) second;
            case STAR:
                return (double) first * (double) second;
            case SLASH:
                if ((double) second == 0)
                    throw new RuntimeError(operator, "Cannot divide by 0");
                else
                    return (double) first / (double) second;
        }

        return null;
    }

    /*
     * Calls the appropriate visitor method that corresponds
     * to the expression, thereby evaluating the expression.
     * 
     * @param expr An expression.
     * @return The value of the expression.
     */
    private Object evaluate(Expr expr)
    {
        return expr.accept(this);
    }

    private void validateNumberOperand(Token operator, Object right)
    {
        if (right instanceof Double) return;
        throw new RuntimeError(operator, "Unary operator must evaluate to numbers.");
    }

    /*
     * Checks the type of the operands of the binary expression,
     * ensuring that they are numbers.
     * 
     * @param operator The operator of the binary expression.
     * @param first The first operand of the binary expression.
     * @param second The second operand of the binary expression.
     */
    private void validateNumberOperands(Token operator, 
        Object first, Object second)
    {
        if (first instanceof Double && second instanceof Double) return;
        throw new RuntimeError(operator, "Binary operators must evaluate to numbers.");
    }

    /*
     * Converts a value of an expression to a string.
     * @param obj An object representing the evaluated value.
     * @return A string of the value.
     */
    private String stringify(Object obj)
    {
        if (obj instanceof Double)
        {
            String text = obj.toString();
            if (text.endsWith(".0"))
            {
                // Integer, so remove the trailing 0
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return obj.toString();
    }
}