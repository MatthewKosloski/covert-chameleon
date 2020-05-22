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
     * program -> binary ;
     *
     * @return A list of expressions to be interpreted.
     */
    private List<Expr> program()
    {
        List<Expr> expressions = new ArrayList<>();
        
        while (hasTokens()) 
            expressions.add(binary());
        
        return expressions;
    }

    /*
     * Implements the following production rule:
     * binary -> "(" ("+" | "-" | "*" | "/") unary (" " unary)+ ")" ;
     *
     * @return A binary expression.
     */
    private Expr binary()
    {
        if (match(TokenType.LPAREN))
        {
            if (!isValidBinaryOperator(peek()))
            {
                if (peek().lexeme == "")
                {
                    throw new ParseError(peek(), "Expected a binary " 
                    + "operator \"+\", \"-\", \"*\", or \"/\"");
                }
                else
                {
                    throw new ParseError(peek(), String.format("Expected a binary " 
                    + "operator \"+\", \"-\", \"*\", or \"/\" but got \"%s\" "
                    + "instead", peek().lexeme));
                }
            }
            
            Token operator = nextToken();

            Expr first = unary();
            Expr second = unary();
            Expr expr = new Expr.Binary(operator, first, second);

            while (peek(TokenType.LPAREN, TokenType.NUMBER, 
                TokenType.MINUS, TokenType.PLUS))
            {
                second = unary();
                expr = new Expr.Binary(operator, expr, second);
            }
            
            String consumeMsg = String.format("Expected \")\" after " +
            "expression but got \"%s\" instead", peek().lexeme);

            if (peek().lexeme == "")
                consumeMsg = String.format("Missing \")\" after expression");
            
            consume(TokenType.RPAREN, consumeMsg);
            
            return expr;
        }

        throw new ParseError(peek(), String.format("Expected an expression " +
            "starting with \"(\" but got \"%s\" instead", peek().lexeme));
    }

     /*
     * Implements the following production rule:
     * unary -> ("+" | "-")? (binary | number) ;
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
                // unary -> ("+" | "-")? binary
                right = binary();
            else
                // unary -> ("+" | "-")? number
                right = number();

            return new Expr.Unary(operator, right);
        }
        else if (peek(TokenType.LPAREN))
        {
            // unary -> binary;
            return binary();
        }
        else if (peek(TokenType.NUMBER))
        {
            // unary -> number;
            return number();
        }
        
        throw new ParseError(peek(), "Expected an expression starting " +
            "with either \"(\", \"+\", \"-\", or a number");
    }

    /*
     * Implements the following production rule:
     * number -> [0-9]+ "." [0-9]+ | [0-9]+ ;
     *
     * @return A number expression.
     */
    private Expr number()
    {
        if (match(TokenType.NUMBER))
        {
            return new Expr.Number((double) previous().literal);
        }

        throw new ParseError(peek(), String.format("Expected either a number " + 
            "or \"(\" to come after the unary operator " +
            "but got \"%s\" instead", peek().lexeme));
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
            if (isNextTokenOfType(type))
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
        if (isNextTokenOfType(type)) return nextToken();
        throw new ParseError(peek(), msg);
    }

    /*
     * Indicates if the next token is of the provided type.
     * 
     * @param type The type of the token.
     * @return True if the next token's TokenType is equal to
     * type; False otherwise.
     */
    private boolean isNextTokenOfType(TokenType type)
    {
        return peek().type == type;
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
            if (isNextTokenOfType(type)) return true;
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

    private boolean isValidBinaryOperator(Token operator)
    {
        return (operator.type == TokenType.PLUS || operator.type == TokenType.MINUS ||
            operator.type == TokenType.STAR || operator.type == TokenType.SLASH);
    }

}