package es.iesisidra.datastock;

import es.iesisidra.datastock.modelo.Producto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

// DataStock - Fase 1 (UT1). Menú principal y requisitos RF-FIC-01 a RF-FIC-06.
public class App {

    // Inventario común: todas las opciones usan esta misma lista
    static ArrayList<Producto> inventario = new ArrayList<Producto>();
    static Scanner teclado = new Scanner(System.in);

    public static void main(String[] args) {
        int opcion;
        do {
            System.out.println();
            System.out.println("================================================");
            System.out.println(" DATASTOCK - GESTIÓN DE INVENTARIO");
            System.out.println(" FASE 1 - UT1");
            System.out.println("================================================");
            System.out.println("1. Explorar ficheros y directorios");
            System.out.println("2. Importar productos desde JSON");
            System.out.println("3. Importar productos desde XML");
            System.out.println("4. Consultar productos");
            System.out.println("5. Exportar productos");
            System.out.println("6. Guardar y recuperar inventario");
            System.out.println("7. Gestionar base de datos MySQL");
            System.out.println("0. Salir");
            System.out.println("------------------------------------------------");
            opcion = leerNumero("Selecciona una opción: ");

            // El switch solo llama a métodos
            switch (opcion) {
                case 1:
                    explorarRuta();
                    break;
                case 2:
                    importarJson();
                    break;
                case 3:
                    importarXml();
                    break;
                case 4:
                    menuConsultas();
                    break;
                case 5:
                    menuExportar();
                    break;
                case 6:
                    menuGuardarRecuperar();
                    break;
                case 7:
                    MenuMySQL.mostrarMenu();
                case 0:
                    System.out.println("Hasta pronto.");
                    break;
                default:
                    System.out.println("Opción no válida. Inténtalo de nuevo.");
                    break;
            }
        } while (opcion != 0);
    }

    // ================================================================
    // MÉTODOS DE AYUDA (leer del teclado)
    // ================================================================

    static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return teclado.nextLine();
    }

    // Repite la pregunta hasta que se escriba un número entero
    static int leerNumero(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje).trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Debes escribir un número entero.");
            }
        }
    }

    // Devuelve true si el usuario responde S
    static boolean confirmar(String mensaje) {
        while (true) {
            String respuesta = leerTexto(mensaje + " (S/N): ").trim();
            if (respuesta.equalsIgnoreCase("S")) {
                return true;
            }
            if (respuesta.equalsIgnoreCase("N")) {
                return false;
            }
            System.out.println("Responde S o N.");
        }
    }

    // ================================================================
    // RF-FIC-01. EXPLORAR RUTAS, FICHEROS Y DIRECTORIOS
    // ================================================================

    static void explorarRuta() {
        System.out.println("===== EXPLORAR RUTA =====");
        String ruta = leerTexto("Ruta: ").trim();

        if (ruta.isEmpty()) {
            System.out.println("Error: la ruta no puede estar vacía.");
            return;
        }

        File f = new File(ruta);

        if (!f.exists()) {
            System.out.println("Error: la ruta no existe.");
            return;
        }

        // Si es un fichero: nombre, ruta absoluta y tamaño
        if (f.isFile()) {
            System.out.println("Es un FICHERO");
            System.out.println("Nombre: " + f.getName());
            System.out.println("Ruta absoluta: " + f.getAbsolutePath());
            System.out.println("Tamaño: " + f.length() + " bytes");
            return;
        }

        // Si es un directorio: listamos su contenido
        File[] hijos = f.listFiles();
        if (hijos == null) {
            // listFiles devuelve null si no se puede leer el directorio
            System.out.println("Error: no se puede acceder al directorio (permisos).");
            return;
        }

        if (hijos.length == 0) {
            System.out.println("El directorio está vacío.");
        }

        int numFicheros = 0;
        int numDirectorios = 0;
        for (File hijo : hijos) {
            if (hijo.isDirectory()) {
                System.out.println("[DIR]     " + hijo.getName());
                numDirectorios++;
            } else {
                System.out.println("[FICHERO] " + hijo.getName() + "  " + hijo.length() + " bytes");
                numFicheros++;
            }
        }
        System.out.println("Total ficheros: " + numFicheros + " | Directorios: " + numDirectorios);
    }

    // ================================================================
    // VALIDACIÓN COMÚN (la usan la importación JSON y la XML)
    // ================================================================

    // Comprueba que el fichero se puede leer
    static boolean comprobarFicheroLectura(File f) {
        if (f.getPath().isEmpty()) {
            System.out.println("Error: la ruta no puede estar vacía.");
            return false;
        }
        if (!f.exists()) {
            System.out.println("Error: el fichero no existe.");
            return false;
        }
        if (f.isDirectory()) {
            System.out.println("Error: la ruta es un directorio, no un fichero.");
            return false;
        }
        if (!f.canRead()) {
            System.out.println("Error: no hay permisos para leer el fichero.");
            return false;
        }
        return true;
    }

    // Busca un código en una lista sin distinguir mayúsculas
    static Producto buscarEnLista(ArrayList<Producto> lista, String codigo) {
        for (Producto p : lista) {
            if (p.getCodigo().equalsIgnoreCase(codigo.trim())) {
                return p;
            }
        }
        return null;
    }

    // Recibe los 6 campos como texto, los valida y crea el Producto.
    // Si algo falla, apunta la incidencia y devuelve null.
    static Producto crearProducto(int num, String codigo, String nombre, String precioTxt,
                                  String stockTxt, String categoriaTxt, String proveedorTxt,
                                  ArrayList<Producto> nuevos, ArrayList<String> incidencias) {

        // --- código ---
        if (codigo == null || codigo.trim().isEmpty()) {
            incidencias.add("Producto " + num + ": el código es obligatorio.");
            return null;
        }
        codigo = codigo.trim().toUpperCase();
        String inicio = "Producto " + num + " (" + codigo + "): ";

        if (codigo.length() > 12) {
            incidencias.add(inicio + "el código tiene más de 12 caracteres.");
            return null;
        }
        if (!codigo.matches("[A-Z0-9-]+")) {
            incidencias.add(inicio + "el código solo admite letras, números y guiones.");
            return null;
        }
        if (buscarEnLista(inventario, codigo) != null) {
            incidencias.add(inicio + "código duplicado (ya existe en el inventario).");
            return null;
        }
        if (buscarEnLista(nuevos, codigo) != null) {
            incidencias.add(inicio + "código duplicado (repetido en el mismo fichero).");
            return null;
        }

        // --- nombre ---
        if (nombre == null || nombre.trim().isEmpty()) {
            incidencias.add(inicio + "el nombre es obligatorio.");
            return null;
        }
        nombre = nombre.trim();
        if (nombre.length() < 3 || nombre.length() > 100) {
            incidencias.add(inicio + "el nombre debe tener entre 3 y 100 caracteres.");
            return null;
        }

        // --- precio ---
        if (precioTxt == null) {
            incidencias.add(inicio + "el precio es obligatorio.");
            return null;
        }
        BigDecimal precio;
        try {
            precio = new BigDecimal(precioTxt.trim());
        } catch (NumberFormatException e) {
            incidencias.add(inicio + "el precio no es un número válido.");
            return null;
        }
        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            incidencias.add(inicio + "el precio debe ser mayor que 0.");
            return null;
        }

        // --- stock ---
        if (stockTxt == null) {
            incidencias.add(inicio + "el stock es obligatorio.");
            return null;
        }
        int stock;
        try {
            stock = Integer.parseInt(stockTxt.trim());
        } catch (NumberFormatException e) {
            incidencias.add(inicio + "el stock debe ser un número entero.");
            return null;
        }
        if (stock < 0) {
            incidencias.add(inicio + "el stock no puede ser negativo.");
            return null;
        }

        // --- categoriaId ---
        if (categoriaTxt == null) {
            incidencias.add(inicio + "la categoriaId es obligatoria.");
            return null;
        }
        int categoriaId;
        try {
            categoriaId = Integer.parseInt(categoriaTxt.trim());
        } catch (NumberFormatException e) {
            incidencias.add(inicio + "la categoriaId debe ser un número entero.");
            return null;
        }
        if (categoriaId <= 0) {
            incidencias.add(inicio + "la categoriaId debe ser positiva.");
            return null;
        }

        // --- proveedorId ---
        if (proveedorTxt == null) {
            incidencias.add(inicio + "el proveedorId es obligatorio.");
            return null;
        }
        int proveedorId;
        try {
            proveedorId = Integer.parseInt(proveedorTxt.trim());
        } catch (NumberFormatException e) {
            incidencias.add(inicio + "el proveedorId debe ser un número entero.");
            return null;
        }
        if (proveedorId <= 0) {
            incidencias.add(inicio + "el proveedorId debe ser positivo.");
            return null;
        }

        // Todo correcto
        return new Producto(codigo, nombre, precio, stock, categoriaId, proveedorId);
    }

    // Añade los productos válidos al inventario y muestra el resumen
    static void terminarImportacion(int encontrados, ArrayList<Producto> nuevos, ArrayList<String> incidencias) {
        inventario.addAll(nuevos);

        System.out.println("Productos encontrados: " + encontrados);
        System.out.println("Importados: " + nuevos.size() + " | Rechazados: " + incidencias.size());
        if (incidencias.isEmpty()) {
            System.out.println("Importación finalizada.");
        } else {
            System.out.println("INCIDENCIAS:");
            for (String incidencia : incidencias) {
                System.out.println("- " + incidencia);
            }
            System.out.println("Importación finalizada con incidencias.");
        }
    }

    // ================================================================
    // RF-FIC-02. IMPORTAR UN CATÁLOGO JSON (Gson)
    // ================================================================

    static void importarJson() {
        System.out.println("===== IMPORTAR JSON =====");
        File fichero = new File(leerTexto("Ruta: ").trim());
        if (!comprobarFicheroLectura(fichero)) {
            return;
        }

        // 1) Leer el fichero entero. Si está mal escrito, no se importa nada.
        JsonElement raiz;
        try (FileReader lector = new FileReader(fichero, StandardCharsets.UTF_8)) {
            raiz = JsonParser.parseReader(lector);
        } catch (JsonParseException e) {
            System.out.println("Error: el JSON está mal formado. No se ha importado ningún producto.");
            return;
        } catch (IOException e) {
            System.out.println("Error al leer el fichero: " + e.getMessage());
            return;
        }

        // 2) La raíz tiene que ser un objeto con el array "productos"
        if (!raiz.isJsonObject()
                || !raiz.getAsJsonObject().has("productos")
                || !raiz.getAsJsonObject().get("productos").isJsonArray()) {
            System.out.println("Error: el JSON debe ser un objeto con el array \"productos\". No se ha importado nada.");
            return;
        }
        JsonArray array = raiz.getAsJsonObject().getAsJsonArray("productos");

        // 3) Recorrer los productos y validarlos uno a uno
        ArrayList<Producto> nuevos = new ArrayList<Producto>();
        ArrayList<String> incidencias = new ArrayList<String>();

        for (int i = 0; i < array.size(); i++) {
            JsonElement elemento = array.get(i);
            if (!elemento.isJsonObject()) {
                incidencias.add("Producto " + (i + 1) + ": no es un objeto JSON.");
                continue;
            }
            JsonObject obj = elemento.getAsJsonObject();

            Producto p = crearProducto(i + 1,
                    leerCampoJson(obj, "codigo"),
                    leerCampoJson(obj, "nombre"),
                    leerCampoJson(obj, "precio"),
                    leerCampoJson(obj, "stock"),
                    leerCampoJson(obj, "categoriaId"),
                    leerCampoJson(obj, "proveedorId"),
                    nuevos, incidencias);
            if (p != null) {
                nuevos.add(p);
            }
        }

        // 4) Añadir los válidos y mostrar el resumen
        terminarImportacion(array.size(), nuevos, incidencias);
    }

    // Devuelve el valor de un campo como texto, o null si no está
    static String leerCampoJson(JsonObject obj, String campo) {
        if (!obj.has(campo) || !obj.get(campo).isJsonPrimitive()) {
            return null;
        }
        return obj.get(campo).getAsString();
    }

    // ================================================================
    // RF-FIC-03. IMPORTAR UN CATÁLOGO XML (DOM)
    // ================================================================

    static void importarXml() {
        System.out.println("===== IMPORTAR XML =====");
        File fichero = new File(leerTexto("Ruta: ").trim());
        if (!comprobarFicheroLectura(fichero)) {
            return;
        }

        // 1) Leer el documento con DOM de forma segura (sin DTD)
        System.out.println("Leyendo documento XML...");
        Document doc;
        try {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            // Prohíbe <!DOCTYPE>: así se evitan los ataques XXE
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder constructor = fabrica.newDocumentBuilder();
            doc = constructor.parse(fichero);
        } catch (SAXException e) {
            System.out.println("Error: XML mal formado o con DTD (no permitida). No se ha importado nada.");
            System.out.println("Detalle: " + e.getMessage());
            return;
        } catch (Exception e) {
            System.out.println("Error al leer el XML: " + e.getMessage());
            return;
        }

        // 2) La raíz tiene que ser <catalogo>
        Element raiz = doc.getDocumentElement();
        if (!raiz.getNodeName().equals("catalogo")) {
            System.out.println("Error: la raíz debe ser <catalogo>. No se ha importado nada.");
            return;
        }

        // 3) Recorrer los <producto>
        NodeList listaProductos = raiz.getElementsByTagName("producto");
        ArrayList<Producto> nuevos = new ArrayList<Producto>();
        ArrayList<String> incidencias = new ArrayList<String>();

        for (int i = 0; i < listaProductos.getLength(); i++) {
            Element elemento = (Element) listaProductos.item(i);

            Producto p = crearProducto(i + 1,
                    leerCampoXml(elemento, "codigo"),
                    leerCampoXml(elemento, "nombre"),
                    leerCampoXml(elemento, "precio"),
                    leerCampoXml(elemento, "stock"),
                    leerCampoXml(elemento, "categoriaId"),
                    leerCampoXml(elemento, "proveedorId"),
                    nuevos, incidencias);
            if (p != null) {
                nuevos.add(p);
            }
        }

        // 4) Añadir los válidos y mostrar el resumen
        terminarImportacion(listaProductos.getLength(), nuevos, incidencias);
    }

    // Devuelve el texto de la etiqueta hija, o null si no existe
    static String leerCampoXml(Element elemento, String etiqueta) {
        NodeList nodos = elemento.getElementsByTagName(etiqueta);
        if (nodos.getLength() == 0) {
            return null;
        }
        return nodos.item(0).getTextContent();
    }

    // ================================================================
    // RF-FIC-04. CONSULTAR Y BUSCAR PRODUCTOS EN MEMORIA
    // ================================================================

    static void menuConsultas() {
        int opcion;
        do {
            System.out.println();
            System.out.println("----------------------------------------------");
            System.out.println(" CONSULTA DE PRODUCTOS");
            System.out.println("----------------------------------------------");
            System.out.println("1. Listar todos los productos");
            System.out.println("2. Buscar producto por código");
            System.out.println("3. Buscar productos por nombre");
            System.out.println("0. Volver al menú principal");
            System.out.println("----------------------------------------------");
            opcion = leerNumero("Selecciona una opción: ");

            switch (opcion) {
                case 1:
                    listarProductos();
                    break;
                case 2:
                    buscarPorCodigo();
                    break;
                case 3:
                    buscarPorNombre();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción no válida.");
                    break;
            }
        } while (opcion != 0);
    }

    // Devuelve una COPIA ordenada por código (método burbuja).
    // Se ordena una copia para no tocar el inventario.
    static ArrayList<Producto> ordenarPorCodigo(ArrayList<Producto> lista) {
        ArrayList<Producto> copia = new ArrayList<Producto>(lista);
        for (int i = 0; i < copia.size() - 1; i++) {
            for (int j = 0; j < copia.size() - 1 - i; j++) {
                if (copia.get(j).getCodigo().compareTo(copia.get(j + 1).getCodigo()) > 0) {
                    Producto aux = copia.get(j);
                    copia.set(j, copia.get(j + 1));
                    copia.set(j + 1, aux);
                }
            }
        }
        return copia;
    }

    static void mostrarProducto(Producto p) {
        System.out.println("Código: " + p.getCodigo());
        System.out.println("Nombre: " + p.getNombre());
        System.out.println("Precio: " + p.getPrecio() + " € | Stock: " + p.getStock());
        System.out.println("Categoría: " + p.getCategoriaId() + " | Proveedor: " + p.getProveedorId());
        System.out.println("-----");
    }

    static void listarProductos() {
        if (inventario.isEmpty()) {
            System.out.println("El inventario está vacío. Importa un catálogo primero.");
            return;
        }
        for (Producto p : ordenarPorCodigo(inventario)) {
            mostrarProducto(p);
        }
        System.out.println("Total de productos: " + inventario.size());
    }

    static void buscarPorCodigo() {
        if (inventario.isEmpty()) {
            System.out.println("El inventario está vacío.");
            return;
        }
        String codigo = leerTexto("Código: ").trim();
        if (codigo.isEmpty()) {
            System.out.println("Error: el código no puede estar vacío.");
            return;
        }
        Producto p = buscarEnLista(inventario, codigo);
        if (p == null) {
            System.out.println("No existe ningún producto con el código " + codigo);
        } else {
            mostrarProducto(p);
        }
    }

    static void buscarPorNombre() {
        if (inventario.isEmpty()) {
            System.out.println("El inventario está vacío.");
            return;
        }
        String texto = leerTexto("Texto a buscar: ").trim();
        if (texto.isEmpty()) {
            System.out.println("Error: el texto de búsqueda no puede estar vacío.");
            return;
        }
        int encontrados = 0;
        for (Producto p : ordenarPorCodigo(inventario)) {
            // Se pasa todo a minúsculas para no distinguir mayúsculas
            if (p.getNombre().toLowerCase().contains(texto.toLowerCase())) {
                mostrarProducto(p);
                encontrados++;
            }
        }
        if (encontrados == 0) {
            System.out.println("No hay productos cuyo nombre contenga \"" + texto + "\".");
        } else {
            System.out.println("Coincidencias: " + encontrados);
        }
    }

    // ================================================================
    // RF-FIC-05. EXPORTAR EL INVENTARIO A JSON Y XML
    // ================================================================

    static void menuExportar() {
        int opcion;
        do {
            System.out.println();
            System.out.println("----------------------------------------------");
            System.out.println(" EXPORTAR PRODUCTOS");
            System.out.println("----------------------------------------------");
            System.out.println("1. Exportar productos a JSON");
            System.out.println("2. Exportar productos a XML");
            System.out.println("0. Volver al menú principal");
            System.out.println("----------------------------------------------");
            opcion = leerNumero("Selecciona una opción: ");

            switch (opcion) {
                case 1:
                    exportarJson();
                    break;
                case 2:
                    exportarXml();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción no válida.");
                    break;
            }
        } while (opcion != 0);
    }

    // Pide la ruta de destino y hace las comprobaciones.
    // Devuelve el File, o null si hay error o el usuario cancela.
    static File pedirFicheroDestino(String extension) {
        String ruta = leerTexto("Ruta: ").trim();
        if (ruta.isEmpty()) {
            System.out.println("Error: la ruta no puede estar vacía.");
            return null;
        }
        if (!ruta.toLowerCase().endsWith(extension)) {
            System.out.println("Error: el fichero debe terminar en " + extension);
            return null;
        }

        File destino = new File(ruta);
        File carpeta = destino.getAbsoluteFile().getParentFile();
        if (carpeta == null || !carpeta.isDirectory()) {
            System.out.println("Error: el directorio de destino no existe.");
            return null;
        }
        if (destino.isDirectory()) {
            System.out.println("Error: esa ruta es un directorio.");
            return null;
        }
        if (destino.exists()) {
            if (!confirmar("El fichero ya existe. ¿Sobrescribirlo?")) {
                System.out.println("Operación cancelada. El fichero no se ha modificado.");
                return null;
            }
        }
        return destino;
    }

    static void exportarJson() {
        System.out.println("===== EXPORTAR A JSON =====");
        if (inventario.isEmpty()) {
            System.out.println("No hay productos para exportar.");
            return;
        }
        File destino = pedirFicheroDestino(".json");
        if (destino == null) {
            return;
        }

        // Construimos { "productos": [ {...}, {...} ] }
        JsonArray array = new JsonArray();
        for (Producto p : ordenarPorCodigo(inventario)) {
            JsonObject obj = new JsonObject();
            obj.addProperty("codigo", p.getCodigo());
            obj.addProperty("nombre", p.getNombre());
            obj.addProperty("precio", p.getPrecio());
            obj.addProperty("stock", p.getStock());
            obj.addProperty("categoriaId", p.getCategoriaId());
            obj.addProperty("proveedorId", p.getProveedorId());
            array.add(obj);
        }
        JsonObject raiz = new JsonObject();
        raiz.add("productos", array);

        // setPrettyPrinting = con saltos de línea y sangría
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        try (FileWriter escritor = new FileWriter(destino, StandardCharsets.UTF_8)) {
            gson.toJson(raiz, escritor);
        } catch (IOException e) {
            System.out.println("Error al escribir el fichero: " + e.getMessage());
            return;
        }
        System.out.println("Fichero generado correctamente: " + destino.getAbsolutePath());
        System.out.println("Formato: JSON | Productos exportados: " + inventario.size());
    }

    static void exportarXml() {
        System.out.println("===== EXPORTAR A XML =====");
        if (inventario.isEmpty()) {
            System.out.println("No hay productos para exportar.");
            return;
        }
        File destino = pedirFicheroDestino(".xml");
        if (destino == null) {
            return;
        }

        try {
            // 1) Crear un documento vacío
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            DocumentBuilder constructor = fabrica.newDocumentBuilder();
            Document doc = constructor.newDocument();

            // 2) <catalogo> con un <producto> por cada objeto
            Element raiz = doc.createElement("catalogo");
            doc.appendChild(raiz);
            for (Producto p : ordenarPorCodigo(inventario)) {
                Element producto = doc.createElement("producto");
                raiz.appendChild(producto);
                anadirEtiqueta(doc, producto, "codigo", p.getCodigo());
                anadirEtiqueta(doc, producto, "nombre", p.getNombre());
                anadirEtiqueta(doc, producto, "precio", p.getPrecio().toPlainString());
                anadirEtiqueta(doc, producto, "stock", String.valueOf(p.getStock()));
                anadirEtiqueta(doc, producto, "categoriaId", String.valueOf(p.getCategoriaId()));
                anadirEtiqueta(doc, producto, "proveedorId", String.valueOf(p.getProveedorId()));
            }

            // 3) Guardar el documento en el fichero, con sangría y UTF-8
            Transformer transformador = TransformerFactory.newInstance().newTransformer();
            transformador.setOutputProperty(OutputKeys.INDENT, "yes");
            transformador.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformador.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformador.transform(new DOMSource(doc), new StreamResult(destino));
        } catch (Exception e) {
            System.out.println("Error al generar el XML: " + e.getMessage());
            return;
        }
        System.out.println("Fichero generado correctamente: " + destino.getAbsolutePath());
        System.out.println("Formato: XML | Productos exportados: " + inventario.size());
    }

    // Crea <etiqueta>texto</etiqueta> dentro de padre
    static void anadirEtiqueta(Document doc, Element padre, String etiqueta, String texto) {
        Element hijo = doc.createElement(etiqueta);
        hijo.setTextContent(texto);
        padre.appendChild(hijo);
    }

    // ================================================================
    // RF-FIC-06. GUARDAR Y RECUPERAR EL INVENTARIO (serialización)
    // ================================================================

    static void menuGuardarRecuperar() {
        int opcion;
        do {
            System.out.println();
            System.out.println("----------------------------------------------");
            System.out.println(" GUARDAR Y RECUPERAR INVENTARIO");
            System.out.println("----------------------------------------------");
            System.out.println("1. Guardar inventario en fichero .dat");
            System.out.println("2. Recuperar inventario desde fichero .dat");
            System.out.println("0. Volver al menú principal");
            System.out.println("----------------------------------------------");
            opcion = leerNumero("Selecciona una opción: ");

            switch (opcion) {
                case 1:
                    guardarInventario();
                    break;
                case 2:
                    recuperarInventario();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción no válida.");
                    break;
            }
        } while (opcion != 0);
    }

    static void guardarInventario() {
        System.out.println("===== GUARDAR INVENTARIO =====");
        if (inventario.isEmpty()) {
            System.out.println("No hay productos en memoria. No se crea ningún fichero.");
            return;
        }
        File destino = pedirFicheroDestino(".dat");
        if (destino == null) {
            return;
        }

        // writeObject guarda la lista entera (con todos sus productos)
        try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(destino))) {
            salida.writeObject(inventario);
        } catch (IOException e) {
            System.out.println("Error al guardar: " + e.getMessage());
            return;
        }
        System.out.println("Inventario guardado correctamente en " + destino.getAbsolutePath());
        System.out.println("Productos guardados: " + inventario.size());
    }

    static void recuperarInventario() {
        System.out.println("===== RECUPERAR INVENTARIO =====");
        String ruta = leerTexto("Ruta: ").trim();
        File fichero = new File(ruta);
        if (!comprobarFicheroLectura(fichero)) {
            return;
        }
        if (!ruta.toLowerCase().endsWith(".dat")) {
            System.out.println("Error: solo se recuperan ficheros .dat generados por DataStock.");
            return;
        }

        // 1) Leemos primero a una lista auxiliar (el inventario aún no se toca)
        ArrayList<Producto> recuperados = new ArrayList<Producto>();
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(fichero))) {
            Object objeto = entrada.readObject();

            // 2) Comprobar que es una lista de productos
            if (!(objeto instanceof ArrayList)) {
                System.out.println("Error: el fichero no contiene un inventario de DataStock.");
                return;
            }
            for (Object o : (ArrayList<?>) objeto) {
                if (!(o instanceof Producto)) {
                    System.out.println("Error: el fichero contiene objetos que no son productos.");
                    return;
                }
                Producto p = (Producto) o;
                if (p.getCodigo() == null || p.getNombre() == null || p.getPrecio() == null
                        || p.getPrecio().compareTo(BigDecimal.ZERO) <= 0 || p.getStock() < 0) {
                    System.out.println("Error: el fichero contiene productos con datos incorrectos.");
                    return;
                }
                recuperados.add(p);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Error: el fichero no es compatible con DataStock.");
            return;
        } catch (IOException e) {
            System.out.println("Error: el fichero está dañado o no se puede leer. El inventario no cambia.");
            return;
        }

        // 3) Si ya hay productos, pedir confirmación antes de sustituir
        if (!inventario.isEmpty()) {
            System.out.println("Actualmente hay " + inventario.size() + " productos en memoria.");
            System.out.println("La recuperación sustituirá el inventario actual.");
            if (!confirmar("¿Continuar?")) {
                System.out.println("Operación cancelada. Se conserva el inventario actual.");
                return;
            }
        }

        // 4) Todo ha ido bien: ahora sí sustituimos
        inventario.clear();
        inventario.addAll(recuperados);
        System.out.println("Inventario recuperado correctamente.");
        System.out.println("Productos disponibles: " + inventario.size());
    }
}
