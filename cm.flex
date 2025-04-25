import java_cup.runtime.*; 

%%

%class Lexer

%eofval{
    return null; 
%eofval}; 

%line
%column

%cup

%{
    private Symbol symbol (int type)
    {
        return new Symbol(type, yyline, yycolumn); 
    }

    private Symbol symbol (int type, Object value)
    {
        return new Symbol(type, yyline, yycolumn, value); 
    }
%}



id = [_a-zA-Z][_a-zA-Z0-9]*
num = [0-9]+
whitespace = [ \t\n\r\f]
truth = true | false

%state COMMENT


%%


<YYINITIAL> "bool"          { return symbol(sym.BOOL); }
<YYINITIAL> "if"            { return symbol(sym.IF); }
<YYINITIAL> "else"          { return symbol(sym.ELSE); }
<YYINITIAL> "int"           { return symbol(sym.INT); }
<YYINITIAL> "return"        { return symbol(sym.RETURN); }
<YYINITIAL> "void"          { return symbol(sym.VOID); }
<YYINITIAL> "while"         { return symbol(sym.WHILE); }
<YYINITIAL> "+"             { return symbol(sym.PLUS); }
<YYINITIAL> "-"             { return symbol(sym.MINUS); }
<YYINITIAL> "*"             { return symbol(sym.MUL); }
<YYINITIAL> "/"             { return symbol(sym.DIV); }
<YYINITIAL> "<"             { return symbol(sym.LT); }
<YYINITIAL> "<="            { return symbol(sym.LE); }
<YYINITIAL> ">"             { return symbol(sym.GT); }
<YYINITIAL> ">="            { return symbol(sym.GE); }
<YYINITIAL> "=="            { return symbol(sym.TESTEQ); }
<YYINITIAL> "!="            { return symbol(sym.NE); }
<YYINITIAL> "~"             { return symbol(sym.NOT); }
<YYINITIAL> "||"            { return symbol(sym.OR); }
<YYINITIAL> "&&"            { return symbol(sym.AND); }
<YYINITIAL> "="             { return symbol(sym.EQ); }
<YYINITIAL> ";"             { return symbol(sym.SEMICOLON); }
<YYINITIAL> ","             { return symbol(sym.COMMA); }
<YYINITIAL> "("             { return symbol(sym.LPAREN); }
<YYINITIAL> ")"             { return symbol(sym.RPAREN); }
<YYINITIAL> "["             { return symbol(sym.LSQUARE); }
<YYINITIAL> "]"             { return symbol(sym.RSQUARE); }
<YYINITIAL> "{"             { return symbol(sym.LCURLY); }
<YYINITIAL> "}"             { return symbol(sym.RCURLY); }
<YYINITIAL> {truth}         { return symbol(sym.TRUTH, yytext()); }
<YYINITIAL> {id}            { return symbol(sym.ID, yytext()); }
<YYINITIAL> {num}           { return symbol(sym.NUM, yytext()); }
<YYINITIAL> {whitespace}+   { /* skip */ }
<YYINITIAL> "/*"            { yybegin(COMMENT); }
<YYINITIAL> .               { 
                                System.err.println("Error when scanning: " + yytext() + " line: " + yyline); 
                                return symbol(sym.ERROR); 
                            }

<COMMENT> "*/"              { yybegin(YYINITIAL); }
<COMMENT> [^]|\n            { /* skip comments */}
