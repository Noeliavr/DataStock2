# Registro de pruebas · DataStock UT1

Ficheros de prueba en `datos/pruebas/`. Las salidas generadas van a `datos/salidas/` (no se suben a GitHub).

| ID | Datos usados | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|---|
| CP-FIC-01 | Ruta `datos/pruebas/explorar` | Tres elementos y totales correctos | `[FICHERO] catalogo.json`, `[FICHERO] proveedores.xml`, `[DIR] antiguos` · Total ficheros: 2 \| Directorios: 1 | OK |
| CP-FIC-02 | Ruta `no/existe` (y ruta vacía) | Mensaje de error sin cerrar DataStock | "Error: la ruta no existe." / "la ruta no puede estar vacía" y vuelve al menú | OK |
| CP-FIC-03 | Ruta `datos/pruebas/json/tres_validos.json` | Muestra tamaño y ruta absoluta | Nombre, ruta absoluta y 359 bytes | OK |
| CP-JSON-01 | `json/tres_validos.json` | Se importan los tres | Encontrados 3 · Importados 3 · Rechazados 0 | OK |
| CP-JSON-02 | `json/cinco_con_dos_errores.json` | Se importan tres y se explican dos rechazos | Importados 3 · Rechazados 2 (precio debe ser mayor que 0; el código es obligatorio) | OK |
| CP-JSON-03 | `ejemplos/catalogo_malformado.json` | No se añade ningún producto | "el JSON está mal formado. No se ha importado ningún producto." Listar → inventario vacío | OK |
| CP-JSON-04 | Importar `tres_validos.json` y después `json/duplicado.json` (contiene `tec-001`) | Se conserva el existente | `tec-001` rechazado por duplicado; buscar TEC-001 sigue mostrando "Teclado USB" | OK |
| Extra JSON | `json/raiz_incorrecta.json` (raíz es un array) | Se rechaza el documento completo | "el JSON debe ser un objeto con el array productos. No se ha importado nada." | OK |
| Extra JSON | `json/campos_invalidos.json` | Solo entran los válidos; cada rechazo explicado | Importados 2 (código ` sil-040 ` se normaliza a SIL-040) · Rechazados 8 con su motivo | OK |
| CP-XML-01 | `xml/tres_validos.xml` | Se importan tres | Encontrados 3 · Importados 3 | OK |
| CP-XML-02 | `xml/con_invalidos.xml` | Se rechazan solo los inválidos | Importado 1 · Rechazados 3 (sin precio, precio con letras, stock negativo) | OK |
| CP-XML-03 | `xml/con_dtd.xml` | Se rechaza el documento | "XML mal formado o con DTD (no permitida). No se ha importado nada." | OK |
| CP-XML-04 | Importar `json/tres_validos.json` y luego `xml/duplicado_de_json.xml` | Se rechaza el duplicado | TEC-001: código duplicado (ya existe en el inventario) | OK |
| Extra XML | `xml/malformado.xml` y `xml/raiz_incorrecta.xml` | No se importa nada | Error de XML mal formado / "la raíz debe ser <catalogo>" | OK |
| CP-CON-01 | Listar sin importar | Mensaje de inventario vacío | "El inventario está vacío. Importa un catálogo primero." | OK |
| CP-CON-02 | Buscar `  tec-001 ` | Localiza TEC-001 | Muestra TEC-001 Teclado USB | OK |
| CP-CON-03 | Buscar `TECLADO` | Coincidencias parciales | Muestra Teclado USB · Coincidencias: 1 | OK |
| CP-CON-04 | Importar JSON y XML y listar | Productos de ambos formatos | 6 productos ordenados por código (3 de JSON + 3 de XML) | OK |
| CP-EXP-01 | Exportar inventario a `datos/salidas/inventario.json` | JSON válido con los productos | Fichero generado con objeto raíz `productos` (ver `json/exportado_ejemplo.json`) | OK |
| CP-EXP-02 | Exportar a `datos/salidas/inventario.xml` | XML válido con los productos | `<catalogo>` con sus `<producto>` indentados (ver `xml/exportado_ejemplo.xml`) | OK |
| CP-EXP-03 | Exportar sobre fichero existente y responder N | Fichero original intacto | "Operación cancelada." Mismo md5 antes y después | OK |
| CP-EXP-04 | Importar JSON + XML y exportar a XML | Se conservan los datos | Mismos códigos, nombres, precios (24.90, 100.00…), stock e IDs | OK |
| CP-EXP-05 | Abrir de nuevo el programa e importar el JSON/XML exportado | Se recuperan los mismos productos | 6 encontrados · 6 importados en ambos formatos | OK |
| CP-SER-01 | Guardar 6 productos en `inventario.dat`, cerrar y recuperar | Se recuperan todos | "Inventario recuperado correctamente. Productos disponibles: 6" | OK |
| CP-SER-02 | Con 3 productos en memoria, recuperar y responder N | Se conserva el inventario actual | "Operación cancelada." Listar → 3 productos | OK |
| CP-SER-03 | `dat/danado.dat` | Se informa y no cambia la memoria | "el fichero está dañado o no se puede leer. El inventario no cambia." Listar → 3 | OK |
| CP-SER-04 | Guardar con inventario vacío | No se crea fichero | "No hay productos en memoria. No se crea ningún fichero." | OK |
| CP-XPATH-01 | `xpath/catalogo_xpath.xml`, código `mon-003` | Se encuentra el producto | MON-003 Monitor 24 pulgadas 149.99 € | OK |
| CP-XPATH-02 | Categoría 1 | Solo productos de categoría 1 | TEC-001 y DOC-007 · Encontrados: 2 | OK |
| CP-XPATH-03 | Precio superior a 100 | Excluye exactamente 100 | Solo MON-003 (DOC-007 vale 100 y no sale) | OK |
| CP-XPATH-04 | Contar | Resultado 3 | "Número de productos en el catálogo: 3" | OK |
| CP-XPATH-05 | `xpath/con_dtd.xml` | Se rechaza | "XML mal formado o con DTD (no permitida)" | OK |
| CP-RAF-01 | Cargar `dat/inventario_cuatro.dat` y generar `stock.bin` | Fichero de 144 bytes | "4 registros \| Tamaño: 144 bytes" | OK |
| CP-RAF-02 | Consultar posición 2 | Acceso directo al tercer registro | `seek(72)` → RAT-002 \| 14.5 € \| stock 25 | OK |
| CP-RAF-03 | Modificar stock de la posición 2 a 15 | Solo cambia ese campo | `seek(104)` + writeInt · lectura secuencial: solo cambia el stock de RAT-002 | OK |
| CP-RAF-04 | Posición 4 y posición -1 | Se rechaza | "Error: posición fuera de rango." | OK |
| CP-RAF-05 | `raf/stock_invalido.bin` (37 bytes) | Se informa de fichero inválido | "su tamaño (37 bytes) no es múltiplo de 36" | OK |
| Extra RAF | Generar sin productos cargados | No se genera | "No hay productos en memoria… No se genera el fichero." | OK |

Nota: al rechazar un XML con DTD o mal formado, Java escribe además una línea `[Fatal Error] ...` en la consola de errores. Es el aviso propio del analizador; el programa no se cierra.
