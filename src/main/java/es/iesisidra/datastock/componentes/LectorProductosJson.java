package es.iesisidra.datastock.componentes;

import es.iesisidra.datastock.modelo.Producto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Componente que lee productos de un fichero JSON (UT3, componente de ficheros).
 * <p>
 * El fichero debe tener este formato:
 * <pre>
 * { "productos": [ { "codigo": "TEC-001", "nombre": "Teclado", "precio": 19.95,
 *                    "stock": 12, "categoriaId": 1, "proveedorId": 1 }, ... ] }
 * </pre>
 * Valida cada producto con las mismas reglas que la importación de la UT1:
 * código obligatorio de 1 a 12 caracteres (A-Z, 0-9 y guion, sin repetir en el fichero),
 * nombre de 3 a 100 caracteres, precio mayor que 0, stock entero mayor o igual que 0
 * y categoriaId y proveedorId enteros positivos.
 * <p>
 * El componente <b>no</b> usa la consola (ni {@code Scanner} ni {@code System.out}),
 * <b>no</b> modifica el inventario y <b>no</b> guarda nada en MySQL: solo lee y devuelve
 * el resultado. Quien lo usa decide qué hacer con los productos.
 * <p>
 * Ejemplo de uso:
 * <pre>
 * LectorProductosJson lector = new LectorProductosJson();
 * ResultadoLecturaJson r = lector.leer(Path.of("datos/pruebas/json/tres_validos.json"));
 * List&lt;Producto&gt; validos = r.getProductos();
 * </pre>
 */
public class LectorProductosJson {

    /**
     * Crea un lector de productos JSON. No necesita ninguna configuración.
     */
    public LectorProductosJson() {
    }

    /**
     * Lee un fichero JSON y devuelve sus productos válidos y los motivos de rechazo de los demás.
     * <p>
     * Si el fichero entero no se puede usar (no existe, está mal formado o no tiene
     * el array {@code productos}) no se devuelve ningún producto: se lanza una excepción.
     * Si el fichero es correcto pero algún producto tiene datos incorrectos, ese producto
     * se rechaza y su motivo se añade a {@link ResultadoLecturaJson#getIncidencias()}.
     *
     * @param ruta ruta del fichero JSON que se quiere leer
     * @return resultado con el número de productos encontrados, los válidos y las incidencias
     * @throws IOException si la ruta es nula o vacía, si el fichero no existe, es un directorio,
     *                     no se puede leer, el JSON está mal formado o la raíz no es un objeto
     *                     con el array {@code productos}. El mensaje explica el motivo.
     */
    public ResultadoLecturaJson leer(Path ruta) throws IOException {
        // 1) Comprobar el fichero
        if (ruta == null || ruta.toString().trim().isEmpty()) {
            throw new IOException("La ruta no puede estar vacía.");
        }
        if (!Files.exists(ruta)) {
            throw new IOException("El fichero no existe: " + ruta);
        }
        if (Files.isDirectory(ruta)) {
            throw new IOException("La ruta es un directorio, no un fichero: " + ruta);
        }
        if (!Files.isReadable(ruta)) {
            throw new IOException("No hay permisos para leer el fichero: " + ruta);
        }

        // 2) Leer el fichero entero. Si está mal escrito, no se devuelve nada.
        JsonElement raiz;
        try (Reader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            raiz = JsonParser.parseReader(lector);
        } catch (JsonParseException e) {
            throw new IOException("El JSON está mal formado. No se ha importado ningún producto.");
        }

        // 3) La raíz tiene que ser un objeto con el array "productos"
        if (!raiz.isJsonObject()
                || !raiz.getAsJsonObject().has("productos")
                || !raiz.getAsJsonObject().get("productos").isJsonArray()) {
            throw new IOException("El JSON debe ser un objeto con el array \"productos\". No se ha importado ningún producto.");
        }
        JsonArray array = raiz.getAsJsonObject().getAsJsonArray("productos");

        // 4) Validar los productos uno a uno
        List<Producto> validos = new ArrayList<Producto>();
        List<String> incidencias = new ArrayList<String>();

        for (int i = 0; i < array.size(); i++) {
            JsonElement elemento = array.get(i);
            if (!elemento.isJsonObject()) {
                incidencias.add("Producto " + (i + 1) + ": no es un objeto JSON.");
                continue;
            }
            JsonObject obj = elemento.getAsJsonObject();
            Producto p = crearProducto(i + 1,
                    leerCampo(obj, "codigo"),
                    leerCampo(obj, "nombre"),
                    leerCampo(obj, "precio"),
                    leerCampo(obj, "stock"),
                    leerCampo(obj, "categoriaId"),
                    leerCampo(obj, "proveedorId"),
                    validos, incidencias);
            if (p != null) {
                validos.add(p);
            }
        }

        return new ResultadoLecturaJson(array.size(), validos, incidencias);
    }

    // Devuelve el valor de un campo como texto, o null si no está
    private String leerCampo(JsonObject obj, String campo) {
        if (!obj.has(campo) || !obj.get(campo).isJsonPrimitive()) {
            return null;
        }
        return obj.get(campo).getAsString();
    }

    // Busca un código en una lista sin distinguir mayúsculas
    private boolean contieneCodigo(List<Producto> lista, String codigo) {
        for (Producto p : lista) {
            if (p.getCodigo().equalsIgnoreCase(codigo)) {
                return true;
            }
        }
        return false;
    }

    // Valida los 6 campos (llegan como texto) y crea el Producto.
    // Si algo falla, apunta la incidencia y devuelve null.
    // Son las mismas reglas que en la UT1 (App.crearProducto).
    private Producto crearProducto(int num, String codigo, String nombre, String precioTxt,
                                   String stockTxt, String categoriaTxt, String proveedorTxt,
                                   List<Producto> validos, List<String> incidencias) {

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
        if (contieneCodigo(validos, codigo)) {
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
}
    