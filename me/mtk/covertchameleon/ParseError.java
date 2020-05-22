package me.mtk.covertchameleon;

public class ParseError extends InterpreterError
{
    public ParseError(Token token, String msg)
    {
        super(token, msg);
    }

    @Override
    public String getErrorName()
    {
        return "ParseError";
    }
}