package es.iesisidra.datastock.modelo;

import java.math.BigDecimal;

// Producto leído de MySQL con los NOMBRES de su categoría y su proveedor (obtenidos con JOIN).
// Hereda todos los campos de Producto y añade dos más.
public class ProductoDetalle extends Producto {

    private String nombreCategoria;
    private String nombreProveedor;

    public ProductoDetalle(String codigo, String nombre, BigDecimal precio, int stock,
                           int categoriaId, int proveedorId,
                           String nombreCategoria, String nombreProveedor) {
        super(codigo, nombre, precio, stock, categoriaId, proveedorId);
        this.nombreCategoria = nombreCategoria;
        this.nombreProveedor = nombreProveedor;
    }

    public String getNombreCategoria() { return nombreCategoria; }
    public String getNombreProveedor() { return nombreProveedor; }
}
