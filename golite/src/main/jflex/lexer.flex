package com.olc1;

import java_cup.runtime.Symbol;

%%

%cup 
%class Lexer
%public
%line 
%column 
%unicode

%state CADENA

%{
    String cadena = "";
    // Lista pública de errores léxicos
    public java.util.ArrayList<com.olc1.reports.GoliteError> errors = new java.util.ArrayList<>();
%}

// === CORRECCIÓN DE EXPRESIONES REGULARES ===
// Ahora \r\n es un solo salto, y los blancos no se roban el \r
BLANCOS = [\ \t\f]+
SALTO = \r|\n|\r\n
ENTERO = [0-9]+
DECIMAL = [0-9]+"."[0-9]+
ID = [a-zA-Z_][a-zA-Z0-9_]*

COMENTARIO_LINEA = "//" [^\r\n]*
COMENTARIO_MULTI = "/*" [^*]* "*"+ ([^*/][^*]* "*"+)* "/"

%%

<YYINITIAL> {
    // 1. Palabras reservadas (Se agregó +1 a yyline y yycolumn en TODOS los tokens)
    "nil"               { return new Symbol(sym.NULL, yyline + 1, yycolumn + 1, yytext()); }
    "reflect.TypeOf"    { return new Symbol(sym.REFLECT, yyline + 1, yycolumn + 1, yytext()); }
    "strconv.Atoi"      { return new Symbol(sym.ATOI, yyline + 1, yycolumn + 1, yytext()); }
    "strconv.ParseFloat" { return new Symbol(sym.PARSEFLOAT, yyline + 1, yycolumn + 1, yytext()); }
    "break"             { return new Symbol(sym.BREAK, yyline + 1, yycolumn + 1, yytext()); }
    "continue"          { return new Symbol(sym.CONTINUE, yyline + 1, yycolumn + 1, yytext()); }
    "return"            { return new Symbol(sym.RETURN, yyline + 1, yycolumn + 1, yytext()); }
    "for"               { return new Symbol(sym.FOR, yyline + 1, yycolumn + 1, yytext()); }
    "range"             { return new Symbol(sym.RANGE, yyline + 1, yycolumn + 1, yytext()); }
    "if"                { return new Symbol(sym.IF, yyline + 1, yycolumn + 1, yytext()); }
    "else"              { return new Symbol(sym.ELSE, yyline + 1, yycolumn + 1, yytext()); }
    "func"              { return new Symbol(sym.FUNC, yyline + 1, yycolumn + 1, yytext()); }
    "main"              { return new Symbol(sym.MAIN, yyline + 1, yycolumn + 1, yytext()); }
    "var"               { return new Symbol(sym.VAR, yyline + 1, yycolumn + 1, yytext()); }
    "true"              { return new Symbol(sym.TRUE, yyline + 1, yycolumn + 1, yytext()); }
    "false"             { return new Symbol(sym.FALSE, yyline + 1, yycolumn + 1, yytext()); }
    "switch"            { return new Symbol(sym.SWITCH, yyline + 1, yycolumn + 1, yytext()); }
    "case"              { return new Symbol(sym.CASE, yyline + 1, yycolumn + 1, yytext()); }
    "default"           { return new Symbol(sym.DEFAULT, yyline + 1, yycolumn + 1, yytext()); }
    "struct"            { return new Symbol(sym.STRUCT, yyline + 1, yycolumn + 1, yytext()); }

    // Tipos
    "int"               { return new Symbol(sym.T_INT, yyline + 1, yycolumn + 1, yytext()); }
    "float64"           { return new Symbol(sym.T_FLOAT64, yyline + 1, yycolumn + 1, yytext()); }
    "string"            { return new Symbol(sym.T_STRING, yyline + 1, yycolumn + 1, yytext()); }
    "bool"              { return new Symbol(sym.T_BOOL, yyline + 1, yycolumn + 1, yytext()); }
    "rune"              { return new Symbol(sym.T_RUNE, yyline + 1, yycolumn + 1, yytext()); }
    
    "fmt.Println"       { return new Symbol(sym.IMPRIMIR, yyline + 1, yycolumn + 1, yytext()); }
    "len"               { return new Symbol(sym.LEN, yyline + 1, yycolumn + 1, yytext()); }
    "append"            { return new Symbol(sym.APPEND, yyline + 1, yycolumn + 1, yytext()); }
    "slices.Index"      { return new Symbol(sym.SLICES_INDEX, yyline + 1, yycolumn + 1, yytext()); }
    "strings.Join"      { return new Symbol(sym.STRINGS_JOIN, yyline + 1, yycolumn + 1, yytext()); }

    // Operadores y signos
    "==" { return new Symbol(sym.IGUAL, yyline + 1, yycolumn + 1, yytext()); }
    "!=" { return new Symbol(sym.DIFERENTE, yyline + 1, yycolumn + 1, yytext()); }
    "<=" { return new Symbol(sym.MENORIGUAL, yyline + 1, yycolumn + 1, yytext()); }
    ">=" { return new Symbol(sym.MAYORIGUAL, yyline + 1, yycolumn + 1, yytext()); }
    "<"  { return new Symbol(sym.MENOR, yyline + 1, yycolumn + 1, yytext()); }
    ">"  { return new Symbol(sym.MAYOR, yyline + 1, yycolumn + 1, yytext()); }
    "&&" { return new Symbol(sym.AND, yyline + 1, yycolumn + 1, yytext()); }
    "||" { return new Symbol(sym.OR, yyline + 1, yycolumn + 1, yytext()); }
    "!"  { return new Symbol(sym.NOT, yyline + 1, yycolumn + 1, yytext()); }
    ":=" { return new Symbol(sym.DECLARAR_ASIGNAR, yyline + 1, yycolumn + 1, yytext()); }
    "="  { return new Symbol(sym.ASIGNAR, yyline + 1, yycolumn + 1, yytext()); }
    ";"  { return new Symbol(sym.FINCADENA, yyline + 1, yycolumn + 1, yytext()); }
    ":"  { return new Symbol(sym.DOS_PUNTOS, yyline + 1, yycolumn + 1, yytext()); }
    "+"  { return new Symbol(sym.MAS, yyline + 1, yycolumn + 1, yytext()); }
    "-"  { return new Symbol(sym.MENOS, yyline + 1, yycolumn + 1, yytext()); }
    "*"  { return new Symbol(sym.MULT, yyline + 1, yycolumn + 1, yytext()); }
    "/"  { return new Symbol(sym.DIV, yyline + 1, yycolumn + 1, yytext()); }
    "("  { return new Symbol(sym.PAR1, yyline + 1, yycolumn + 1, yytext()); }
    ")"  { return new Symbol(sym.PAR2, yyline + 1, yycolumn + 1, yytext()); }
    "{"  { return new Symbol(sym.LLAVE1, yyline + 1, yycolumn + 1, yytext()); }
    "}"  { return new Symbol(sym.LLAVE2, yyline + 1, yycolumn + 1, yytext()); }
    "%"  { return new Symbol(sym.MOD, yyline + 1, yycolumn + 1, yytext()); }
    "+=" { return new Symbol(sym.MAS_IGUAL, yyline + 1, yycolumn + 1, yytext()); }
    "-=" { return new Symbol(sym.MENOS_IGUAL, yyline + 1, yycolumn + 1, yytext()); }
    ","  { return new Symbol(sym.COMA, yyline + 1, yycolumn + 1, yytext()); }
    "++" { return new Symbol(sym.INCREMENTO, yyline + 1, yycolumn + 1, yytext()); }
    "--" { return new Symbol(sym.DECREMENTO, yyline + 1, yycolumn + 1, yytext()); }
    "["  { return new Symbol(sym.CORCHETE1, yyline + 1, yycolumn + 1, yytext()); }
    "]"  { return new Symbol(sym.CORCHETE2, yyline + 1, yycolumn + 1, yytext()); }
    "."  { return new Symbol(sym.PUNTO, yyline + 1, yycolumn + 1, yytext()); }

    // Literales e IDs
    "'" [^'\\] "'"      { return new Symbol(sym.RUNE_LITERAL, yyline + 1, yycolumn + 1, yytext()); }
    {DECIMAL}           { return new Symbol(sym.DECIMAL, yyline + 1, yycolumn + 1, yytext()); }
    {ENTERO}            { return new Symbol(sym.ENTERO, yyline + 1, yycolumn + 1, yytext()); }
    {ID}                { return new Symbol(sym.ID, yyline + 1, yycolumn + 1, yytext()); }

    // Manejo de espacios y saltos
    {BLANCOS}           { /* Ignorar */ }
    {SALTO}             { /* Ignorar, JFlex cuenta las lineas solo */ }
    {COMENTARIO_LINEA}  { /* Ignorar */ }
    {COMENTARIO_MULTI}  { /* Ignorar */ }

    // Strings
    "\""                { yybegin(CADENA); cadena=""; }

    .                   { 
                           String desc = "Carácter inválido '" + yytext() + "'";
                           errors.add(new com.olc1.reports.GoliteError("Léxico", desc, yyline + 1, yycolumn + 1));
                        }
}

<CADENA> {
    "\\\""  { cadena += "\""; }
    "\\\\"  { cadena += "\\"; }
    "\\n"   { cadena += "\n"; }
    "\\t"   { cadena += "\t"; }
    "\\r"   { cadena += "\r"; }
    "\""    { String temp = cadena; cadena = ""; yybegin(YYINITIAL); return new Symbol(sym.CADENA, yyline + 1, yycolumn + 1, temp); }
    [^\"]   { cadena += yytext(); }
}