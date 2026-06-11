package com.olc1;

import java_cup.runtime.Scanner;
import java_cup.runtime.Symbol;
import com.olc1.reports.GoliteToken;
import java.util.ArrayList;

public class TokenTrackerLexer implements Scanner {
    private final Lexer actualLexer;
    public final ArrayList<GoliteToken> tokens = new ArrayList<>();

    public TokenTrackerLexer(Lexer actualLexer) {
        this.actualLexer = actualLexer;
    }

    @Override
    public Symbol next_token() throws Exception {
        Symbol s = actualLexer.next_token();
        if (s != null && s.sym != sym.EOF) {
            String tokenName = getSymbolName(s.sym);
            String lexeme = (s.value != null) ? s.value.toString() : "";
            // s.left es la línea y s.right la columna calculadas por tu Lexer
            tokens.add(new GoliteToken(tokenName, lexeme, s.left, s.right));
        }
        return s;
    }

    private String getSymbolName(int id) {
        try {
            // CUP genera automáticamente un arreglo de nombres de terminales
            return sym.terminalNames[id];
        } catch (Exception e) {
            return "TOKEN_" + id;
        }
    }
}