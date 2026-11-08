Registro de pruebas · DataStock UT1

Ficheros de prueba en datos/pruebas/. Las salidas generadas van a datos/salidas/ (no se suben a GitHub).

ID	Datos usados	Resultado esperado	Resultado obtenido	Estado
CP-FIC-01	Ruta datos/pruebas/explorar	Tres elementos y totales correctos	[FICHERO] catalogo.json, [FICHERO] proveedores.xml, [DIR] antiguos · Total ficheros: 2 | Directorios: 1	OK
CP-FIC-02	Ruta no/existe (y ruta vacía)	Mensaje de error sin cerrar DataStock	"Error: la ruta no existe." / "la ruta no puede estar vacía" y vuelve al menú	OK
CP-FIC-03	Ruta datos/pruebas/json/tres_validos.json	Muestra tamaño y ruta absoluta	Nombre, ruta absoluta y 359 bytes	OK
CP-JSON-01	json/tres_validos.json	Se importan los tres	Encontrados 3 · Importados 3 · Rechazados 0	OK
CP-JSON-02	json/cinco_con_dos_errores.json	Se importan tres y se explican dos rechazos	Importados 3 · Rechazados 2 (precio debe ser mayor que 0; el código es obligatorio)	OK
CP-JSON-03	ejemplos/catalogo_malformado.json	No se añade ningún producto	"el JSON está mal formado. No se ha importado ningún producto." Listar → inventario vacío	OK
CP-JSON-04	Importar tres_validos.json y después json/duplicado.json (contiene tec-001)	Se conserva el existente	tec-001 rechazado por duplicado; buscar TEC-001 sigue mostrando "Teclado USB"	OK
Extra JSON	json/raiz_incorrecta.json (raíz es un array)	Se rechaza el documento completo	"el JSON debe ser un objeto con el array productos. No se ha importado nada."	OK
Extra JSON	json/campos_invalidos.json	Solo entran los válidos; cada rechazo explicado	Importados 2 (código sil-040 se normaliza a SIL-040) · Rechazados 8 con su motivo	OK
CP-XML-01	xml/tres_validos.xml	Se importan tres	Encontrados 3 · Importados 3	OK
CP-XML-02	xml/con_invalidos.xml	Se rechazan solo los inválidos	Importado 1 · Rechazados 3 (sin precio, precio con letras, stock negativo)	OK
CP-XML-03	xml/con_dtd.xml	Se rechaza el documento	"XML mal formado o con DTD (no permitida). No se ha importado nada."	OK
CP-XML-04	Importar json/tres_validos.json y luego xml/duplicado_de_json.xml	Se rechaza el duplicado	TEC-001: código duplicado (ya existe en el inventario)	OK
Extra XML	xml/malformado.xml y xml/raiz_incorrecta.xml	No se importa nada	Error de XML mal formado / "la raíz debe ser <catalogo>"	OK
CP-CON-01	Listar sin importar	Mensaje de inventario vacío	"El inventario está vacío. Importa un catálogo primero."	OK
CP-CON-02	Buscar  tec-001	Localiza TEC-001	Muestra TEC-001 Teclado USB	OK
CP-CON-03	Buscar TECLADO	Coincidencias parciales	Muestra Teclado USB · Coincidencias: 1	OK
CP-CON-04	Importar JSON y XML y listar	Productos de ambos formatos	6 productos ordenados por código (3 de JSON + 3 de XML)	OK
CP-EXP-01	Exportar inventario a datos/salidas/inventario.json	JSON válido con los productos	Fichero generado con objeto raíz productos (ver json/exportado_ejemplo.json)	OK
CP-EXP-02	Exportar a datos/salidas/inventario.xml	XML válido con los productos	<catalogo> con sus <producto> indentados (ver xml/exportado_ejemplo.xml)	OK
CP-EXP-03	Exportar sobre fichero existente y responder N	Fichero original intacto	"Operación cancelada." Mismo md5 antes y después	OK
CP-EXP-04	Importar JSON + XML y exportar a XML	Se conservan los datos	Mismos códigos, nombres, precios (24.90, 100.00…), stock e IDs	OK
CP-EXP-05	Abrir de nuevo el programa e importar el JSON/XML exportado	Se recuperan los mismos productos	6 encontrados · 6 importados en ambos formatos	OK
CP-SER-01	Guardar 6 productos en inventario.dat, cerrar y recuperar	Se recuperan todos	"Inventario recuperado correctamente. Productos disponibles: 6"	OK
CP-SER-02	Con 3 productos en memoria, recuperar y responder N	Se conserva el inventario actual	"Operación cancelada." Listar → 3 productos	OK
CP-SER-03	dat/danado.dat	Se informa y no cambia la memoria	"el fichero está dañado o no se puede leer. El inventario no cambia." Listar → 3	OK
CP-SER-04	Guardar con inventario vacío	No se crea fichero	"No hay productos en memoria. No se crea ningún fichero."	OK
CP-XPATH-01	xpath/catalogo_xpath.xml, código mon-003	Se encuentra el producto	MON-003 Monitor 24 pulgadas 149.99 €	OK
CP-XPATH-02	Categoría 1	Solo productos de categoría 1	TEC-001 y DOC-007 · Encontrados: 2	OK
CP-XPATH-03	Precio superior a 100	Excluye exactamente 100	Solo MON-003 (DOC-007 vale 100 y no sale)	OK
CP-XPATH-04	Contar	Resultado 3	"Número de productos en el catálogo: 3"	OK
CP-XPATH-05	xpath/con_dtd.xml	Se rechaza	"XML mal formado o con DTD (no permitida)"	OK
CP-RAF-01	Cargar dat/inventario_cuatro.dat y generar stock.bin	Fichero de 144 bytes	"4 registros | Tamaño: 144 bytes"	OK
CP-RAF-02	Consultar posición 2	Acceso directo al tercer registro	seek(72) → RAT-002 | 14.5 € | stock 25	OK
CP-RAF-03	Modificar stock de la posición 2 a 15	Solo cambia ese campo	seek(104) + writeInt · lectura secuencial: solo cambia el stock de RAT-002	OK
CP-RAF-04	Posición 4 y posición -1	Se rechaza	"Error: posición fuera de rango."	OK
CP-RAF-05	raf/stock_invalido.bin (37 bytes)	Se informa de fichero inválido	"su tamaño (37 bytes) no es múltiplo de 36"	OK
Extra RAF	Generar sin productos cargados	No se genera	"No hay productos en memoria… No se genera el fichero."	OK

Nota: al rechazar un XML con DTD o mal formado, Java escribe además una línea [Fatal Error] ... en la consola de errores. Es el aviso propio del analizador; el programa no se cierra.

Pruebas UT2 · JDBC y MySQL

Cómo rellenar: en "Resultado obtenido" escribe lo que salió por pantalla (resumido), pon OK o FALLO y guarda la captura en evidencias/ con el nombre de la prueba (por ejemplo CP-JDBC-01.png). Cuando la prueba lo indica, comprueba también en MySQL que la tabla coincide con lo que dice la consola.

Entorno: MySQL 8.0, JDK 11 (maven.compiler.release = 11 en el pom.xml; el proyecto también funciona con JDK 21).

Datos usados, en este orden:

Script material_referencia/DataStock_BD_MySQL.sql ejecutado y config/bd.properties creado.
Categorías 1 "Periféricos" y 2 "Monitores"; proveedores 1 "Distribuciones Norte" (ventas@distribuciones.example), 2 "Electrónica Sur" y 3 "Monitores Centro".
Productos del ejemplo guiado dados de alta con la opción 7.4: TEC-001 (49.90, stock 10, cat. 1, prov. 1), RAT-002 (19.95, 25, 1, 1) y MON-003 (149.00, 8, 2, 3).
Ficheros datos/pruebas/json/lote_mixto_ut2.json y datos/pruebas/json/lote_rollback_ut2.json.
ID	Operación y datos	Resultado esperado	Resultado obtenido	Estado
CP-JDBC-01	7.1 con bd.properties correcto	[OK] verde, base de datos datastock, gestor MySQL, versión y número de registros		
CP-JDBC-02	7.1 con una contraseña incorrecta en bd.properties	[ERROR] "Usuario o contraseña incorrectos" sin mostrar la contraseña; el menú sigue funcionando		
CP-JDBC-03	Alta de categorías 1 y 2 y de los proveedores 1, 2 (sin email) y 3; listar ambos	[OK] en cada alta; listados ordenados por ID; los proveedores sin email se ven con — y en MySQL con NULL		
CP-JDBC-04	Alta de categoría con ID 1 repetido y con nombre "Periféricos" repetido; eliminar la categoría 1 (tiene productos)	[ERROR] por ID duplicado y por nombre duplicado; [ERROR] "tiene productos asociados" y la categoría sigue existiendo		
CP-JDBC-05	7.4.1 alta kbd-100, "Teclado compacto", 35.50, stock 8, categoría 1, proveedor 1; 7.4.2 consultar KBD-100	Se guarda como KBD-100; la ficha muestra los datos y los nombres de categoría y proveedor		
CP-JDBC-06	7.4.3 modificar KBD-100: nombre "Teclado compacto RGB", precio 39.90, Intro en categoría y proveedor	[OK]; consulta con nombre y precio nuevos; el stock sigue en 8		
CP-JDBC-07	Alta de KBD-100 otra vez; alta de KBD-101 con categoría 99	[ERROR] "ya existe en MySQL"; [ERROR] "No existe la categoría con ID 99"; no se inserta nada		
CP-JDBC-08	7.4.4 eliminar KBD-100 (sin movimientos); eliminar TEC-001 después de CP-JDBC-13	KBD-100 eliminado con [OK]; TEC-001 [ERROR] "tiene movimientos de stock" y sigue en MySQL		
CP-JDBC-09	7.5.1 listar todos	MON-003, RAT-002, TEC-001 ordenados por código, con nombres de categoría y proveedor (JOIN)		
CP-JDBC-10	7.5.3 buscar "rat" y después "zzz"	"rat" encuentra "Ratón inalámbrico" sin distinguir mayúsculas; "zzz" muestra [AVISO] "No se han encontrado productos"		
CP-JDBC-11	7.5.4 umbral 10	Solo MON-003 (stock 8); TEC-001, con stock exactamente 10, no aparece		
CP-JDBC-12	7.6.1 ENTRADA de 5 unidades de TEC-001	[OK] stock anterior 10 → posterior 15		
CP-JDBC-13	7.6.2 SALIDA de 4 unidades de TEC-001	[OK] stock anterior 15 → posterior 11		
CP-JDBC-14	7.6.2 SALIDA de 50 unidades de TEC-001	[ERROR] "Stock insuficiente. El stock permanece en 11"; no se añade ningún movimiento		
CP-JDBC-15	7.7.1 historial completo; 7.7.2 historial de TEC-001	2 movimientos con fecha, tipo, cantidad y stock anterior/posterior; el de 50 no aparece. En MySQL, movimiento_stock tiene 2 filas y TEC-001 stock 11		
CP-JDBC-16	7.7.2 historial de RAT-002 (sin movimientos) y de NOEXISTE	RAT-002: [AVISO] "existe, pero no tiene movimientos"; NOEXISTE: [ERROR] "No existe ningún producto"		
CP-JDBC-17	Opción 2 con lote_mixto_ut2.json → 7.8 (S, prueba de rollback N). Después repetir 7.8 sin cerrar el programa	1ª vez: analizados 6, insertados 2 (HUB-030, CAB-031), omitido 1 (TEC-001), rechazados 3 (categoría 99, proveedor 99, precio con 3 decimales). 2ª vez: insertados 0, omitidos 3, rechazados 3 (no se duplica nada)		
CP-JDBC-18	Reiniciar el programa, opción 2 con lote_rollback_ut2.json → 7.8 respondiendo S a la prueba de rollback. Después repetir 7.8 respondiendo N	1ª vez: SSD-040 y RAM-041 válidos, pero [ERROR] "Se ha ejecutado ROLLBACK" e insertados 0; no aparecen en 7.5.1 ni en MySQL. 2ª vez: insertados 2		
CP-JDBC-19	7.9 escenario A	[OK] las dos categorías de prueba (9001 y 9002) existen tras el commit		
CP-JDBC-20	7.9 escenario B	[AVISO] error de clave duplicada capturado; [OK] la categoría 9003 NO existe. En MySQL salen 9001 y 9002, pero no 9003		
CP-SQLITE-01	Ejecutar DemoSQLite por primera vez	Se crea datos/sqlite/productos_demo.db; "Insertados: 2 | Ya existentes: 0"; se listan SQL-001 y SQL-002		
CP-SQLITE-02	Ejecutar DemoSQLite otra vez	"Insertados: 0 | Ya existentes: 2"; siguen saliendo solo 2 filas		
CP-SQLITE-03	Tras la ejecución, comprobar el fichero	[OK] "Conexión cerrada" y el fichero .db sigue existiendo en la carpeta		
Extra	7.1 sin el fichero config/bd.properties	[ERROR] "No se encuentra config/bd.properties…"; la aplicación sigue funcionando		
Extra	7.4.1 con código tec_01, nombre "TV", precio 12.345 y precio 0	[ERROR] por cada dato incorrecto, sin llegar a insertar		
Extra	7.8 con el inventario en memoria vacío	[AVISO] "No hay productos para importar"