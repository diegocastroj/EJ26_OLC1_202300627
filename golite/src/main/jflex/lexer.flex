package com.olc1;

import java_cup.runtime.Symbol;

%%

// --- CONFIGURACIÓN Y MACROS ---
%init{
    yyline = 1;
    yycolumn = 1;
%init}

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

    private void resetColumna() {
        yycolumn = 1;
    }
%}

BLANCOS = [\ \r\t\f]+
SALTO = \n
ENTERO = [0-9]+
DECIMAL = [0-9]+"."[0-9]+
ID = [a-zA-Z_][a-zA-Z0-9_]*

COMENTARIO_LINEA = "//" [^\r\n]*
COMENTARIO_MULTI = "/*" [^*]* "*"+ ([^*/][^*]* "*"+)* "/"

%%

<YYINITIAL> {
    // 1. Palabras reservadas
    "nil"               { return new Symbol(sym.NULL, yyline, yycolumn, yytext()); }
    "reflect.TypeOf"    { return new Symbol(sym.REFLECT, yyline, yycolumn, yytext()); }
    "strconv.Atoi"      { return new Symbol(sym.ATOI, yyline, yycolumn, yytext()); }
    "strconv.ParseFloat" { return new Symbol(sym.PARSEFLOAT, yyline, yycolumn, yytext()); }
    "break"             { return new Symbol(sym.BREAK, yyline, yycolumn, yytext()); }
    "continue"          { return new Symbol(sym.CONTINUE, yyline, yycolumn, yytext()); }
    "return"            { return new Symbol(sym.RETURN, yyline, yycolumn, yytext()); }
    "for"               { return new Symbol(sym.FOR, yyline, yycolumn, yytext()); }
    "range"             { return new Symbol(sym.RANGE, yyline, yycolumn, yytext()); }
    "if"                { return new Symbol(sym.IF, yyline, yycolumn, yytext()); }
    "else"              { return new Symbol(sym.ELSE, yyline, yycolumn, yytext()); }
    "func"              { return new Symbol(sym.FUNC, yyline, yycolumn, yytext()); }
    "main"              { return new Symbol(sym.MAIN, yyline, yycolumn, yytext()); }
    "var"               { return new Symbol(sym.VAR, yyline, yycolumn, yytext()); }
    "true"              { return new Symbol(sym.TRUE, yyline, yycolumn, yytext()); }
    "false"             { return new Symbol(sym.FALSE, yyline, yycolumn, yytext()); }
    "switch"            { return new Symbol(sym.SWITCH, yyline, yycolumn, yytext()); }
    "case"              { return new Symbol(sym.CASE, yyline, yycolumn, yytext()); }
    "default"           { return new Symbol(sym.DEFAULT, yyline, yycolumn, yytext()); }
    "struct"            { return new Symbol(sym.STRUCT, yyline, yycolumn, yytext()); }

    // Tipos
    "int"               { return new Symbol(sym.T_INT, yyline, yycolumn, yytext()); }
    "float64"           { return new Symbol(sym.T_FLOAT64, yyline, yycolumn, yytext()); }
    "string"            { return new Symbol(sym.T_STRING, yyline, yycolumn, yytext()); }
    "bool"              { return new Symbol(sym.T_BOOL, yyline, yycolumn, yytext()); }
    "rune"              { return new Symbol(sym.T_RUNE, yyline, yycolumn, yytext()); }
    
    "fmt.Println"       { return new Symbol(sym.IMPRIMIR, yyline, yycolumn, yytext()); }
    "len"               { return new Symbol(sym.LEN, yyline, yycolumn, yytext()); }
    "append"            { return new Symbol(sym.APPEND, yyline, yycolumn, yytext()); }
    "slices.Index"      { return new Symbol(sym.SLICES_INDEX, yyline, yycolumn, yytext()); }
    "strings.Join"      { return new Symbol(sym.STRINGS_JOIN, yyline, yycolumn, yytext()); }

    // Operadores y signos
    "==" { return new Symbol(sym.IGUAL, yyline, yycolumn, yytext()); }
    "!=" { return new Symbol(sym.DIFERENTE, yyline, yycolumn, yytext()); }
    "<=" { return new Symbol(sym.MENORIGUAL, yyline, yycolumn, yytext()); }
    ">=" { return new Symbol(sym.MAYORIGUAL, yyline, yycolumn, yytext()); }
    "<"  { return new Symbol(sym.MENOR, yyline, yycolumn, yytext()); }
    ">"  { return new Symbol(sym.MAYOR, yyline, yycolumn, yytext()); }
    "&&" { return new Symbol(sym.AND, yyline, yycolumn, yytext()); }
    "||" { return new Symbol(sym.OR, yyline, yycolumn, yytext()); }
    "!"  { return new Symbol(sym.NOT, yyline, yycolumn, yytext()); }
    ":=" { return new Symbol(sym.DECLARAR_ASIGNAR, yyline, yycolumn, yytext()); }
    "="  { return new Symbol(sym.ASIGNAR, yyline, yycolumn, yytext()); }
    ";"  { return new Symbol(sym.FINCADENA, yyline, yycolumn, yytext()); }
    ":"  { return new Symbol(sym.DOS_PUNTOS, yyline, yycolumn, yytext()); }
    "+"  { return new Symbol(sym.MAS, yyline, yycolumn, yytext()); }
    "-"  { return new Symbol(sym.MENOS, yyline, yycolumn, yytext()); }
    "*"  { return new Symbol(sym.MULT, yyline, yycolumn, yytext()); }
    "/"  { return new Symbol(sym.DIV, yyline, yycolumn, yytext()); }
    "("  { return new Symbol(sym.PAR1, yyline, yycolumn, yytext()); }
    ")"  { return new Symbol(sym.PAR2, yyline, yycolumn, yytext()); }
    "{"  { return new Symbol(sym.LLAVE1, yyline, yycolumn, yytext()); }
    "}"  { return new Symbol(sym.LLAVE2, yyline, yycolumn, yytext()); }
    "%"  { return new Symbol(sym.MOD, yyline, yycolumn, yytext()); }
    "+=" { return new Symbol(sym.MAS_IGUAL, yyline, yycolumn, yytext()); }
    "-=" { return new Symbol(sym.MENOS_IGUAL, yyline, yycolumn, yytext()); }
    ","  { return new Symbol(sym.COMA, yyline, yycolumn, yytext()); }
    "++" { return new Symbol(sym.INCREMENTO, yyline, yycolumn, yytext()); }
    "--" { return new Symbol(sym.DECREMENTO, yyline, yycolumn, yytext()); }
    "["  { return new Symbol(sym.CORCHETE1, yyline, yycolumn, yytext()); }
    "]"  { return new Symbol(sym.CORCHETE2, yyline, yycolumn, yytext()); }
    "."  { return new Symbol(sym.PUNTO, yyline, yycolumn, yytext()); }

    // Literales e IDs
    "'" [^'\\] "'"      { return new Symbol(sym.RUNE_LITERAL, yyline, yycolumn, yytext()); }
    {DECIMAL}           { return new Symbol(sym.DECIMAL, yyline, yycolumn, yytext()); }
    {ENTERO}            { return new Symbol(sym.ENTERO, yyline, yycolumn, yytext()); }
    {ID}                { return new Symbol(sym.ID, yyline, yycolumn, yytext()); }

    // Manejo de espacios y saltos
    {BLANCOS}           { /* Ignorar */ }
    {SALTO}             { yyline++; resetColumna(); }
    {COMENTARIO_LINEA}  { /* Ignorar */ }
    {COMENTARIO_MULTI}  { /* Ignorar */ }

    // Strings
    "\""                { yybegin(CADENA); cadena=""; }

    .                   { 
                           String desc = "Carácter inválido '" + yytext() + "'";
                           errors.add(new com.olc1.reports.GoliteError("Léxico", desc, yyline, yycolumn));
                        }
}

<CADENA> {
    "\\\""  { cadena += "\""; }
    "\\\\"  { cadena += "\\"; }
    "\\n"   { cadena += "\n"; }
    "\\t"   { cadena += "\t"; }
    "\\r"   { cadena += "\r"; }
    "\""    { String temp = cadena; cadena = ""; yybegin(YYINITIAL); return new Symbol(sym.CADENA, yyline, yycolumn, temp); }
    [^\"]   { cadena += yytext(); }
}