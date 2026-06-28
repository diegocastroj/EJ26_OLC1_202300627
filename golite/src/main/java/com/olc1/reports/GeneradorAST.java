package com.olc1.reports;

import com.olc1.ast.ASTNode;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

public class GeneradorAST {

    private int contador;
    private StringBuilder dot;
    private IdentityHashMap<Object, Integer> visitados;

    public void generar(ASTNode raiz, String rutaDot, String rutaPng) {
        contador = 0;
        dot = new StringBuilder();
        visitados = new IdentityHashMap<>();

        dot.append("digraph AST {\n");
        dot.append("rankdir=TB;\n");
        dot.append("nodesep=0.6;\n");
        dot.append("ranksep=0.8;\n");

        dot.append("""
            node [
                shape=box,
                style="rounded",
                fontname="Arial",
                fontsize=12
            ];
        """);

        recorrer(raiz);

        dot.append("}\n");

        try (FileWriter fw = new FileWriter(rutaDot)) {
            fw.write(dot.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "dot",
                    "-Tpng",
                    rutaDot,
                    "-o",
                    rutaPng
            );

            Process p = pb.start();
            int exit = p.waitFor();

            if (exit != 0) {
                System.err.println("Graphviz falló");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int recorrer(Object nodo) {
        if (nodo == null) return -1;

        if (visitados.containsKey(nodo)) {
            return visitados.get(nodo);
        }

        int miId = contador++;
        visitados.put(nodo, miId);

        String etiqueta = obtenerEtiqueta(nodo);

        dot.append("node")
           .append(miId)
           .append(" [label=\"")
           .append(escape(etiqueta))
           .append("\"];\n");

        procesarAtributos(nodo, miId);
        procesarHijos(nodo, miId);

        return miId;
    }

    private void procesarAtributos(Object nodo, int miId) {
        Class<?> clase = nodo.getClass();

        while (clase != null && clase != Object.class) {
            for (Field f : clase.getDeclaredFields()) {
                try {
                    f.setAccessible(true);
                    Object valor = f.get(nodo);

                    if (esPrimitivo(valor) && !ignorarCampo(f.getName())) {
                        int id = contador++;

                        String texto = "<" + f.getName() + ", " + valor + ">";

                        dot.append("node")
                           .append(id)
                           .append(" [label=\"")
                           .append(escape(texto))
                           .append("\"];\n");

                        dot.append("node")
                           .append(miId)
                           .append(" -> node")
                           .append(id)
                           .append(";\n");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            clase = clase.getSuperclass();
        }
    }

    private void procesarHijos(Object nodo, int miId) {
        Class<?> clase = nodo.getClass();

        while (clase != null && clase != Object.class) {
            for (Field f : clase.getDeclaredFields()) {
                try {
                    f.setAccessible(true);
                    Object valor = f.get(nodo);

                    if (valor instanceof ASTNode) {
                        int hijo = recorrer(valor);

                        dot.append("node")
                           .append(miId)
                           .append(" -> node")
                           .append(hijo)
                           .append(";\n");
                    }

                    else if (valor instanceof List<?>) {
                        List<?> lista = (List<?>) valor;

                        if (!lista.isEmpty()) {
                            int listId = contador++;

                            dot.append("node")
                               .append(listId)
                               .append(" [label=\"")
                               .append(f.getName())
                               .append("\"];\n");

                            dot.append("node")
                               .append(miId)
                               .append(" -> node")
                               .append(listId)
                               .append(";\n");

                            for (Object item : lista) {
                                if (item instanceof ASTNode) {
                                    int hijo = recorrer(item);

                                    dot.append("node")
                                       .append(listId)
                                       .append(" -> node")
                                       .append(hijo)
                                       .append(";\n");
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            clase = clase.getSuperclass();
        }
    }

    private boolean esPrimitivo(Object o) {
        return o instanceof String ||
               o instanceof Integer ||
               o instanceof Double ||
               o instanceof Boolean;
    }

    private boolean ignorarCampo(String nombre) {
        return nombre.contains("line")
            || nombre.contains("column")
            || nombre.contains("left")
            || nombre.contains("right");
    }

    private String obtenerEtiqueta(Object nodo) {
        String nombre = nodo.getClass().getSimpleName();

        if (nombre.contains("Statement")) return "INSTRUCCION";
        if (nombre.contains("Expression")) return "EXPRESION";

        return nombre;
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}