package com.olc1.reports;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class GeneradorHTML {

    public static void generarTablaSimbolos(ArrayList<Simbolo> simbolos) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'><title>Tabla de Símbolos</title>");
        html.append("<style>table {width: 80%; margin: 20px auto; border-collapse: collapse; font-family: Arial, sans-serif;} th, td {border: 1px solid #444; padding: 10px; text-align: center;} th {background-color: #2c3e50; color: white;} h1 {text-align: center; font-family: Arial, sans-serif; color: #2c3e50;} tr:nth-child(even) {background-color: #f2f2f2;}</style>");
        html.append("</head><body>");
        html.append("<h1>Reporte de Tabla de Símbolos - GoLite</h1>");
        html.append("<table><tr><th>ID (Nombre)</th><th>Tipo de Símbolo</th><th>Tipo de Dato</th><th>Ámbito</th><th>Línea</th><th>Columna</th>");
        
        for (Simbolo s : simbolos) {
            html.append("<tr>");
            html.append("<td>").append(s.id).append("</td>");
            html.append("<td>").append(s.tipoSimbolo).append("</td>");
            html.append("<td>").append(s.tipoDato).append("</td>");
            html.append("<td>").append(s.ambito).append("</td>");
            html.append("<td>").append(s.linea).append("</td>");
            html.append("<td>").append(s.columna).append("</td>");
            html.append("</tr>");
        }
        
        html.append("</table></body></html>");
        
        escribirArchivo("TablaSimbolos.html", html.toString());
    }

    public static void generarTablaErrores(ArrayList<GoliteError> erroresSin, ArrayList<GoliteError> erroresSem) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'><title>Tabla de Errores</title>");
        html.append("<style>table {width: 80%; margin: 20px auto; border-collapse: collapse; font-family: Arial;} th, td {border: 1px solid #444; padding: 10px; text-align: center;} th {background-color: #c0392b; color: white;} h1 {text-align: center; font-family: Arial; color: #c0392b;}</style>");
        html.append("</head><body>");
        html.append("<h1>Reporte de Errores - GoLite</h1>");
        html.append("<table><tr><th>Tipo</th><th>Descripción</th><th>Línea</th><th>Columna</th></tr>");
        
        // Escribir errores Sintácticos
        for (GoliteError e : erroresSin) {
            html.append("<tr><td>").append(e.getType()).append("</td><td>").append(e.getDescription()).append("</td><td>").append(e.getLine()).append("</td><td>").append(e.getColumn()).append("</td></tr>");
        }
        // Escribir errores Semánticos
        for (GoliteError e : erroresSem) {
            html.append("<tr><td>").append(e.getType()).append("</td><td>").append(e.getDescription()).append("</td><td>").append(e.getLine()).append("</td><td>").append(e.getColumn()).append("</td></tr>");
        }
        
        html.append("</table></body></html>");
        escribirArchivo("TablaErrores.html", html.toString());
    }

    private static void escribirArchivo(String nombreArchivo, String contenido) {
        try (FileWriter writer = new FileWriter(nombreArchivo)) {
            writer.write(contenido);
            System.out.println("¡Reporte " + nombreArchivo + " generado con éxito!");
        } catch (IOException e) {
            System.err.println("Error al generar " + nombreArchivo + ": " + e.getMessage());
        }
    }
}