package me.mtk.covertchameleon;

import java.util.HashMap;
import java.util.Map;

// Implements the lexical scope of the let
// expression.
public class Scope 
{
    private final Scope parent;
    private final Map<String, Object> values = new HashMap<>();

    public Scope(Scope parent)
    {
        this.parent = parent;
    }
}