package analisis;

// importaciones
import java_cup.runtime.Symbol;

%%

// Código de usuario

%{
    String cadena = "";
%}

%init{
    yyline = 1;
    yycolumn = 1;
%init}


%cup 
%class Scanner
%public
%line 
%column 
%8bit  

%state CADENA

// Expresiones regulares básicas
BLANCOS = [\ \r\t\f\n]+
ENTERO = [0-9]+
DECIMAL = [0-9]+"."[0-9]+

// NUEVOS MACROS OBLIGATORIOS PARA LA FASE 1
// Un ID puede empezar con letra o guion bajo, seguido de letras, números o guiones bajos
ID = [a-zA-Z_][a-zA-Z0-9_]*

// Expresiones para ignorar comentarios de una línea (//) y multilínea (/* */)
COMENTARIO_LINEA = "//" [^\r\n]*
COMENTARIO_MULTI = "/*" [^*]* "*"+ ([^*/][^*]* "*"+)* "/"

%%

<YYINITIAL> {
    // 1. Palabras reservadas lógicas de GoLite (en minúsculas)
    "true"          { return new Symbol(sym.TRUE, yyline, yycolumn, yytext()); }
    "false"         { return new Symbol(sym.FALSE, yyline, yycolumn, yytext()); }
    
    // Mapeamos provisionalmente la función nativa de impresión
    "fmt.Println"   { return new Symbol(sym.IMPRIMIR, yyline, yycolumn, yytext()); }

    // 2. Signos y Operadores
    "="             { return new Symbol(sym.ASIGNAR, yyline, yycolumn, yytext()); }
    ";"             { return new Symbol(sym.FINCADENA, yyline, yycolumn, yytext()); }
    "+"             { return new Symbol(sym.MAS, yyline, yycolumn, yytext()); }
    "-"             { return new Symbol(sym.MENOS, yyline, yycolumn, yytext()); }
    "*"             { return new Symbol(sym.MULT, yyline, yycolumn, yytext()); }
    "/"             { return new Symbol(sym.DIV, yyline, yycolumn, yytext()); }
    "("             { return new Symbol(sym.PAR1, yyline, yycolumn, yytext()); }
    ")"             { return new Symbol(sym.PAR2, yyline, yycolumn, yytext()); }

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
}

<CADENA> {
    "\""            { String temporal = cadena; cadena = "";
                      yybegin(YYINITIAL);
                      return new Symbol(sym.CADENA, yyline, yycolumn, temporal); }
    [^\"]           { cadena += yytext(); }
}