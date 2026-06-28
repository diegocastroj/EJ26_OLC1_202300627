package com.olc1.views;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.File;
import java.util.ArrayList;

import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;


import com.olc1.Lexer;
import com.olc1.TokenTrackerLexer;
import com.olc1.parser;
import com.olc1.ast.ASTNode;
import com.olc1.ast.stm.BreakException;
import com.olc1.ast.stm.ContinueException;
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
    private InterpreterVisitor interprete;

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
        
        editorCodigo.setLineWrap(false); 
        
        JScrollPane panelDesplazamientoEditor = new JScrollPane(editorCodigo);
        panelDesplazamientoEditor.setBorder(BorderFactory.createEmptyBorder()); // Elimina bordes blancos nativos

        JTextArea numerosLinea = new JTextArea("1");
        numerosLinea.setBackground(COLOR_DIVISOR); // Usamos tu mismo color de divisor de fondo
        numerosLinea.setForeground(Color.LIGHT_GRAY);
        numerosLinea.setEditable(false);
        numerosLinea.setFocusable(false);
        numerosLinea.setFont(editorCodigo.getFont()); // Mismo tipo y tamaño de letra
        numerosLinea.setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 10)); 

        panelDesplazamientoEditor.setRowHeaderView(numerosLinea);

        Runnable actualizarNumeros = () -> {
            SwingUtilities.invokeLater(() -> {
                int totalLineas = editorCodigo.getLineCount();
                StringBuilder textoNumeros = new StringBuilder();
                for (int i = 1; i <= totalLineas; i++) {
                    textoNumeros.append(i).append("\n"); // Usar \n directo suele ser más estable en JTextArea
                }
                numerosLinea.setText(textoNumeros.toString());
            });
        };

        editorCodigo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarNumeros.run(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarNumeros.run(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarNumeros.run(); }
        });

        // Escuchador 2 (Refuerzo): Detecta cuando el cursor se mueve, seleccionas texto, 
        // o cargas un archivo nuevo desde el menú. ¡Esto lo hace a prueba de fallos!
        editorCodigo.addCaretListener(e -> actualizarNumeros.run());

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
        panelDivisor.setResizeWeight(0.7); 
        panelDivisor.setOneTouchExpandable(true);  
        panelDivisor.setContinuousLayout(true); 
        panelDivisor.setDividerSize(8); // Un divisor un poco más grueso para que sea fácil de arrastrar
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
        barraMenu.onOpen(e -> abrirArchivo());
        barraMenu.onSave(e -> guardarArchivo());
        barraMenu.onExit(e -> System.exit(0));
        barraMenu.onTokens(e -> abrirReporteHtml("Reporte_Tokens.html"));
        barraMenu.onAST(e -> abrirReporteHtml("AST_Graph.png"));
        barraMenu.onErrors(e -> { ; 
            abrirReporteHtml("Reporte_Errors.html");
        });

        barraMenu.onTablaSimbolos(e -> abrirReporteHtml("TablaSimbolos.html"));
        barraMenu.onAbout(e -> JOptionPane.showMessageDialog(
                this,
                "GoLite Compilador & IDE\nDesarrollador: Diego José Castro Lux\nUSAC 2026",
                "Acerca de",
                JOptionPane.INFORMATION_MESSAGE));
    }

   private void ejecutar() {
        try {
            analizadorLexico = new Lexer(new BufferedReader(new StringReader(editorCodigo.getText())));
            rastreadorTokens = new TokenTrackerLexer(analizadorLexico);
            analizadorSintactico = new parser(rastreadorTokens);

            java_cup.runtime.Symbol simboloRaiz = analizadorSintactico.parse();
            limpiarConsola();

            // INICIALIZAMOS EL INTÉRPRETE SIEMPRE
            interprete = new InterpreterVisitor();

            // SOLO EJECUTAMOS SI EL ÁRBOL EXISTE (simboloRaiz.value es el AST)
            if (simboloRaiz != null && simboloRaiz.value != null) {
                try {
                    ASTNode nodoAst = (ASTNode) simboloRaiz.value;
                    interprete.Visit(nodoAst);
                } catch (BreakException e) {
                    interprete.output += ">> [ERROR SEMÁNTICO]: sentencia break fuera de ciclo\n";
                } catch (ContinueException e) {
                    interprete.output += ">> [ERROR SEMÁNTICO]: sentencia continue fuera de ciclo\n";
                } catch (Exception e) {
                    if (e.getMessage() != null) {
                        interprete.output += ">> [ERROR SEMÁNTICO O DE EJECUCIÓN]: " + e.getMessage() + "\n";
                    }
                }
            } else {
                areaConsola.append("[COMPILACIÓN] Árbol AST no generado completamente.\n");
            }

            // RECOLECCIÓN SEGURA
            ArrayList<GoliteError> listaErrores = new ArrayList<>();
            listaErrores.addAll(analizadorLexico.errors);
            listaErrores.addAll(analizadorSintactico.erroresSintacticos);
            listaErrores.addAll(interprete.erroresSemanticos);

            if (simboloRaiz != null && simboloRaiz.value instanceof ASTNode) {
                com.olc1.reports.GeneradorAST genAST = new com.olc1.reports.GeneradorAST();
                genAST.generar((ASTNode) simboloRaiz.value, "AST_Graph.dot", "AST_Graph.png");
            }
            
            ReportGenerator.generateTokenReport(rastreadorTokens.tokens, "Reporte_Tokens.html");
            ReportGenerator.generateErrorReport(listaErrores, "Reporte_Errors.html");
            com.olc1.reports.GeneradorHTML.generarTablaSimbolos(interprete.tablaSimbolos);

            areaConsola.append(interprete.output);
            areaConsola.append("\n\n[ÉXITO] Ejecución de fases finalizada.");
            
        } catch (Exception e) {
            areaConsola.append("\n[ERROR FATAL]: " + (e.getMessage() != null ? e.getMessage() : "Error desconocido") + "\n");
        }
        areaConsola.setCaretPosition(areaConsola.getDocument().getLength());
        editorCodigo.requestFocus();
    }

    private void abrirArchivo() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Abrir archivo GoLite");
        
        // Filtro estricto para archivos .glt
        FileNameExtensionFilter filtro = new FileNameExtensionFilter("Archivos GoLite (*.glt)", "glt");
        selector.setFileFilter(filtro);

        int resultado = selector.showOpenDialog(this);
        
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = selector.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(archivoSeleccionado))) {
                editorCodigo.read(br, null); 
                JOptionPane.showMessageDialog(this, "Archivo cargado con éxito.", "Abrir", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al leer el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    private void guardarArchivo() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar archivo GoLite");
        
        FileNameExtensionFilter filtro = new FileNameExtensionFilter("Archivos GoLite (*.glt)", "glt");
        selector.setFileFilter(filtro);

        int resultado = selector.showSaveDialog(this);
        
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivoGuardar = selector.getSelectedFile();
            
            // Validación: Si el usuario no escribió .glt, se lo agregamos automáticamente
            if (!archivoGuardar.getName().toLowerCase().endsWith(".glt")) {
                archivoGuardar = new File(archivoGuardar.getAbsolutePath() + ".glt");
            }

            try (FileWriter fw = new FileWriter(archivoGuardar)) {
                editorCodigo.write(fw);
                JOptionPane.showMessageDialog(this, "Archivo guardado con éxito.", "Guardar", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
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