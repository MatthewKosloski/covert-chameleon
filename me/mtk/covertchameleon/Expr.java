package me.mtk.covertchameleon;

import java.util.List;

abstract class Expr 
{
	interface Visitor<T>
	{
		T visitGroupExpr(Group expr);
		T visitBinaryExpr(Binary expr);
		T visitUnaryExpr(Unary expr);
		T visitLiteralExpr(Literal expr);
		T visitPrintExpr(Print expr);
		T visitLetExpr(Let expr);
		T visitBindingExpr(Binding expr);
		T visitVariableExpr(Variable expr);
	}

	abstract <T> T accept(Visitor<T> visitor);

	static class Group extends Expr
	{
		final List<Expr> exprs;

		public Group(List<Expr> exprs)
		{
			this.exprs = exprs;
		}

		@Override
		public <T> T accept(Visitor<T> visitor)
		{
			return visitor.visitGroupExpr(this);
		}
	}

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
		final Token operator;
		final List<Expr> exprs;

		public Print(Token operator, List<Expr> exprs)
		{
			this.operator = operator;
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
		final List<Binding> bindings;
		final Expr body;

		public Let(List<Binding> bindings, Expr body)
		{
			this.bindings = bindings;
			this.body = body;
		}

		@Override
		public <T> T accept(Visitor<T> visitor)
		{
			return visitor.visitLetExpr(this);
		} 
	}

	static class Binding extends Expr
	{
		final Token identifier;
		final Expr value;

		public Binding(Token identifier, Expr value)
		{
			this.identifier = identifier;
			this.value = value;
		}

		@Override
		public <T> T accept(Visitor<T> visitor)
		{
			return visitor.visitBindingExpr(this);
		} 
	}

	static class Variable extends Expr
	{
		final Token name;

		public Variable(Token name)
		{
			this.name = name;
		}

		@Override
		public <T> T accept(Visitor<T> visitor)
		{
			return visitor.visitVariableExpr(this);
		}
	}
}