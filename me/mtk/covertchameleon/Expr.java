package me.mtk.covertchameleon;

import java.util.List;

abstract class Expr 
{
	interface Visitor<T>
	{
		T visitBinaryExpr(Binary expr);
		T visitUnaryExpr(Unary expr);
		T visitLiteralExpr(Literal expr);
		T visitPrintExpr(Print expr);
		T visitLetExpr(Let expr);
	}

	abstract <T> T accept(Visitor<T> visitor);

	static class Binary extends Expr
	{
		final Token operator;
		final Expr first;
		final Expr second;

		public Binary(Token operator, Expr first, Expr second)
		{
			this.operator = operator;
			this.first = first;
			this.second = second;
		}

		@Override
		public <T> T accept(Visitor<T> visitor)
		{
			return visitor.visitBinaryExpr(this);
		}
	}

	static class Unary extends Expr
	{
		final Token operator;
		final Expr right;

		public Unary(Token operator, Expr right)
		{
			this.operator = operator;
			this.right = right;
		}

		@Override
		public <T> T accept(Visitor<T> visitor)
		{
			return visitor.visitUnaryExpr(this);
		}
	}

	static class Literal extends Expr
	{
		final Object value;

		public Literal(Object value)
		{
			this.value = value;
		}

		@Override
		public <T> T accept(Visitor<T> visitor)
		{
			return visitor.visitLiteralExpr(this);
		}
	}

	static class Print extends Expr
	{
		final List<Expr> exprs;

		public Print(List<Expr> exprs)
		{
			this.exprs = exprs;
		}

		@Override
		public <T> T accept(Visitor<T> visitor)
		{
			return visitor.visitPrintExpr(this);
		} 
	}

	static class Let extends Expr
	{
		final Scope scope;
		final List<Expr> exprs;

		public Let(Scope scope, List<Expr> exprs)
		{
			this.scope = scope;
			this.exprs = exprs;
		}

		@Override
		public <T> T accept(Visitor<T> visitor)
		{
			return visitor.visitLetExpr(this);
		} 
	}
}