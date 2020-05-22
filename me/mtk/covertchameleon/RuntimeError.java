package me.mtk.covertchameleon;

public class RuntimeError extends InterpreterError
{
    public RuntimeError(Token token, String msg)
    {
        super(token, msg);
    }

    @Override
    public String getErrorName()
    {
        return "RuntimeError";
    }
}