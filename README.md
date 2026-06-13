# EJ26_OLC1_202300627
Organización de Lenguajes y Compiladores 1

# GoLite IDE - Compilador e Intérprete (Fase 1)

## 📌 Descripción General
GoLite IDE es un entorno de desarrollo integrado (IDE) y un intérprete construido en Java para procesar **GoLite**, un subconjunto del lenguaje de programación Go. 

El proyecto consta de un motor de análisis léxico y sintáctico ascendente (LALR) que valida la estructura del código, genera un Árbol de Sintaxis Abstracta (AST) y ejecuta las instrucciones en tiempo real. Está diseñado para manejar tipos estáticos, control de flujo avanzado y un estricto control de ámbitos (scopes) para variables.

## 🚀 Características Principales
* **Análisis Léxico y Sintáctico:** Implementado con JFlex y Java CUP, incluyendo recuperación de errores mediante *Modo Pánico*.
* **Ejecución Semántica:** Utiliza el Patrón de Diseño **Visitor** para recorrer el AST, validar operaciones matemáticas, verificar compatibilidad de tipos y gestionar entornos de memoria dinámicos (scopes).
* **Azúcar Sintáctico:** Soporte nativo para operadores de incremento/decremento (`++`, `--`) y asignaciones compuestas (`+=`, `-=`) dentro de ciclos `for`.
* **Entorno Gráfico (GUI):** Interfaz amigable construida con Java Swing (estilo Dark Mode) que permite escribir código, visualizar la salida de la consola y gestionar archivos de extensión `.glt`.
* **Generación de Reportes:** Exportación automática de la tabla de tokens y el log de errores (léxicos, sintácticos y semánticos) a formatos HTML interactivos.

## 🛠️ Tecnologías Utilizadas
* **Lenguaje Base:** Java (JDK 17 o superior recomendado)
* **Gestor de Dependencias:** Apache Maven
* **Analizador Léxico:** JFlex
* **Analizador Sintáctico:** Java CUP
* **Interfaz Gráfica:** Java Swing

## ⚙️ Requisitos Previos
Para compilar y ejecutar este proyecto en tu entorno local, necesitas tener instalado:
* [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/)
* [Apache Maven](https://maven.apache.org/download.cgi) configurado en las variables de entorno del sistema.

**⚠️ IMPORTANTE - Configuración de Maven:**
Para que los comandos de compilación funcionen correctamente, es obligatorio que la carpeta `bin` de tu instalación de Apache Maven esté registrada en las **Variables de Entorno** (`PATH`) de tu sistema operativo. Puedes verificar tu instalación ejecutando `mvn -version` en tu terminal.

## 📖 Instrucciones de Ejecución

### 1. Limpieza y Compilación
Abre tu terminal en el directorio raíz del proyecto (donde se encuentra el archivo `pom.xml`) y ejecuta el siguiente comando para que Maven descargue las dependencias, procese los archivos de JFlex/CUP y compile el código fuente:

```bash
mvn clean compile
```

### 2. Ejecucion del IDE
Una vez compilado correctamente, puedes levantar la interfaz gráfica ejecutando la clase principal del proyecto. Dependiendo de la configuración de tu IDE (IntelliJ, Eclipse, VS Code) puedes ejecutar el archivo principal, o utilizar el siguiente comando de Maven en la terminal:

```bash
mvn exec:java "-Dexec.mainClass=com.olc1.Main"