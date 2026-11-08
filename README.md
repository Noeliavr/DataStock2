DataStock – UT1 (Ficheros) y UT2 (JDBC + MySQL)

Módulo: Acceso a Datos · 2.º DAM · IES Isidra de Guzmán · Curso 2026–2027 Lenguaje: Java (compilado para Java 11, funciona también con Java 21) · Construcción: Maven · Interfaz: consola

Aplicación de consola que gestiona un inventario de productos.

UT1: inventario en memoria. Importa catálogos JSON y XML, consulta productos, exporta a JSON y XML y guarda/recupera el inventario mediante serialización. Actividades independientes: XPath y RandomAccessFile.
UT2: base de datos MySQL mediante JDBC (opción 7 del menú). Categorías, proveedores, CRUD de productos, consultas, movimientos de stock con procedimiento almacenado, importación de memoria a MySQL con transacción y demostración de commit/rollback. Actividad independiente: DemoSQLite.
Requisitos
JDK 11 o superior. En el pom.xml, maven.compiler.release está en 11 porque el equipo de desarrollo solo dispone de JDK 11; el código no usa nada exclusivo de versiones posteriores, así que funciona igual con JDK 21.
Maven (o VS Code / IDE con Maven). La primera vez descarga las librerías.
MySQL 8.0.16 o posterior (para la UT2).
Librería	Dependencia Maven	Versión
Gson	com.google.code.gson:gson	2.11.0
MySQL Connector/J	com.mysql:mysql-connector-j	9.3.0
SQLite JDBC	org.xerial:sqlite-jdbc	3.49.1.0
Preparar MySQL (UT2)
Ejecuta material_referencia/DataStock_BD_MySQL.sql: en MySQL Workbench con el botón del rayo, o en MySQL 8.0 Command Line Client con source <ruta>/DataStock_BD_MySQL.sql (usando barras /).
Comprueba que existe el esquema datastock con las tablas categoria, proveedor, producto, movimiento_stock y el procedimiento registrar_movimiento.
Configurar la conexión (sin publicar la contraseña)
Copia config/bd.properties.ejemplo en la misma carpeta con el nombre bd.properties.
Escribe tus datos:
properties
url=jdbc:mysql://localhost:3306/datastock?connectTimeout=5000
usuario=root
contrasena=tu_contraseña

config/bd.properties está en .gitignore, así que no se sube a GitHub. La clase config/ConexionBD lee este fichero y abre la conexión con DriverManager.getConnection(url, usuario, contrasena).

Cómo ejecutar

Desde el IDE: ejecutar es.iesisidra.datastock.App.

Desde la terminal, en la carpeta del pom.xml:

bash
mvn clean compile
mvn exec:java

Actividades complementarias (cada una tiene su propio main):

bash
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.XPathActividad
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.AccesoAleatorioActividad
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.DemoSQLite

En VS Code también se pueden ejecutar con el botón Run que aparece encima del main de cada clase.

Las rutas se escriben relativas a la carpeta del proyecto, por ejemplo datos/pruebas/json/tres_validos.json.

Menú
Opción	Operación	Requisito
1	Explorar ficheros y directorios	RF-FIC-01
2	Importar productos desde JSON (Gson)	RF-FIC-02
3	Importar productos desde XML (DOM)	RF-FIC-03
4 → 1/2/3	Listar, buscar por código, buscar por nombre	RF-FIC-04
5 → 1/2	Exportar a JSON / a XML	RF-FIC-05
6 → 1/2	Guardar / recuperar inventario .dat	RF-FIC-06
7	Gestionar base de datos MySQL	UT2
Clase aparte	Consultas XPath	RF-FIC-07
Clase aparte	Acceso aleatorio a stock.bin	RF-FIC-08
Clase aparte	Demo SQLite	Actividad comparativa UT2

Menú 7 (MySQL):

Opción	Operación	Requisito
7.1	Comprobar conexión e información de la BD	RF-JDBC-01
7.2	Categorías: alta, listado, modificar, eliminar	RF-JDBC-02
7.3	Proveedores: alta, listado, modificar, eliminar	RF-JDBC-02
7.4	Productos: alta, consultar, modificar, eliminar	RF-JDBC-03
7.5	Listar (JOIN), buscar por código, por nombre (LIKE), stock < umbral	RF-JDBC-04
7.6	Entrada / salida de stock (CallableStatement)	RF-JDBC-05
7.7	Historial de movimientos (todos / de un producto)	RF-JDBC-06
7.8	Importar productos de memoria a MySQL (transacción)	RF-JDBC-07
7.9	Demostración de commit y rollback	RF-JDBC-08

Para la actividad de acceso aleatorio: primero guarda un inventario con la opción 6.1 de DataStock (o usa datos/pruebas/dat/inventario_cuatro.dat), cárgalo con la opción 1 de la actividad y genera stock.bin con la opción 2.

Estructura del proyecto
text
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
    ├── modelo/                # Producto, ProductoDetalle, Categoria, Proveedor, MovimientoStock
    └── complementarias/
        ├── XPathActividad.java            # RF-FIC-07
        ├── AccesoAleatorioActividad.java  # RF-FIC-08
        └── DemoSQLite.java                # Actividad comparativa UT2

Además: config/bd.properties.ejemplo (plantilla de conexión) y material_referencia/DataStock_BD_MySQL.sql (script de la base de datos).

Decisiones
El inventario es un ArrayList<Producto> compartido por todas las opciones.
El switch de cada menú solo llama a métodos; la lógica está en métodos separados de App.
JSON y XML se validan con el mismo método (crearProducto), por eso ambos formatos aplican exactamente las mismas reglas.
El código se guarda sin espacios y en mayúsculas; los duplicados se detectan sin distinguir mayúsculas.
Si un fichero está mal formado o la raíz es incorrecta, no se importa nada. Si es correcto, se añaden solo los productos válidos.
Importar JSON/XML añade; recuperar un .dat sustituye (previa confirmación).
El XML se lee con la DTD prohibida para evitar ataques XXE.
stock.bin es una copia independiente: modificarlo no cambia el inventario.
Memoria (UT1) y MySQL (UT2) son almacenes distintos
Las opciones 1 a 6 trabajan solo con el ArrayList en memoria, que se pierde al cerrar el programa.
La opción 7 trabaja solo con MySQL, donde los datos se conservan entre ejecuciones.
Importar un JSON o XML no guarda nada en MySQL. El único puente es la opción 7.8, que copia los productos válidos de memoria a MySQL. No borra la memoria ni deja nada sincronizado.
Decisiones de la UT2
MenuMySQL se encarga de la consola y OperacionesBD del SQL: el menú nunca escribe SQL.
Todas las sentencias con datos del usuario usan PreparedStatement (nunca se concatena lo que escribe el usuario).
Antes de cada INSERT/UPDATE se valida en Java: formato del código, longitudes, precio BigDecimal > 0 con 2 decimales como máximo, duplicados y existencia de categoría y proveedor. Si aun así MySQL rechaza la operación (clave foránea al borrar, por ejemplo), se captura la SQLException y se explica en rojo.
Cada operación abre una conexión corta y la cierra con try-with-resources.
El stock solo cambia con el procedimiento registrar_movimiento (opción 7.6), nunca desde la modificación de productos.
Opción 7.8: inserta el lote en una transacción (setAutoCommit(false) → commit() / rollback()). Para la prueba CP-JDBC-18, la opción pregunta si se quiere activar la prueba de rollback: al final del lote repite el primer producto para provocar un error de clave duplicada.
Opción 7.9: usa categorías de prueba con IDs libres a partir de 9001 y nombres Demo TX <id>. Al terminar pregunta si se borran. Si se conservan, se pueden eliminar desde el menú de categorías o con DELETE FROM categoria WHERE nombre LIKE 'Demo TX%'; (no tienen productos, así que se pueden borrar sin problema).
Comparativa MySQL y SQLite
Pregunta	MySQL	SQLite
¿Necesita servidor?	Sí, un servidor independiente que hay que instalar y arrancar.	No, es embebido: el motor va dentro de la aplicación, como una librería.
¿Dónde se guardan los datos?	En los ficheros que gestiona el servidor MySQL.	En un único fichero: datos/sqlite/productos_demo.db.
¿Cómo se conecta por JDBC?	jdbc:mysql://localhost:3306/datastock con usuario y contraseña.	jdbc:sqlite:datos/sqlite/productos_demo.db, sin usuario ni contraseña.
¿Qué dependencia se usa?	com.mysql:mysql-connector-j	org.xerial:sqlite-jdbc
Ventajas	Varios usuarios a la vez, usuarios y permisos, procedimientos almacenados, apto para mucho volumen y acceso por red.	Nada que instalar, el fichero se copia y se lleva a cualquier sitio, muy ligero.
Inconvenientes	Hay que instalar, configurar y mantener el servidor.	Escrituras de un solo usuario a la vez, sin usuarios ni permisos, sin procedimientos almacenados.
¿Para qué lo elegiría?	Aplicaciones de empresa con varios usuarios, como DataStock.	Apps de móvil o escritorio, configuraciones locales, pruebas y prototipos.

Pool de conexiones. Es un conjunto de conexiones que se abren una vez y se reutilizan: el programa "pide prestada" una conexión y la devuelve al terminar, en lugar de abrir y cerrar una nueva cada vez. Es útil en servidores web con muchos usuarios simultáneos, porque abrir una conexión es lento. DataStock es una aplicación de consola con un solo usuario y pocas operaciones, así que funciona bien con conexiones cortas: cada método abre la conexión, hace su trabajo y la cierra con try-with-resources, sin dejar ninguna abierta.