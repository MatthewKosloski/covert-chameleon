package me.mtk.covertchameleon;

import java.util.List;

// The Interpreter is the part of the interpreter that, well,
// interprets the source code. It takes in an abstract syntax
// tree (AST) as input and outputs a list of strings representing
// the output of the program.
public class Interpreter implements Expr.Visitor<Object>
{

    private Scope scope = new Scope(null);

    /**
     * Interprets the source program by walking, or traversing,
     * the given AST in post-order. 
     * 
     * @param expressions A list of expressions to interpret.
     * @return A corresponding list of values of the provided expressions.
     */
    public void interpret(List<Expr> expressions) throws RuntimeError
    {
        for (Expr expr : expressions)
            evaluate(expr);
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        
        Object first = evaluate(expr.first);

        if (expr.operator.type == TokenType.OR)
        {
            if (isTruthy(first)) return first;
        }
        else 
        {
            if (!isTruthy(first)) return first;
        }
        
        return evaluate(expr.second);
    }

    @Override
    public Object visitClauseExpr(Expr.Clause expr) {return null;}

    @Override
    public Object visitCondExpr(Expr.Cond expr) {

        for (Expr.Clause clause : expr.clauses)
        {
            Object condition = evaluate(clause.condition);

            if (isTruthy(condition))
                return evaluate(clause.body);
        }

        if (expr.elseExpr != null)
            return evaluate(expr.elseExpr);

        return null;
    }

    @Override
    public Object visitIfExpr(Expr.IfExpr expr)
    {
        Object condition = evaluate(expr.condition);

        if (isTruthy(condition))
            return evaluate(expr.thenExpr);
        else if(expr.elseExpr != null)
            return evaluate(expr.elseExpr);
        
        return null;
    }

    @Override
    public Object visitLetExpr(Expr.Let expr)
    {
        Scope local = new Scope(scope);

        // cache global scope to be restored later
        Scope global = scope;

        try
        {
            scope = local;

            for (Expr.Binding binding : expr.bindings)
                evaluate(binding);
            
            return evaluate(expr.body);
        }
        finally
        {
            // restore global scope
            scope = global;
        }
    }

    @Override
    public Object visitBodyExpr(Expr.Body body) 
    {
        for (int i = 0; i < body.exprs.size(); i++)
        {
            Object evaluatedExpr = evaluate(body.exprs.get(i));

            if (i == body.exprs.size() - 1)
                // return last expression
                return evaluatedExpr;
        }

        return null;
    }

    @Override
    public Void visitBindingExpr(Expr.Binding expr)
    {
        scope.define(expr.identifier.lexeme, evaluate(expr.value));
        return null;
    }

    @Override
    public Void visitPrintExpr(Expr.Print expr) 
    {
        for (Expr e : expr.body.exprs)
        {
            String value = stringify(evaluate(e));
            if (expr.operator.type == TokenType.PRINTLN)
                System.out.println(value);
            else
                System.out.print(value);
        }
        
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr)
    {
        return scope.get(expr.name);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr)
    {
        Token operator = expr.operator;
        Object first = evaluate(expr.first);
        Object second = evaluate(expr.second);

        switch (operator.type)
        {
            case EOF:
            case FALSE:
            case IDENTIFIER:
            case LBRACKET:
            case LET:
            case LPAREN:
            case NOT:
            case NULL:
            case NUMBER:
            case PRINT:
            case PRINTLN:
            case RBRACKET:
            case RPAREN:
            case TRUE:
            case TRUE_PREDICATE:
            case COND:
            case IF:
            case THEN:
            case ELSE:
            case AND:
            case OR:
            case UNIDENTIFIED:
            break;
        
            // equal? and nequal? operators
            case EQUAL_PREDICATE: 
                return isEqual(first, second);
            case NEQUAL_PREDICATE:
                return !isEqual(first, second);

            // +, -, *, / operators
            // These operators only work on number types, even
            // the + operator. If you want to concatenate two or more
            // strings, use a standard library function.
            case PLUS:
                validateNumberOperands(operator, first, second);
                return (double) first + (double) second;
            case MINUS:
                validateNumberOperands(operator, first, second);
                return (double) first - (double) second;
            case STAR:
                validateNumberOperands(operator, first, second);
                return (double) first * (double) second;
            case SLASH:
            case SLASHSLASH:
                validateNumberOperands(operator, first, second);

                if ((double) second != 0)
                {
                    double quotient = (double) first / (double) second;
                    if (operator.type == TokenType.SLASHSLASH)
                        return Math.floor(quotient);
                    else
                        return quotient;
                }

                throw new RuntimeError(operator, "Cannot divide by zero");
            case PERCENT:
                validateNumberOperands(operator, first, second);
                return (double) first % (double) second;
 
            // >, >=, <, <= operators
            // These operators only work on number and string types.
            // TODO: Compare strings lexicographically
            case GREATER_THAN:
                validateNumberOperands(operator, first, second);
                return (double) first > (double) second;
            case GREATER_THAN_OR_EQUAL_TO:
                validateNumberOperands(operator, first, second);
                return (double) first >= (double) second;
            case LESS_THAN:
                validateNumberOperands(operator, first, second);
                return (double) first < (double) second;
            case LESS_THAN_OR_EQUAL_TO:
                validateNumberOperands(operator, first, second);
                return (double) first <= (double) second;

        }

        return null;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr)
    {
        Token operator = expr.operator;
        Object right = evaluate(expr.right);

        switch (operator.type)
        {
            case MINUS:
                validateNumberOperand(operator, right);
                return - (double) right;
            case NOT:
                // validateBooleanOperand(operator, right);
                return !isTruthy(right);
            case TRUE_PREDICATE:
                return isTruthy(right);
            default:
                validateNumberOperand(operator, right);
                return (double) right;
        }
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr)
    {
        return expr.value;
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

    private void validateNumberOperand(Token operator, Object operand)
    {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, String.format("Expected number " + 
            "after unary operator \"%s\"", operator.lexeme));
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
        throw new RuntimeError(operator, String.format("Binary operator " +
            "\"%s\" only operates on numbers", operator.lexeme));
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
        else if (obj == null)
            return "null";

        return obj.toString();
    }

    /*
     * Indicates if the provided evaluated expression is
     * truthy in accordance with the source language's 
     * semantics.
     *  
     * @param obj The value of an evaluated expression
     * @return True if the evaluated expression is true
     * in aligned with the semantics of the language;
     * False otherwise.
     */
    private boolean isTruthy(Object obj)
    {
        if (obj == null) return false;

        if (obj instanceof Boolean) return (boolean) obj;
        
        // 0 is false
        if (obj instanceof Double) return !((double) obj == 0);

        // Everything else is true
        return true;
    }

    /*
     * Indicates if the provided evaluated expressions
     * are equal. 
     * 
     * @param a The first evaluated expression
     * @param b The second evaluated expression
     * @return True if both evaluated expressions
     * are equal; False otherwise.
     */
    private boolean isEqual(Object a, Object b)
    {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        return a.equals(b);
    }
}