package me.mtk.covertchameleon;

import java.util.List;
import java.util.ArrayList;

// The Parser is the part of the interpreter that takes
// a list of Token objects as input and, from those tokens, 
// constructs an abstract syntax tree (AST). The construction
// of the AST is based on the grammar of the language of 
// the source program. That is, the Parser is an implementation
// of the grammar. Each nonterminal symbol of the grammar
// is implemented as a method.
// 
// The Parser is also responsible for reporting syntax errors
// to the user.
public class Parser 
{
    // The tokens of the source program. These come from
    // the Lexer.
    private final List<Token> tokens;

    // The current position in the token list (an index in tokens).
    // This member can take on any value in the range [0, n - 1], where
    // n is the size of tokens. This is the index in tokens of the next
    // Token that is to be processed. That is, the index of the Token
    // that is currently being processed is one less than this value.
    private int position = 0;

    /**
     * Constructs a new Parser object, initializing
     * it with a list of tokens.
     * 
     * @param tokens A list of tokens.
     */
    public Parser(List<Token> tokens)
    {
        this.tokens = tokens;
    }

    /**
     * Parses the program.
     * 
     * @return A list of expressions representing the program.
     */
    public List<Expr> parse() throws ParseError
    {
        return program();
    }

    /*
     * Implements the following production rule:
     * program -> expression* EOF ;
     *
     * @return A list of expressions to be interpreted.
     */
    private List<Expr> program()
    {
        List<Expr> expressions = new ArrayList<>();
        
        while (hasTokens()) 
            expressions.add(expression());
        
        return expressions;
    }

    // expression -> equality | let | print ;
    private Expr expression()
    {
        if (!hasExpression())
            throw new ParseError(peek(), "Expected an expression");

        if (peek(TokenType.LPAREN) && peekNext(TokenType.LET))
        {
            // expression -> let ;
            System.out.println("expression -> let ;");
        }
        else if (peek(TokenType.LPAREN) && 
            peekNext(TokenType.PRINT, TokenType.PRINTLN))
        {
            // expression -> print ;
            return print();
        }

        // expression -> equality ;
        return equality();
    }

    // print -> "(" ("print" | "println") equality+ ")" ;
    private Expr print()
    {
        // Consume (
        nextToken();

        Token operator = nextToken();

        List<Expr> exprs = new ArrayList<>();

        while (hasExpression()) exprs.add(equality());

        consume(TokenType.RPAREN, "Missing closing \"(\"");
        return new Expr.Print(operator, exprs);
    }

    // equality -> "(" ("==" | "!=") comparison comparison+ ")" ;
    private Expr equality()
    {
        if (peek(TokenType.LPAREN) && peekNext(TokenType.EQUAL_TO,
            TokenType.NOT_EQUAL_TO))
        {
            // Consume (
            nextToken();

            Token operator = nextToken();
            Expr first = comparison();
            Expr second = comparison();
            Expr expr = new Expr.Binary(operator, first, second);

            while (hasExpression())
            {
                second = comparison();
                expr = new Expr.Binary(operator, expr, second);
            }

            consume(TokenType.RPAREN, "Missing closing \"(\"");
            return expr;
        }

        return comparison();
    }

    // comparison -> "(" ( ">" | ">=" | "<" | "<=" ) binary binary+ ")" ; 
    private Expr comparison()
    {
        if (peek(TokenType.LPAREN) && peekNext(TokenType.GREATER_THAN, 
            TokenType.GREATER_THAN_OR_EQUAL_TO, TokenType.LESS_THAN, 
            TokenType.LESS_THAN_OR_EQUAL_TO))
        {
            // Consume (
            nextToken();

            Token operator = nextToken();
            Expr first = binary();
            Expr second = binary();
            Expr expr = new Expr.Binary(operator, first, second);

            while (hasExpression())
            {
                second = binary();
                expr = new Expr.Binary(operator, expr, second);
            }

            consume(TokenType.RPAREN, "Missing closing \"(\"");
            return expr;
        }

        return binary();
    }

    /*
     * Implements the following production rule:
     * binary -> "(" ("+" | "-" | "*" | "/") unary unary+ ")" ;
     *
     * @return A binary expression.
     */
    private Expr binary()
    {
        if (peek(TokenType.LPAREN) && peekNext(TokenType.PLUS, TokenType.MINUS,
            TokenType.STAR, TokenType.SLASH))
        {
            // Consume (
            nextToken();

            Token operator = nextToken();
            Expr first = unary();
            Expr second = unary();
            Expr expr = new Expr.Binary(operator, first, second);

            while (hasExpression())
            {
                second = unary();
                expr = new Expr.Binary(operator, expr, second);
            }

            consume(TokenType.RPAREN, "Missing closing \"(\"");
            return expr;
        }

        return unary();
    }

     /*
     * Implements the following production rule:
     * unary -> ("+" | "-")? (binary | literal) ;
     *
     * @return A unary expression.
     */
    private Expr unary()
    {
        if (match(TokenType.PLUS, TokenType.MINUS))
        {
            Token operator = previous();
            Expr right;
            if (peek(TokenType.LPAREN))
                // unary -> ("+" | "-") binary ;
                right = binary();
            else
                // unary -> ("+" | "-") literal ;
                right = literal();

            return new Expr.Unary(operator, right);
        }
        else if (peek(TokenType.LPAREN))
        {
            // unary -> binary ;
            return binary();
        }
        
        // unary -> literal ;
        return literal();
    }

    /*
     * Implements the following production rule:
     * literal -> number | identifier | boolean | "null" ;
     *
     * @return A number expression.
     */
    private Expr literal()
    {
        if (match(TokenType.NUMBER))
            return new Expr.Literal(previous().literal);
        else if (match(TokenType.IDENTIFIER))
            return new Expr.Variable(previous());
        else if (match(TokenType.TRUE))
            return new Expr.Literal(true);
        else if (match(TokenType.FALSE))
            return new Expr.Literal(false);
        else if (match(TokenType.NULL))
            return new Expr.Literal(null);

        throw new ParseError(peek(), "Expected an expression");
    }

    /*
     * If the next token's type matches at least one 
     * of the provided types, consume it and return true.
     *  
     * @param types A variable number of token types.
     * @return True if the token type of the next token
     * matches at least one of the provided types; False
     * otherwise.
     */
    private boolean match(TokenType... types)
    {
        for (TokenType type : types)
        {
            if (peekType(type))
            {
                nextToken();
                return true;
            }
        }
        return false;
    }

    /*
     * Returns the next token if its type is of the provided type.
     * 
     * @param type The type of the token.
     * @param msg The error message to display if the next token
     * is not of the provided type.
     * @return The next token
     * @throws ParseError if next token is not of the provided type.
     */
    private Token consume(TokenType type, String msg)
    {
        if (peekType(type)) return nextToken();
        throw new ParseError(peek(), msg);
    }

    /*
     * Gets the next token.
     * 
     * @return The next token.
     */
    private Token nextToken()
    {
        if (hasTokens())
            return tokens.get(position++);
        else
            return previous();
    }

    /*
     * Indicates if there are more tokens to process.
     * 
     * @return True if there are no more tokens to process;
     * False otherwise.
     */
    private boolean hasTokens()
    {
        return peek().type != TokenType.EOF;
    }

    /*
     * Returns the next token.
     * 
     * @return The next token.
     */
    private Token peek()
    {
        return tokens.get(position);
    }

    /*
     * Returns the next-next token.
     * 
     * @return The next-next token.
     */
    private Token peekNext()
    {
        return tokens.get(position + 1);
    }

    /*
     * Indicates if the next token is of the provided type.
     * 
     * @param type The type of the token.
     * @return True if the next token's TokenType is equal to
     * type; False otherwise.
     */
    private boolean peekType(TokenType type)
    {
        return peek().type == type;
    }

    /*
     * Indicates if the next-next token is of the provided type.
     * 
     * @param type The type of the token.
     * @return True if the next-next token's TokenType is equal to
     * type; False otherwise.
     */
    private boolean peekNextType(TokenType type)
    {
        return peekNext().type == type;
    }

    /*
     * Indicates whether the next token is one of 
     * the provided token types.
     *  
     * @param types A variable number of token types.
     * @return True if the next token is one of the provided
     * token types; False otherwise.
     */
    private boolean peek(TokenType... types)
    {
        for (TokenType type : types)
        {
            if (peekType(type)) return true;
        }
        return false;
    }

    /*
     * Indicates whether the next-next token is one of 
     * the provided token types.
     *  
     * @param types A variable number of token types.
     * @return True if the next-next token is one of the provided
     * token types; False otherwise.
     */
    private boolean peekNext(TokenType... types)
    {
        for (TokenType type : types)
        {
            if (peekNextType(type)) return true;
        }
        return false;
    }

    /*
     * Returns the previously consumed token.
     * 
     * @return The previously consumed token.
     */
    private Token previous()
    {
        return tokens.get(position - 1);
    }

    /*
     * Indicates if the next token is the start of an expression.
     * 
     * @return True if the next token is the start of an expression; 
     * False otherwise.
     */
    private boolean hasExpression()
    {
        return peek(
            TokenType.LPAREN, TokenType.NUMBER, 
            TokenType.MINUS, TokenType.PLUS, 
            TokenType.TRUE, TokenType.FALSE,
            TokenType.NULL, TokenType.IDENTIFIER
        );
    }
}