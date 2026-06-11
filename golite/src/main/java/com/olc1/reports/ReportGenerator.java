package com.olc1.reports;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ReportGenerator {

    public static void generateErrorReport(List<GoliteError> errorList, String path) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset='UTF-8'>\n<title>Reporte de Errores - GoLite</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 30px; background-color: #fcfcfc; }\n");
        html.append("h2 { color: #d9534f; border-bottom: 2px solid #d9534f; padding-bottom: 10px; }\n");
        html.append("table { border-collapse: collapse; width: 100%; background-color: #fff; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }\n");
        html.append("th, td { border: 1px solid #e0e0e0; text-align: left; padding: 12px; }\n");
        html.append("th { background-color: #d9534f; color: white; font-weight: 600; }\n");
        html.append("tr:nth-child(even) { background-color: #f9f9f9; }\n");
        html.append("</style>\n</head>\n<body>\n");
        html.append("<h2>Reporte de Errores Sintácticos y Léxicos</h2>\n");
        html.append("<table>\n<tr><th>#</th><th>Tipo Error</th><th>Descripción</th><th>Línea</th><th>Columna</th></tr>\n");

        int i = 1;
        for (GoliteError error : errorList) {
            html.append("<tr>\n")
                .append("<td>").append(i++).append("</td>\n")
                .append("<td>").append(error.getType()).append("</td>\n")
                .append("<td>").append(error.getDescription()).append("</td>\n")
                .append("<td>").append(error.getLine()).append("</td>\n")
                .append("<td>").append(error.getColumn()).append("</td>\n")
                .append("</tr>\n");
        }

        html.append("</table>\n</body>\n</html>");

        try (FileWriter writer = new FileWriter(path)) {
            writer.write(html.toString());
        } catch (IOException e) {
            System.err.println("Error al guardar reporte de errores: " + e.getMessage());
        }
    }

    public static void generateTokenReport(List<GoliteToken> tokenList, String path) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset='UTF-8'>\n<title>Tabla de Tokens - GoLite</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 30px; background-color: #fcfcfc; }\n");
        html.append("h2 { color: #0275d8; border-bottom: 2px solid #0275d8; padding-bottom: 10px; }\n");
        html.append("table { border-collapse: collapse; width: 100%; background-color: #fff; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }\n");
        html.append("th, td { border: 1px solid #e0e0e0; text-align: left; padding: 12px; }\n");
        html.append("th { background-color: #0275d8; color: white; font-weight: 600; }\n");
        html.append("tr:nth-child(even) { background-color: #f9f9f9; }\n");
        html.append("</style>\n</head>\n<body>\n");
        html.append("<h2>Tabla de Tokens Reconocidos</h2>\n");
        html.append("<table>\n<tr><th>#</th><th>Token / Tipo</th><th>Lexema</th><th>Línea</th><th>Columna</th></tr>\n");

        int i = 1;
        for (GoliteToken token : tokenList) {
            html.append("<tr>\n")
                .append("<td>").append(i++).append("</td>\n")
                .append("<td>").append(token.getType()).append("</td>\n")
                .append("<td>").append(escapeHtml(token.getLexeme())).append("</td>\n")
                .append("<td>").append(token.getLine()).append("</td>\n")
                .append("<td>").append(token.getColumn()).append("</td>\n")
                .append("</tr>\n");
        }

        html.append("</table>\n</body>\n</html>");

        try (FileWriter writer = new FileWriter(path)) {
            writer.write(html.toString());
        } catch (IOException e) {
            System.err.println("Error al guardar tabla de tokens: " + e.getMessage());
        }
    }

    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }
}