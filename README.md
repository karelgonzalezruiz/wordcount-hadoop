# WordCount con Hadoop MapReduce

Implementación de **WordCount en Java** para el conteo de palabras mediante **Apache Hadoop MapReduce**, con almacenamiento de entrada y salida en **HDFS** y administración de recursos mediante **YARN**.

Este proyecto modifica el WordCount tradicional para que el conteo **no distinga entre mayúsculas y minúsculas** y **no considere los signos de puntuación como parte de las palabras**. Se conservan las letras acentuadas y la `ñ`.

## Objetivo

Procesar un archivo de texto en HDFS y obtener la frecuencia de cada palabra bajo las siguientes reglas:

- Convertir a minúsculas: `Hadoop`, `HADOOP` y `hadoop` se cuentan como `hadoop`.
- Utilizar la puntuación como separador: `datos,`, `datos.` y `datos` se cuentan como `datos`.
- Conservar caracteres del idioma español, como `á`, `é` y `ñ`.
- Sumar las apariciones de cada palabra mediante las etapas **Mapper** y **Reducer**.

**Ejemplo**

Entrada:

```text
Hadoop, HADOOP! hadoop.
Datos, DATOS datos.
```

Salida esperada:

```text
datos	3
hadoop	3
```

> Los acentos se conservan: `mas` y `más` siguen siendo palabras diferentes. Los signos de puntuación se usan como separadores, por lo que no se unen palabras situadas a ambos lados de un signo.

## Tecnologías utilizadas

- **Java** (configuración de compilación del proyecto: Java 8).
- **Apache Hadoop 3.5.0**: HDFS, YARN y MapReduce.
- **Apache Maven** para compilar y empaquetar.
- **IntelliJ IDEA** como entorno de desarrollo.
- **Ubuntu sobre WSL2** para la ejecución local de Hadoop.

## Estructura del repositorio

```text
wordcount-hadoop/
├── .gitignore
├── README.md
├── pom.xml
├── src/
│   └── main/
│       └── java/
│           └── org/
│               └── karel/
│                   ├── WC_Mapper.java
│                   ├── WC_Reducer.java
│                   └── WC_Runner.java
├── datos/
│   └── input.txt
├── resultados/
│   └── resultado_wordcount.txt
└── evidencias/
    ├── 01_Compilacion_Maven_BUILD_SUCCESS.png
    ├── 02_HDFS_Entrada_input_txt.png
    ├── 03_HDFS_Salida_SUCCESS_y_part_00000.png
    ├── 04_Conteo_Parte_1_A_C.png
    ├── 05_Conteo_Parte_2_D_F.png
    ├── 06_Conteo_Parte_3_F_N.png
    ├── 07_Conteo_Parte_4_M_S.png
    └── 08_Conteo_Parte_5_S_Z.png
```

Las carpetas `.idea/` y `target/` se excluyen del repositorio mediante `.gitignore`, porque contienen configuraciones locales o archivos generados. El archivo JAR se reconstruye con Maven.

## Funcionamiento del programa

### Mapper (`WC_Mapper.java`)

Convierte cada línea a minúsculas y separa las palabras mediante una expresión regular que reconoce letras y números Unicode:

```java
String line = value.toString().toLowerCase(Locale.ROOT);
String[] tokens = line.split("[^\\p{L}\\p{N}]+");

for (String token : tokens) {
    if (!token.isEmpty()) {
        word.set(token);
        output.collect(word, ONE);
    }
}
```

La expresión regular `[^\p{L}\p{N}]+` considera separadores las secuencias de caracteres que no son letras ni números. Cada palabra válida produce un par `(palabra, 1)`.

### Reducer (`WC_Reducer.java`)

Agrupa las palabras iguales y suma sus apariciones para obtener el conteo total. El programa también utiliza el Reducer como **Combiner** para realizar agregaciones parciales.

### Runner (`WC_Runner.java`)

Configura el trabajo MapReduce: clases Mapper y Reducer, formatos de entrada y salida y rutas en HDFS.

## Compilación y ejecución

**Requisitos:** Java y Maven instalados, además de una instalación funcional de Hadoop con HDFS y YARN iniciados.

### 1. Compilar el proyecto

Desde la raíz del repositorio:

```bash
mvn clean package
```

Maven genera el archivo:

```text
target/WC_count-1.0-SNAPSHOT.jar
```

### 2. Cargar el archivo de entrada en HDFS

```bash
hdfs dfs -mkdir -p /entrada
hdfs dfs -put datos/input.txt /entrada/
```

Si `/entrada/input.txt` ya existe, no es necesario volver a cargarlo.

### 3. Ejecutar WordCount

```bash
hadoop jar target/WC_count-1.0-SNAPSHOT.jar org.karel.WC_Runner /entrada /salida_final
```

> **Importante:** Hadoop requiere que la carpeta de salida no exista antes de ejecutar el trabajo. Si `/salida_final` ya existe, utiliza una ruta nueva, por ejemplo `/salida_nueva`.

### 4. Consultar el resultado

```bash
hdfs dfs -ls /salida_final
hdfs dfs -cat /salida_final/part-00000
```

En la ejecución documentada, Hadoop generó `_SUCCESS` y `part-00000` dentro de `/salida_final`.

### 5. Descargar el resultado desde HDFS

```bash
hdfs dfs -getmerge /salida_final resultado_wordcount.txt
```

## Resultados obtenidos

Se procesó [`datos/input.txt`](datos/input.txt) con el código modificado. El resultado está disponible en [`resultados/resultado_wordcount.txt`](resultados/resultado_wordcount.txt).

| Indicador | Resultado |
|---|---:|
| Palabras diferentes | **193** |
| Apariciones totales | **423** |
| Frecuencia de `datos` | **21** |
| Frecuencia de `procesamiento` | **6** |

Las palabras aparecen en minúsculas y los signos de puntuación no forman parte de las claves del conteo.

## Evidencias

### Compilación en IntelliJ IDEA

![Compilación Maven exitosa](evidencias/01_Compilacion_Maven_BUILD_SUCCESS.png)

### Archivo de entrada cargado en HDFS

![Archivo input.txt en HDFS](evidencias/02_HDFS_Entrada_input_txt.png)

### Archivos de salida en HDFS

![Archivos SUCCESS y part-00000 en HDFS](evidencias/03_HDFS_Salida_SUCCESS_y_part_00000.png)

### Conteo de palabras

Las capturas de la terminal muestran el resultado ordenado alfabéticamente:

- [Conteo, parte 1 (A–C)](evidencias/04_Conteo_Parte_1_A_C.png)
- [Conteo, parte 2 (D–F)](evidencias/05_Conteo_Parte_2_D_F.png)
- [Conteo, parte 3 (F–N)](evidencias/06_Conteo_Parte_3_F_N.png)
- [Conteo, parte 4 (M–S)](evidencias/07_Conteo_Parte_4_M_S.png)
- [Conteo, parte 5 (S–Z)](evidencias/08_Conteo_Parte_5_S_Z.png)

## Nota

Este repositorio documenta una **actividad académica de procesamiento masivo de datos**. Para reproducir el trabajo se requiere una instalación funcional de Apache Hadoop; no basta con ejecutar las clases Java como una aplicación convencional.
