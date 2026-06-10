package com.olc1;

// importaciones
import java_cup.runtime.Symbol;

%%

// Código de usuario

%init{
    yyline = 1;
    yycolumn = 1;
%init}

// Configuración de FLEX
%cup 
%class Lexer
%public
%line 
%column 
%8bit  

%state CADENA

// Declaración unificada de variables globales en el Lexer
%{
    String cadena = "";
    // Lista pública de errores léxicos que buscará GoliteFrame.java
    public java.util.ArrayList<com.olc1.reports.GoliteError> errors = new java.util.ArrayList<>();
%}

// Expresiones regulares básicas
BLANCOS = [\ \r\t\f\n]+
ENTERO = [0-9]+
DECIMAL = [0-9]+"."[0-9]+

// NUEVOS MACROS OBLIGATORIOS PARA LA FASE 1
ID = [a-zA-Z_][a-zA-Z0-9_]*

// Expresiones para ignorar comentarios de una línea (//) y multilínea (/* */)
COMENTARIO_LINEA = "//" [^\r\n]*
COMENTARIO_MULTI = "/*" [^*]* "*"+ ([^*/][^*]* "*"+)* "/"

%%

<YYINITIAL> {
    // 1. Palabras reservadas lógicas de GoLite (en minúsculas)
    "strconv.Atoi"       { return new Symbol(sym.ATOI, yyline, yycolumn, yytext()); }
    "strconv.ParseFloat" { return new Symbol(sym.PARSEFLOAT, yyline, yycolumn, yytext()); }
    "for"           { return new Symbol(sym.FOR, yyline, yycolumn, yytext()); }
    "if"            { return new Symbol(sym.IF, yyline, yycolumn, yytext()); }
    "else"          { return new Symbol(sym.ELSE, yyline, yycolumn, yytext()); }
    "true"          { return new Symbol(sym.TRUE, yyline, yycolumn, yytext()); }
    "false"         { return new Symbol(sym.FALSE, yyline, yycolumn, yytext()); }
    "break"         { return new Symbol(sym.BREAK, yyline, yycolumn, yytext()); }
    "continue"      { return new Symbol(sym.CONTINUE, yyline, yycolumn, yytext()); }
    "for"           { return new Symbol(sym.FOR, yyline, yycolumn, yytext()); }
    "if"            { return new Symbol(sym.IF, yyline, yycolumn, yytext()); }
    "else"          { return new Symbol(sym.ELSE, yyline, yycolumn, yytext()); }
    "reflect.TypeOf"     { return new Symbol(sym.REFLECT, yyline, yycolumn, yytext()); }
    "strconv.Atoi"       { return new Symbol(sym.ATOI, yyline, yycolumn, yytext()); }
    "strconv.ParseFloat" { return new Symbol(sym.PARSEFLOAT, yyline, yycolumn, yytext()); }
    
    
    // Mapeamos provisionalmente la función nativa de impresión
    "fmt.Println"   { return new Symbol(sym.IMPRIMIR, yyline, yycolumn, yytext()); }

    // 2. Signos y Operadores
    "=="            { return new Symbol(sym.IGUAL, yyline, yycolumn, yytext()); }
    "!="            { return new Symbol(sym.DIFERENTE, yyline, yycolumn, yytext()); }
    "<="            { return new Symbol(sym.MENORIGUAL, yyline, yycolumn, yytext()); }
    ">="            { return new Symbol(sym.MAYORIGUAL, yyline, yycolumn, yytext()); }
    "<"             { return new Symbol(sym.MENOR, yyline, yycolumn, yytext()); }
    ">"             { return new Symbol(sym.MAYOR, yyline, yycolumn, yytext()); }
    
    "&&"            { return new Symbol(sym.AND, yyline, yycolumn, yytext()); }
    "||"            { return new Symbol(sym.OR, yyline, yycolumn, yytext()); }
    "!"             { return new Symbol(sym.NOT, yyline, yycolumn, yytext()); }
    
    "="             { return new Symbol(sym.ASIGNAR, yyline, yycolumn, yytext()); }
    ";"             { return new Symbol(sym.FINCADENA, yyline, yycolumn, yytext()); }
    "+"             { return new Symbol(sym.MAS, yyline, yycolumn, yytext()); }
    "-"             { return new Symbol(sym.MENOS, yyline, yycolumn, yytext()); }
    "*"             { return new Symbol(sym.MULT, yyline, yycolumn, yytext()); }
    "/"             { return new Symbol(sym.DIV, yyline, yycolumn, yytext()); }
    "("             { return new Symbol(sym.PAR1, yyline, yycolumn, yytext()); }
    ")"             { return new Symbol(sym.PAR2, yyline, yycolumn, yytext()); }
    "{"             { return new Symbol(sym.LLAVE1, yyline, yycolumn, yytext()); }
    "}"             { return new Symbol(sym.LLAVE2, yyline, yycolumn, yytext()); }
    "%"             { return new Symbol(sym.MOD, yyline, yycolumn, yytext()); }

    // 3. Literales e Identificadores (¡El orden importa! Primero expresiones numéricas, luego IDs)
    {DECIMAL}       { return new Symbol(sym.DECIMAL, yyline, yycolumn, yytext()); }
    {ENTERO}        { return new Symbol(sym.ENTERO, yyline, yycolumn, yytext()); }
    {ID}            { return new Symbol(sym.ID, yyline, yycolumn, yytext()); }

    // 4. Ignorar Espacios en Blanco y Comentarios
    {BLANCOS}          { /* Ignorar */ }
    {COMENTARIO_LINEA} { /* Ignorar */ }
    {COMENTARIO_MULTI} { /* Ignorar */ }

    // 5. Cambio al estado de captura de cadenas de texto
    "\""            { yybegin(CADENA); cadena=""; }

    // 6. REGLA COMODÍN (FALLBACK) - Al puro final para capturar errores sin romper las otras reglas
    [^]  { 
           String desc = "Carácter inválido '" + yytext() + "'";
           System.err.println("Error léxico: " + desc + " en línea " + yyline + ", columna " + yycolumn);
           
           // Se inserta el objeto con el orden exacto de parámetros
           errors.add(new com.olc1.reports.GoliteError("Léxico", desc, yyline, yycolumn));
         }
}

<CADENA> {
    "\""            { String temporal = cadena; cadena = "";
                      yybegin(YYINITIAL);
                      return new Symbol(sym.CADENA, yyline, yycolumn, temporal); }
    [^\"]           { cadena += yytext(); }
}