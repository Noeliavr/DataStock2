package es.iesisidra.datastock.componentes;

import es.iesisidra.datastock.modelo.Producto;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de leer un catálogo JSON con {@link LectorProductosJson}.
 * <p>
 * Guarda cuántos productos había en el fichero, los productos válidos
 * y los motivos por los que se rechazaron los demás.
 * Solo contiene datos: no imprime nada por consola.
 */
public class ResultadoLecturaJson {

    private final int encontrados;
    private final List<Producto> productos;
    private final List<String> incidencias;

    /**
     * Crea un resultado de lectura.
     *
     * @param encontrados número de productos que contenía el array {@code productos} del fichero
     * @param productos   productos que han superado todas las validaciones
     * @param incidencias un texto por cada producto rechazado, con el motivo
     */
    public ResultadoLecturaJson(int encontrados, List<Producto> productos, List<String> incidencias) {
        this.encontrados = encontrados;
        this.productos = new ArrayList<Producto>(productos);
        this.incidencias = new ArrayList<String>(incidencias);
    }

    /**
     * Devuelve cuántos productos había en el fichero.
     *
     * @return número de productos que había en el fichero (válidos más rechazados)
     */
    public int getEncontrados() {
        return encontrados;
    }

    /**
     * Devuelve los productos que han superado todas las validaciones.
     *
     * @return lista con los productos válidos (puede estar vacía, nunca es {@code null})
     */
    public List<Producto> getProductos() {
        return productos;
    }

    /**
     * Devuelve el motivo de cada producto rechazado.
     *
     * @return lista con el motivo de cada producto rechazado (vacía si no hubo rechazos)
     */
    public List<String> getIncidencias() {
        return incidencias;
    }
}
