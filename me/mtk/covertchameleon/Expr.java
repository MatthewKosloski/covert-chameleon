package me.mtk.covertchameleon;

abstract class Expr 
{
	interface Visitor<T>
	{
		T visitBinaryExpr(Binary expr);
		T visitUnaryExpr(Unary expr);
		T visitNumberExpr(Number expr);
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

	static class Number extends Expr
	{
		final double value;

		public Number(double value)
		{
			this.value = value;
		}

		@Override
		public <T> T accept(Visitor<T> visitor)
		{
			return visitor.visitNumberExpr(this);
		}
	}
}