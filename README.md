# DataStock – UT1 (Ficheros), UT2 (JDBC + MySQL) y UT3 (Componentes)

**Módulo:** Acceso a Datos · 2.º DAM · IES Isidra de Guzmán · Curso 2026–2027
**Lenguaje:** Java (compilado para Java 11, funciona también con Java 21) · **Construcción:** Maven · **Interfaz:** consola

Aplicación de consola que gestiona un inventario de productos.

- **UT1:** inventario en memoria. Importa catálogos JSON y XML, consulta productos, exporta a JSON y XML y guarda/recupera el inventario mediante serialización. Actividades independientes: XPath y `RandomAccessFile`.
- **UT3:** la lectura de JSON (opción 2) y la búsqueda por código en MySQL (opción 7 → 5 → 2) están extraídas a dos componentes reutilizables: `LectorProductosJson` y `ConsultaProductoJdbc`. Ver la sección «UT3: componentes reutilizables».
- **UT2:** base de datos MySQL mediante JDBC (opción 7 del menú). Categorías, proveedores, CRUD de productos, consultas, movimientos de stock con procedimiento almacenado, importación de memoria a MySQL con transacción y demostración de commit/rollback. Actividad independiente: `DemoSQLite`.

## Requisitos

- JDK 11 o superior. En el `pom.xml`, `maven.compiler.release` está en **11** porque el equipo de desarrollo solo dispone de JDK 11; el código no usa nada exclusivo de versiones posteriores, así que funciona igual con JDK 21.
- Maven (o VS Code / IDE con Maven). La primera vez descarga las librerías.
- MySQL 8.0.16 o posterior (para la UT2).

| Librería | Dependencia Maven | Versión |
|---|---|---|
| Gson | `com.google.code.gson:gson` | 2.11.0 |
| MySQL Connector/J | `com.mysql:mysql-connector-j` | 9.3.0 |
| SQLite JDBC | `org.xerial:sqlite-jdbc` | 3.49.1.0 |

## Preparar MySQL (UT2)

1. Ejecuta `material_referencia/DataStock_BD_MySQL.sql`: en MySQL Workbench con el botón del rayo, o en **MySQL 8.0 Command Line Client** con `source <ruta>/DataStock_BD_MySQL.sql` (usando barras `/`).
2. Comprueba que existe el esquema `datastock` con las tablas `categoria`, `proveedor`, `producto`, `movimiento_stock` y el procedimiento `registrar_movimiento`.

## Configurar la conexión (sin publicar la contraseña)

1. Copia `config/bd.properties.ejemplo` en la misma carpeta con el nombre **`bd.properties`**.
2. Escribe tus datos:

```properties
url=jdbc:mysql://localhost:3306/datastock?connectTimeout=5000
usuario=root
contrasena=tu_contraseña
```

`config/bd.properties` está en `.gitignore`, así que **no se sube a GitHub**. La clase `config/ConexionBD` lee este fichero y abre la conexión con `DriverManager.getConnection(url, usuario, contrasena)`.

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
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.DemoSQLite
```

En VS Code también se pueden ejecutar con el botón **Run** que aparece encima del `main` de cada clase.

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
| 7 | Gestionar base de datos MySQL | UT2 |
| Clase aparte | Consultas XPath | RF-FIC-07 |
| Clase aparte | Acceso aleatorio a `stock.bin` | RF-FIC-08 |
| Clase aparte | Demo SQLite | Actividad comparativa UT2 |

**Menú 7 (MySQL):**

| Opción | Operación | Requisito |
|---|---|---|
| 7.1 | Comprobar conexión e información de la BD | RF-JDBC-01 |
| 7.2 | Categorías: alta, listado, modificar, eliminar | RF-JDBC-02 |
| 7.3 | Proveedores: alta, listado, modificar, eliminar | RF-JDBC-02 |
| 7.4 | Productos: alta, consultar, modificar, eliminar | RF-JDBC-03 |
| 7.5 | Listar (JOIN), buscar por código, por nombre (LIKE), stock < umbral | RF-JDBC-04 |
| 7.6 | Entrada / salida de stock (`CallableStatement`) | RF-JDBC-05 |
| 7.7 | Historial de movimientos (todos / de un producto) | RF-JDBC-06 |
| 7.8 | Importar productos de memoria a MySQL (transacción) | RF-JDBC-07 |
| 7.9 | Demostración de commit y rollback | RF-JDBC-08 |

Para la actividad de acceso aleatorio: primero guarda un inventario con la opción 6.1 de DataStock (o usa `datos/pruebas/dat/inventario_cuatro.dat`), cárgalo con la opción 1 de la actividad y genera `stock.bin` con la opción 2.

## Estructura del proyecto

```text
├── pom.xml
├── README.md
├── PRUEBAS.md                 # Registro de pruebas (CP-...)
├── evidencias/                # Capturas de las pruebas
├── config/bd.properties.ejemplo  # Plantilla de conexión (bd.properties no se sube)
├── datos/
│   ├── ejemplos/              # Catálogos de ejemplo facilitados
│   ├── pruebas/               # Ficheros válidos e inválidos para cada prueba (UT1 y UT2)
│   ├── sqlite/                # Base de datos de DemoSQLite (se genera, no se sube)
│   └── salidas/               # Exportaciones y binarios generados (no se suben)
├── material_referencia/       # Modelo relacional y SQL (para UT2)
└── src/main/java/es/iesisidra/datastock/
    ├── App.java               # Menú principal + RF-FIC-01 a RF-FIC-06
    ├── MenuMySQL.java         # Opción 7: menús UT2 (pide datos, valida, muestra)
    ├── bd/OperacionesBD.java  # Todas las sentencias SQL (JDBC), sin consola
    ├── config/ConexionBD.java # Abre la conexión leyendo config/bd.properties
    ├── util/Colores.java      # [OK] verde, [ERROR] rojo, [AVISO] amarillo
    ├── componentes/           # UT3: componentes reutilizables (sin consola)
    │   ├── LectorProductosJson.java   # Lee y valida productos de un JSON
    │   ├── ResultadoLecturaJson.java  # Lo que devuelve el lector: válidos + incidencias
    │   └── ConsultaProductoJdbc.java  # Busca un producto en MySQL por código
    ├── modelo/                # Producto, ProductoDetalle, Categoria, Proveedor, MovimientoStock
    └── complementarias/
        ├── XPathActividad.java            # RF-FIC-07
        ├── AccesoAleatorioActividad.java  # RF-FIC-08
        └── DemoSQLite.java                # Actividad comparativa UT2
```

Además: `config/bd.properties.ejemplo` (plantilla de conexión) y `material_referencia/DataStock_BD_MySQL.sql` (script de la base de datos).

## Decisiones

- El inventario es un `ArrayList<Producto>` compartido por todas las opciones.
- El `switch` de cada menú solo llama a métodos; la lógica está en métodos separados de `App`.
- JSON y XML aplican las mismas reglas de validación. Desde la UT3, el JSON lo valida el componente `LectorProductosJson` y el XML sigue usando `App.crearProducto`.
- El código se guarda sin espacios y en mayúsculas; los duplicados se detectan sin distinguir mayúsculas.
- Si un fichero está mal formado o la raíz es incorrecta, no se importa nada. Si es correcto, se añaden solo los productos válidos.
- Importar JSON/XML **añade**; recuperar un `.dat` **sustituye** (previa confirmación).
- El XML se lee con la DTD prohibida para evitar ataques XXE.
- `stock.bin` es una copia independiente: modificarlo no cambia el inventario.

## Memoria (UT1) y MySQL (UT2) son almacenes distintos

- Las opciones 1 a 6 trabajan **solo** con el `ArrayList` en memoria, que se pierde al cerrar el programa.
- La opción 7 trabaja **solo** con MySQL, donde los datos se conservan entre ejecuciones.
- Importar un JSON o XML **no** guarda nada en MySQL. El único puente es la opción **7.8**, que copia los productos válidos de memoria a MySQL. No borra la memoria ni deja nada sincronizado.

## Decisiones de la UT2

- `MenuMySQL` se encarga de la consola y `OperacionesBD` del SQL: el menú nunca escribe SQL.
- Todas las sentencias con datos del usuario usan `PreparedStatement` (nunca se concatena lo que escribe el usuario).
- Antes de cada `INSERT`/`UPDATE` se valida en Java: formato del código, longitudes, precio `BigDecimal` > 0 con 2 decimales como máximo, duplicados y existencia de categoría y proveedor. Si aun así MySQL rechaza la operación (clave foránea al borrar, por ejemplo), se captura la `SQLException` y se explica en rojo.
- Cada operación abre una conexión corta y la cierra con try-with-resources.
- El stock solo cambia con el procedimiento `registrar_movimiento` (opción 7.6), nunca desde la modificación de productos.
- **Opción 7.8:** inserta el lote en una transacción (`setAutoCommit(false)` → `commit()` / `rollback()`). Para la prueba CP-JDBC-18, la opción pregunta si se quiere activar la prueba de rollback: al final del lote repite el primer producto para provocar un error de clave duplicada.
- **Opción 7.9:** usa categorías de prueba con IDs libres a partir de 9001 y nombres `Demo TX <id>`. Al terminar pregunta si se borran. Si se conservan, se pueden eliminar desde el menú de categorías o con `DELETE FROM categoria WHERE nombre LIKE 'Demo TX%';` (no tienen productos, así que se pueden borrar sin problema).

## Comparativa MySQL y SQLite

| Pregunta | MySQL | SQLite |
|---|---|---|
| ¿Necesita servidor? | Sí, un servidor independiente que hay que instalar y arrancar. | No, es **embebido**: el motor va dentro de la aplicación, como una librería. |
| ¿Dónde se guardan los datos? | En los ficheros que gestiona el servidor MySQL. | En un único fichero: `datos/sqlite/productos_demo.db`. |
| ¿Cómo se conecta por JDBC? | `jdbc:mysql://localhost:3306/datastock` con usuario y contraseña. | `jdbc:sqlite:datos/sqlite/productos_demo.db`, sin usuario ni contraseña. |
| ¿Qué dependencia se usa? | `com.mysql:mysql-connector-j` | `org.xerial:sqlite-jdbc` |
| Ventajas | Varios usuarios a la vez, usuarios y permisos, procedimientos almacenados, apto para mucho volumen y acceso por red. | Nada que instalar, el fichero se copia y se lleva a cualquier sitio, muy ligero. |
| Inconvenientes | Hay que instalar, configurar y mantener el servidor. | Escrituras de un solo usuario a la vez, sin usuarios ni permisos, sin procedimientos almacenados. |
| ¿Para qué lo elegiría? | Aplicaciones de empresa con varios usuarios, como DataStock. | Apps de móvil o escritorio, configuraciones locales, pruebas y prototipos. |

**Pool de conexiones.** Es un conjunto de conexiones que se abren una vez y se reutilizan: el programa "pide prestada" una conexión y la devuelve al terminar, en lugar de abrir y cerrar una nueva cada vez. Es útil en servidores web con muchos usuarios simultáneos, porque abrir una conexión es lento. DataStock es una aplicación de consola con un solo usuario y pocas operaciones, así que funciona bien con **conexiones cortas**: cada método abre la conexión, hace su trabajo y la cierra con try-with-resources, sin dejar ninguna abierta.

**Nota:** al ejecutar `DemoSQLite` puede salir un aviso `SLF4J: No SLF4J providers were found`. Es un aviso de la librería de registro que usa el driver de SQLite y no afecta al funcionamiento.

## UT3: componentes reutilizables

En la UT3 se han extraído dos operaciones que ya existían a dos **componentes**: clases que hacen un único trabajo, reciben datos, devuelven un resultado y **no usan la consola**. Están en el paquete `es.iesisidra.datastock.componentes`.

| Componente | Entrada | Salida | Errores |
|---|---|---|---|
| `LectorProductosJson` | `Path` del fichero JSON | `ResultadoLecturaJson`: nº de productos encontrados, lista de `Producto` válidos y lista de incidencias | `IOException` con el motivo: fichero inexistente, directorio, sin permisos, JSON mal formado o raíz sin el array `productos` |
| `ConsultaProductoJdbc` | Código del producto | `Optional<ProductoDetalle>`: con el producto si existe, vacío si no existe | `SQLException` si falla la conexión o la consulta; `IllegalArgumentException` si el código está vacío |

### Opciones de DataStock modificadas

1. **Opción 2 del menú principal (Importar productos desde JSON).** `App.importarJson()` pide la ruta, llama a `LectorProductosJson.leer(...)`, descarta los códigos que ya están en el inventario, añade el resto y muestra el resumen con [OK] en verde, las incidencias en amarillo y los errores en rojo. El componente no toca el inventario: eso lo decide el menú.
2. **Opción 7 → 5 → 2 (Buscar producto por código en MySQL).** `MenuMySQL.consultarProducto()` pide el código, llama a `ConsultaProductoJdbc.buscarPorCodigo(...)` y muestra la ficha ([OK]) o un aviso si no existe. También `OperacionesBD.buscarProducto(...)` reutiliza el componente, para que el SQL de búsqueda por código esté en un solo sitio y el resto de opciones (alta, modificación, movimientos…) sigan funcionando igual.

Los menús, las opciones y el resto de funcionalidades de la UT1 y la UT2 no cambian.

### ¿Qué ventaja tiene separar la lectura JSON del menú? ¿Qué coste tiene?

**Ventajas:** el lector se puede **reutilizar** desde otro sitio (otra aplicación, una prueba automática, una interfaz gráfica…) porque no depende de `Scanner` ni de `System.out`. Además, cada clase tiene **una sola responsabilidad**: el componente lee y valida, y el menú pregunta y muestra. Así es más fácil de entender, de probar y de cambiar (por ejemplo, cambiar los mensajes sin tocar la lectura, o al revés).

**Inconvenientes:** hay **más clases** (tres en lugar de un método), hay que pensar qué devuelve cada una (por eso existe `ResultadoLecturaJson`) y los errores se tienen que pasar con excepciones en lugar de imprimirse directamente. Para un programa tan pequeño es algo más de trabajo. Además, las reglas de validación están ahora en dos sitios: en el componente (JSON) y en `App.crearProducto` (XML), porque el enunciado pide refactorizar solo estas dos operaciones.

### ¿Por qué conviene separar una consulta JDBC de la presentación por consola?

Porque son dos responsabilidades distintas: **obtener los datos** y **mostrarlos**. Si la consulta imprimiera por pantalla, solo serviría para esta consola. Separada, la misma consulta sirve para una web, una app gráfica o una prueba, y cada una decide cómo mostrar el resultado. El componente devuelve un `Optional`, que deja claro si el producto existe o no sin usar `null`, y lanza `SQLException` para que quien lo usa decida qué mensaje enseñar. Además, el componente se encarga de cerrar la conexión, la sentencia y el `ResultSet` con try-with-resources.

### Herramientas utilizadas

| Herramienta | Para qué sirve en este proyecto |
|---|---|
| **IDE (Visual Studio Code con Extension Pack for Java)** | Escribir el código, ver los errores al momento, ejecutar `App` y las actividades, y usar Git. |
| **Maven** (`pom.xml`) | Descargar las librerías (Gson, MySQL Connector/J, SQLite JDBC), compilar (`mvn clean compile`), ejecutar (`mvn exec:java`) y generar la documentación. |
| **Javadoc** | Documentar las clases y los métodos públicos de los componentes (finalidad, parámetros, resultado y errores). Se genera la web de documentación con `mvn javadoc:javadoc` (queda en `target/reports/apidocs` o, según la versión del plugin, en `target/site/apidocs`). En VS Code, al pasar el ratón por encima de un método, se ve su Javadoc. |
| **Git y GitHub** | Guardar el historial con commits descriptivos y entregar con etiquetas (`UT1`, `UT2-v1.0`, `UT3`). |
| **JUnit 5** | No se ha usado (es opcional en esta entrega). Serviría para automatizar las pruebas de los componentes, que precisamente se pueden probar porque no dependen de la consola. |

### Pruebas

Las pruebas CP-COMP-01, CP-COMP-02 y CP-COMP-03 están en `PRUEBAS.md`, con sus evidencias en `evidencias/`.
