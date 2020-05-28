# covert-chameleon

In my quest to understand how interpeters for high-level programming languages work, I decided to make my own functional programming language called Torrey.  The language is inspired by [Scheme](https://www.scheme.com/tspl4/) and [Clojure](https://clojure.org/index).

Instead of building it all in one go (which would be too overwhelming), I decided to break it up into iterations.  Each iteration will be an independent interpreter that is more complex than the last.

Covert Chameleon is the second iteration of the Torrey interpreter, a continuation of the first iteration, [Magnetic Moose](https://github.com/MatthewKosloski/magnetic-moose).

The interpreter has all of the functionality included in Magnetic Moose (interpretation of fully parenthesized arithmetic expressions) but adds the following:

- A `let` expression that creates a lexical scope and binds values to identifiers within the scope
- Control flow structures (if-else, for-loop, and while-loop) 
- Relational (e.g, `>=`) and logical (e.g., `==`, 'or', 'and') operators
- Boolean data types (`true` and `false`)
- Null data type (`null`)
- `print` and `println` expressions for sending output to the standard output stream

## Checklist

What has and has not (but will) be implemented:

- [x] Let expression
- [ ] Control flow
- [x] Relational operators
- [ ] Logical operators
- [x] Boolean data types
- [x] `null` data type
- [x] `print` expressions

## Grammar

`Parser.java` implements the following grammar (written in [Extended Backus-Naur form](https://en.wikipedia.org/wiki/Extended_Backus%E2%80%93Naur_form)) that describes the language's syntax:

```
program               -> expression* EOF ;

expression            -> equality 
                      | let 
                      | print 
                      | if 
                      | then
                      | else
                      | cond ;

equality              -> "(" ("equal?" | "nequal?") comparison comparison+ ")" ;
comparison            -> "(" ( ">" | ">=" | "<" | "<=" ) binary binary+ ")" ; 
binary                -> "(" ("+" | "-" | "*" | "/" | "//" | "%") unary unary+ ")" ;
unary                 -> ("+" | "-" | "not" | "true?") expression | literal ;

let                   -> "(" "let" bindings body ")" ;
bindings              -> "[" binding+ "]" ;
binding               -> identifier expression ;
body                  -> expression* ;

print                 -> "(" ("print" | "println") body")" ;

if                    -> "(" "if" equality then else? ")" ;
then                  -> "(" "then" body ")" ;
else                  -> "(" "else" body ")" ;

cond                  -> "(" "cond" clause+ else? ")" ;
clause                -> "(" equality body ")" ;

literal               -> number | identifier 
                      | boolean 
                      | "null" ;

number                -> float | integer ;
float                 -> [0-9]+ "." [0-9]+ ;
integer               -> [0-9]+ ;

identifier            -> initial subsequent* ;
initial               -> [a-zA-Z_$] ;
subsequent            -> initial | [0-9?-] ;

boolean               -> ("true" | "false") ;
```

_Note: The `print` expressions and the `true?` predicate ought to be removed in favor of one or more standard library functions (when the standard library gets introduced)._

## Code Examples

```
; This is an inline comment

; These
; are 
; multiple
; inline
; comments !!!

``This is a
    multi-line block comment :)``

;; Most expressions, except for literals and some
;; unary expressions, are enclosed in a pair of
;; parentheses. See the above grammar for a list
;; of literals. Every parenthesized expression
;; can take a variable number of expressions
;; as "arguments."

;; Use the print(ln) expressions to send evaluated
;; expressions to the standard output stream:

(print 1)
;=> 1
(print 1 2)
;=> 12
(println 1 2 3)
;=> 1
;   2
;   3

;; Negate the truthiness of an expression using
;; the unary `not` operator.

(print (not true))
;=> false
(print (not 0))
;=> true

;; Check the truthiness of an expression using 
;; the true? predicate. The only expressions
;; that evaluate to false are: false, 0, and
;; null.

(print (true? 1))
;=> true
(print (true? 0))
;=> false

;; It should be noted that the true? predicate is
;; a short-hand for a double `not`.

(println (not (not true)))
;=> true

;; Check if two or more expressions are equal using the 
;; equal? predicate.

(print (equal? 1 1))
;=> true
(print (equal? 1 1 2))
;=> false
(print (equal? 1 1))
;=> true
(print (equal? 2 2 2))
;=> false

;; The previous expression evaluates to false because
;; the equal? predicate is analagous to the binary
;; equality operator `==` and has left associativity.
;; That is, the operands are evaluated from left to right.
;; In the aforementioned expression, (equal? 2 2) is 
;; evaluated first, and then the value (true) is 
;; compared to 2 in (equal? true 2), which of 
;; course is false.

;; Perform comparisons on expressions that evaluate to 
;; numbers using relational operators such as >, >=, 
;; <, and <=. That is, the operands to relational 
;; operators _must_ be numbers.

(print (> 1 0))
;=> true
(print (> 3 2 1))
;=>program.in:1:9: RuntimeError: Binary operator ">" only operates on numbers
;=>
;=>Ln 1, Col 9>
;=>        "(print (> 3 2 1))"
;=>                 ^

;; Simiarly to the equal? predicate, relational operators
;; have left associativity and thus evaluation is performed
;; from left to right.

;; Binary arithmetic can be performed on expressions that
;; evaluate to numbers using the following operators:
;;     + (addition)
;;     - (subtraction)
;;     * (multiplication)
;;     / (division)
;;    // (integer floor division)
;;     % (modulus)

(print (+ 1 2 3))
;=> 6
(print (- 10 (+ 2 3)))
;=> 5
(print (/ 22 8))
;=> 2.75
(print (// 22 8))
;=> 2
(print (% 10 3))
;=> 1

;; If the denominator of an arithmetic expression
;; evaluates to 0, then an RuntimeError occurs.

(print (/ 5 (+ 999 -999)))
;=> program.in:1:9: RuntimeError: Cannot divide by 0
;=>
;=> Ln 1, Col 9>
;=>         "(print (/ 5 (+ 999 -999)))"
;=>                  ^

;; In addition to unary `not`, which negates
;; the truthiness of the expression, there are
;; two other unary operators '+' and '-'.

(print (+ 1 2 -3))
;=> 0
(print -(* +1 +5))
;=> -5

;; To create a lexical scope, use the `let`
;; expression. Refer to the above grammar
;; for the formal syntax and to determine
;; which characters are valid for identifiers.

(let [x 1] (print x))
;=> 1
(let [x 1 y (+ 1 x)] (println x y))
;=> 1
;   2

;; Nested scopes can also be created.

(let [a -1 b -2]
    (println a)
    (let [a 999 b b]
        (println a b)))
;=> -1
;=> 999
;=> -2

;; Attempting to refer to an identifier
;; that doesn't exist causes a RuntimeError.

(let [a 100] (println b))
;=> program.in:1:23: RuntimeError: Undefined identifier 'b'
;=>
;=> Ln 1, Col 23>
;=>        "(let [a 100] (println b))"
;=>                               ^

```