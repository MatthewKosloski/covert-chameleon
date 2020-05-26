# covert-chameleon

In my quest to understand how interpeters for high-level programming languages work, I decided to make my own functional programming language called Torrey.  The language is inspired by [Scheme](https://www.scheme.com/tspl4/) and [Clojure](https://clojure.org/index).

Instead of building it all in one go (which would be too overwhelming), I decided to break it up into iterations.  Each iteration will be an independent interpreter that is more complex than the last.

Covert Chameleon is the second iteration of the Torrey interpreter, a continuation of the first iteration, [Magnetic Moose](https://github.com/MatthewKosloski/magnetic-moose).

The interpreter has all of the functionality included in Magnetic Moose (interpretation of fully parenthesized arithmetic expressions) but adds the following:

- A `let` expression that creates a lexical scope and binds values to identifiers within the scope
- Control flow structures (if-else, for-loop, and while-loop) 
- Relational (e.g, `>=`) and logical (e.g., `==`) operators
- Boolean data types (`true` and `false`)
- Null data type (`null`)
- `print` and `println` expressions for sending output to the standard output stream

## Grammar

`Parser.java` implements the following grammar that describes the language's syntax:

```
program               -> expression* EOF ;

expression            -> equality 
                      | let 
                      | print ;

equality              -> "(" ("equal?" | "nequal?") comparison comparison+ ")" ;
comparison            -> "(" ( ">" | ">=" | "<" | "<=" ) binary binary+ ")" ; 
binary                -> "(" ("+" | "-" | "*" | "/" | "//" | "%") unary unary+ ")" ;
unary                 -> ("+" | "-" | "not")? equality | literal ;

let                   -> "(" "let" bindings body ")" ;
bindings              -> "[" binding+ "]" ;
binding               -> identifier equality ;
body                  -> expression+ ;

print                 -> "(" ("print" | "println") equality+ ")" ;

literal               -> number | identifier 
                      | boolean 
                      | "null" ;

number                -> float | integer ;
float                 -> [0-9]+ "." [0-9]+ ;
integer               -> [0-9]+ ;

identifier            -> initial subsequent* ;
initial               -> [a-zA-Z_$] ;
subsequent            -> initial | [0-9?] ;

boolean               -> ("true" | "false") ;
```

_Note: The `print` expression ought to be removed in favor of one or more standard library functions (when the standard library gets introduced)._