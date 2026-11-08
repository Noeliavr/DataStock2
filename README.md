# DataStock – UT1 (Ficheros)

**Módulo:** Acceso a Datos · 2.º DAM · IES Isidra de Guzmán · Curso 2026–2027
**Lenguaje:** Java 21 · **Construcción:** Maven · **Interfaz:** consola

Aplicación de consola que gestiona un inventario de productos en memoria: importa catálogos JSON y XML, consulta productos, exporta a JSON y XML y guarda/recupera el inventario mediante serialización. Incluye dos actividades independientes: consultas XPath y acceso aleatorio con `RandomAccessFile`.

## Requisitos

- JDK 21
- Maven (o un IDE con Maven). La primera vez descarga la librería Gson.

## Cómo ejecutar

Desde el IDE: ejecutar `es.iesisidra.datastock.App`.

Desde la terminal, en la carpeta del `pom.xml`:

```bash
mvn clean compile
mvn exec:java
```

Actividades complementarias (cada una tiene su propio `main`):

```bash
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.XPathActividad
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.AccesoAleatorioActividad
```

Las rutas se escriben relativas a la carpeta del proyecto, por ejemplo `datos/pruebas/json/tres_validos.json`.

## Menú

| Opción | Operación | Requisito |
|---|---|---|
| 1 | Explorar ficheros y directorios | RF-FIC-01 |
| 2 | Importar productos desde JSON (Gson) | RF-FIC-02 |
| 3 | Importar productos desde XML (DOM) | RF-FIC-03 |
| 4 → 1/2/3 | Listar, buscar por código, buscar por nombre | RF-FIC-04 |
| 5 → 1/2 | Exportar a JSON / a XML | RF-FIC-05 |
| 6 → 1/2 | Guardar / recuperar inventario `.dat` | RF-FIC-06 |
| Clase aparte | Consultas XPath | RF-FIC-07 |
| Clase aparte | Acceso aleatorio a `stock.bin` | RF-FIC-08 |

Para la actividad de acceso aleatorio: primero guarda un inventario con la opción 6.1 de DataStock (o usa `datos/pruebas/dat/inventario_cuatro.dat`), cárgalo con la opción 1 de la actividad y genera `stock.bin` con la opción 2.

## Estructura del proyecto

```text
├── pom.xml
├── README.md
├── PRUEBAS.md                 # Registro de pruebas (CP-...)
├── datos/
│   ├── ejemplos/              # Catálogos de ejemplo facilitados
│   ├── pruebas/               # Ficheros válidos e inválidos para cada prueba
│   └── salidas/               # Exportaciones y binarios generados (no se suben)
├── material_referencia/       # Modelo relacional y SQL (para UT2)
└── src/main/java/es/iesisidra/datastock/
    ├── App.java               # Menú principal + RF-FIC-01 a RF-FIC-06
    ├── modelo/                # Producto, Categoria, Proveedor, MovimientoStock
    └── complementarias/
        ├── XPathActividad.java            # RF-FIC-07
        └── AccesoAleatorioActividad.java  # RF-FIC-08
```

## Decisiones

- El inventario es un `ArrayList<Producto>` compartido por todas las opciones.
- El `switch` de cada menú solo llama a métodos; la lógica está en métodos separados de `App`.
- JSON y XML se validan con el mismo método (`crearProducto`), por eso ambos formatos aplican exactamente las mismas reglas.
- El código se guarda sin espacios y en mayúsculas; los duplicados se detectan sin distinguir mayúsculas.
- Si un fichero está mal formado o la raíz es incorrecta, no se importa nada. Si es correcto, se añaden solo los productos válidos.
- Importar JSON/XML **añade**; recuperar un `.dat` **sustituye** (previa confirmación).
- El XML se lee con la DTD prohibida para evitar ataques XXE.
- `stock.bin` es una copia independiente: modificarlo no cambia el inventario.
