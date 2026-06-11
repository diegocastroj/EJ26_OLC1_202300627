package com.olc1.views;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.File;
import java.util.ArrayList;

import com.olc1.Lexer;
import com.olc1.TokenTrackerLexer;
import com.olc1.parser;
import com.olc1.ast.ASTNode;
import com.olc1.reports.GoliteError;
import com.olc1.reports.ReportGenerator;
import com.olc1.visitor.interpreter.InterpreterVisitor;

public class GoliteFrame extends JFrame {
    // Componentes gráficos del entorno de desarrollo
    private final JTextArea editorCodigo;
    private final JTextArea areaConsola;
    private Lexer analizadorLexico;
    private parser analizadorSintactico;
    private TokenTrackerLexer rastreadorTokens;

    // Paleta de colores planos estilo VS Code
    private final Color FONDO_OSCURO = new Color(30, 30, 30);       // #1E1E1E
    private final Color FONDO_PANEL = new Color(37, 37, 38);        // #252526
    private final Color COLOR_TEXTO = new Color(220, 220, 220);      // #DCDCDC
    private final Color SALIDA_CONSOLA = new Color(156, 220, 254);   // #9CDCFE (Celeste plano)
    private final Color COLOR_DIVISOR = new Color(45, 45, 45);       // #2D2D2D

    public GoliteFrame() {
        setTitle("GoLite IDE");
        setMinimumSize(new Dimension(700, 500));
        setSize(new Dimension(1200, 750));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. Configuración del área de edición de texto
        editorCodigo = new JTextArea();
        editorCodigo.setBackground(FONDO_OSCURO);
        editorCodigo.setForeground(COLOR_TEXTO);
        editorCodigo.setFont(new Font("Consolas", Font.PLAIN, 14));
        editorCodigo.setCaretColor(Color.WHITE);
        editorCodigo.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JScrollPane panelDesplazamientoEditor = new JScrollPane(editorCodigo);
        panelDesplazamientoEditor.setBorder(BorderFactory.createEmptyBorder()); // Elimina bordes blancos nativos

        // 2. Configuración del área de la terminal / consola
        areaConsola = new JTextArea();
        areaConsola.setBackground(FONDO_PANEL);
        areaConsola.setForeground(SALIDA_CONSOLA);
        areaConsola.setFont(new Font("Consolas", Font.PLAIN, 13));
        areaConsola.setEditable(false);
        areaConsola.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JScrollPane panelDesplazamientoConsola = new JScrollPane(areaConsola);
        panelDesplazamientoConsola.setBorder(BorderFactory.createEmptyBorder()); // Elimina bordes blancos nativos

        // 3. Creación del panel divisor móvil
        JSplitPane panelDivisor = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelDesplazamientoEditor, panelDesplazamientoConsola);
        panelDivisor.setDividerLocation(460);
        panelDivisor.setDividerSize(5);
        panelDivisor.setBorder(BorderFactory.createEmptyBorder());
        panelDivisor.setBackground(COLOR_DIVISOR);

        // 4. Panel contenedor del layout principal
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(FONDO_OSCURO);
        panelPrincipal.add(panelDivisor, BorderLayout.CENTER);
        add(panelPrincipal);

        // 5. Instanciar la barra de menús y aplicar diseño en caliente
        GoliteMenuBar barraMenu = new GoliteMenuBar();
        personalizarAspectoMenu(barraMenu);
        setJMenuBar(barraMenu);

        conectarAcciones(barraMenu);
        limpiarConsola();

        setVisible(true);
        editorCodigo.requestFocus();
    }

    // Procesa de forma dinámica tanto menús como botones de la barra superior
    private void personalizarAspectoMenu(GoliteMenuBar mb) {
        mb.setBackground(FONDO_PANEL);
        mb.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_DIVISOR));
        
        for (java.awt.Component componente : mb.getComponents()) {
            if (componente instanceof JMenu menu) {
                menu.setForeground(COLOR_TEXTO);
                menu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            } 
            else if (componente instanceof JButton boton) {
                boton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                
                // Resalta el botón de ejecución con un verde estilizado si su texto coincide
                if (boton.getText().equals("Ejecutar")) {
                    boton.setForeground(new Color(106, 195, 121)); 
                    boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else {
                    boton.setForeground(COLOR_TEXTO);
                }
                
                boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            }
        }
    }

    // Enlaza los manejadores de eventos con los listeners públicos de la barra
    private void conectarAcciones(GoliteMenuBar barraMenu) {
        barraMenu.onRun(e -> ejecutar());
        barraMenu.onClean(e -> limpiarConsola());
        barraMenu.onNew(e -> editorCodigo.setText("// Código de Prueba GoLite\nfmt.Println(10 % 3);\n"));
        barraMenu.onExit(e -> System.exit(0));
        barraMenu.onTokens(e -> abrirReporteHtml("Reporte_Tokens.html"));
        barraMenu.onErrors(e -> { 
            mostrarErrores(); 
            abrirReporteHtml("Reporte_Errores.html");
        });
        barraMenu.onAbout(e -> JOptionPane.showMessageDialog(
                this,
                "GoLite Compilador & IDE\nDesarrollador: Diego José Castro Lux\nUSAC 2026",
                "Acerca de",
                JOptionPane.INFORMATION_MESSAGE));
    }

    // Orquesta las fases de análisis léxico, sintáctico y la ejecución del intérprete
    private void ejecutar() {
        try {
            analizadorLexico = new Lexer(new BufferedReader(new StringReader(editorCodigo.getText())));
            rastreadorTokens = new TokenTrackerLexer(analizadorLexico);
            analizadorSintactico = new parser(rastreadorTokens);

            java_cup.runtime.Symbol simboloRaiz = analizadorSintactico.parse();
            limpiarConsola();

            // Consolidación de alertas léxicas y sintácticas para el reporte HTML
            ArrayList<GoliteError> listaErrores = new ArrayList<>();
            listaErrores.addAll(analizadorLexico.errors);
            listaErrores.addAll(analizadorSintactico.errors);

            ReportGenerator.generateTokenReport(rastreadorTokens.tokens, "Reporte_Tokens.html");
            ReportGenerator.generateErrorReport(listaErrores, "Reporte_Errores.html");

            // Validar si el análisis contiene fallas antes de pasar al Intérprete
            if (!analizadorLexico.errors.isEmpty() || !analizadorSintactico.errors.isEmpty() || simboloRaiz == null || simboloRaiz.value == null) {
                areaConsola.append("[COMPILACIÓN FALLIDA] Se detectaron errores en el código fuente.\n");
                areaConsola.append("[INFORMACIÓN] Abre el menú 'Reportes' para examinar las tablas detalladas.\n");
                return;
            }

            // Recuperación del nodo raíz e inicio del recorrido conceptual del AST
            ASTNode nodoAst = (ASTNode) simboloRaiz.value;
            InterpreterVisitor interprete = new InterpreterVisitor();
            interprete.Visit(nodoAst);

            areaConsola.append(interprete.output);
            areaConsola.append("\n\n[ÉXITO] Ejecución finalizada correctamente.");
            
        } catch (Exception e) {
            areaConsola.append("\n[ERROR FATAL EN TIEMPO DE EJECUCIÓN]: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
        areaConsola.setCaretPosition(areaConsola.getDocument().getLength());
        editorCodigo.requestFocus();
    }

    // Vuelca la información de los búferes de error directo en la consola gráfica
    private void mostrarErrores() {
        limpiarConsola();
        if (analizadorLexico == null || analizadorSintactico == null) {
            areaConsola.append("[INFORMACIÓN] Búfer de ejecución vacío.\n");
            return;
        }

        areaConsola.append("--- LOG DE ERRORES DETECTADOS ---\n\n");
        areaConsola.append("[Errores Léxicos]:\n");
        for (GoliteError error : analizadorLexico.errors) {
            areaConsola.append(" -> " + error.toString() + "\n");
        }

        areaConsola.append("\n[Errores Sintácticos]:\n");
        for (GoliteError error : analizadorSintactico.errors) {
            areaConsola.append(" -> " + error.toString() + "\n");
        }
    }

    // Despacha al sistema operativo la petición de abrir el navegador de internet
    private void abrirReporteHtml(String nombreArchivo) {
        try {
            File archivo = new File(nombreArchivo);
            if (archivo.exists()) {
                Desktop.getDesktop().browse(archivo.toURI());
            } else {
                JOptionPane.showMessageDialog(this, "Ejecuta el código primero para generar el archivo.", "Sin Datos", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void limpiarConsola() {
        areaConsola.setText("go run main.go\n\n");
    }
}