package me.mtk.covertchameleon;

public class AstPrinter implements Expr.Visitor<String>
{

    public String print(Expr expr)
    {
        return expr.accept(this);
    }

    @Override 
    public String visitBinaryExpr(Expr.Binary expr)
    {
        return parenthesize(expr.operator.lexeme, expr.first, expr.second);
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr)
    {
        if (expr.right instanceof Expr.Binary)
        {
            Expr.Binary binaryExpr = (Expr.Binary) expr.right;
            return expr.operator.lexeme + parenthesize(binaryExpr.operator.lexeme, 
                binaryExpr.first, binaryExpr.second);
        }
        else 
        {
            Expr.Number numberExpr = (Expr.Number) expr.right;
            return expr.operator.lexeme + numberExpr.value;
        }
    }

    @Override
    public String visitNumberExpr(Expr.Number expr)
    {
        return expr.value + "";
    }

    private String parenthesize(String name, Expr... exprs)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs)
        {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}