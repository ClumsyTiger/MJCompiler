// ________________________________________________________________________________________________
// import section
package rs.ac.bg.etf.pp1;


// ________________________________________________________________________________________________
// directive section
%%

%class Lexer
%cup
// %unicode
%line
%column



// methods
%{
    private static int symbolIdx = 0;

    // create a symbol from the given symbol type
    private Symbol new_symbol( int symbolCode )
    {
        return new_symbol( symbolCode, null );
    }
    // create a symbol from the given symbol type and its value
    private Symbol new_symbol( int symbolCode, Object value )
    {
        return new Symbol( symbolCode, symbolIdx++, yyline+1, yycolumn, value );
    }
%}



// classes
Newline    = \r|\n|\r\n
NotNewline = [^\r\n]
Whitespace = [ \t\f]+

// different types of comments
// +   the line comment can be on the last line of the file, therefore not ending with a newline
LineComment      = "//" {NotNewline}* {Newline}?
MultilineComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"

IntLiteral = 0 | [1-9][0-9]*
BoolLiteral = true | false
CharLiteral = "'"."'"

Identifier        = ([:jletter:]|_) ([:jletterdigit:]|_)*
InvalidIdentifier = [0-9]           ([:jletterdigit:]|_)*





// ________________________________________________________________________________________________
// regex section
%%

// send newlines, whitespaces and comments to the parser
// +   the parser will filter them out (used for better error reporting)
{Newline}           { return new_symbol( SymbolCode.newline, yytext() ); }
{Whitespace}        { return new_symbol( SymbolCode.whitespace, yytext() ); }
{LineComment}       { return new_symbol( SymbolCode.line_comment, yytext() ); }
{MultilineComment}  { return new_symbol( SymbolCode.multi_comment, yytext() ); }
<<EOF>>             { return new_symbol( SymbolCode.EOF ); }



// keywords
"program"    { return new_symbol( SymbolCode.PROGRAM_K, yytext() ); }
"class"      { return new_symbol( SymbolCode.CLASS_K, yytext() ); }
"enum"       { return new_symbol( SymbolCode.invalid /*SymbolCode.ENUM_K*/, yytext() ); }
"extends"    { return new_symbol( SymbolCode.EXTENDS_K, yytext() ); }

"static"     { return new_symbol( SymbolCode.STATIC_K, yytext() ); }
"const"      { return new_symbol( SymbolCode.CONST_K, yytext() ); }
"void"       { return new_symbol( SymbolCode.VOID_K, yytext() ); }

"if"         { return new_symbol( SymbolCode.IF_K, yytext() ); }
"else"       { return new_symbol( SymbolCode.ELSE_K, yytext() ); }
"switch"     { return new_symbol( SymbolCode.SWITCH_K, yytext() ); }
"case"       { return new_symbol( SymbolCode.CASE_K, yytext() ); }
"default"    { return new_symbol( SymbolCode.invalid /*SymbolCode.DEFAULT_K*/, yytext() ); }
"break"      { return new_symbol( SymbolCode.BREAK_K, yytext() ); }
"continue"   { return new_symbol( SymbolCode.CONTINUE_K, yytext() ); }
"return"     { return new_symbol( SymbolCode.RETURN_K, yytext() ); }

"do"         { return new_symbol( SymbolCode.DO_K, yytext() ); }
"while"      { return new_symbol( SymbolCode.WHILE_K, yytext() ); }

"new"        { return new_symbol( SymbolCode.NEW_K, yytext() ); }
"print"      { return new_symbol( SymbolCode.PRINT_K, yytext() ); }
"read"       { return new_symbol( SymbolCode.READ_K, yytext() ); }



// operators
"++"         { return new_symbol( SymbolCode.plusplus, yytext() ); }
"--"         { return new_symbol( SymbolCode.minusminus, yytext() ); }

"+"          { return new_symbol( SymbolCode.plus, yytext() ); }
"-"          { return new_symbol( SymbolCode.minus, yytext() ); }
"*"          { return new_symbol( SymbolCode.mul, yytext() ); }
"/"          { return new_symbol( SymbolCode.div, yytext() ); }
"%"          { return new_symbol( SymbolCode.perc, yytext() ); }

"=="         { return new_symbol( SymbolCode.eq, yytext() ); }
"!="         { return new_symbol( SymbolCode.ne, yytext() ); }
">"          { return new_symbol( SymbolCode.gt, yytext() ); }
">="         { return new_symbol( SymbolCode.ge, yytext() ); }
"<"          { return new_symbol( SymbolCode.lt, yytext() ); }
"<="         { return new_symbol( SymbolCode.le, yytext() ); }
"&&"         { return new_symbol( SymbolCode.and, yytext() ); }
"||"         { return new_symbol( SymbolCode.or, yytext() ); }

"="          { return new_symbol( SymbolCode.assign, yytext() ); }

";"          { return new_symbol( SymbolCode.semicol, yytext() ); }
","          { return new_symbol( SymbolCode.comma, yytext() ); }
"."          { return new_symbol( SymbolCode.dot, yytext() ); }
"{"          { return new_symbol( SymbolCode.lbrace, yytext() ); }
"}"          { return new_symbol( SymbolCode.rbrace, yytext() ); }
"("          { return new_symbol( SymbolCode.lparen, yytext() ); }
")"          { return new_symbol( SymbolCode.rparen, yytext() ); }
"["          { return new_symbol( SymbolCode.lbracket, yytext() ); }
"]"          { return new_symbol( SymbolCode.rbracket, yytext() ); }
// "?"       { return new_symbol( SymbolCode.qmark, yytext() ); }
":"          { return new_symbol( SymbolCode.colon, yytext() ); }



// constants
{IntLiteral}    { return new_symbol( SymbolCode.int_lit, Integer.parseInt( yytext() ) ); }
{BoolLiteral}   { return new_symbol( SymbolCode.bool_lit, Boolean.parseBoolean( yytext() ) ); }
{CharLiteral}   { return new_symbol( SymbolCode.char_lit, yytext().charAt( 1 ) ); }

// identifiers
{Identifier} 	      { return new_symbol( SymbolCode.ident, yytext() ); }
{InvalidIdentifier}   { return new_symbol( SymbolCode.invalid, yytext() ); }

// error fallback (for unrecognized token)
[^]             { return new_symbol( SymbolCode.invalid, yytext() ); }






