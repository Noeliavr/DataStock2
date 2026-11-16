package es.iesisidra.datastock.componentes;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.ProductoDetalle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Componente que consulta un producto de MySQL por su código (UT3, componente de base de datos).
 * <p>
 * Usa JDBC con {@link PreparedStatement} y {@link ResultSet}. La conexión la obtiene de
 * {@link ConexionBD}, que lee los datos de {@code config/bd.properties}.
 * Todos los recursos (conexión, sentencia y resultado) se cierran con try-with-resources.
 * <p>
 * El componente <b>no</b> muestra mensajes por consola: devuelve el resultado y
 * quien lo usa decide cómo presentarlo.
 * <p>
 * Ejemplo de uso:
 * <pre>
 * ConsultaProductoJdbc consulta = new ConsultaProductoJdbc();
 * Optional&lt;ProductoDetalle&gt; r = consulta.buscarPorCodigo("TEC-001");
 * if (r.isPresent()) { ... r.get() ... } else { ... no existe ... }
 * </pre>
 */
public class ConsultaProductoJdbc {

    // JOIN para traer también los nombres de la categoría y del proveedor
    private static final String SQL_POR_CODIGO =
            "SELECT p.codigo, p.nombre, p.precio, p.stock, p.categoria_id, p.proveedor_id, "
            + "c.nombre AS categoria, pr.nombre AS proveedor "
            + "FROM producto p "
            + "JOIN categoria c ON p.categoria_id = c.id "
            + "JOIN proveedor pr ON p.proveedor_id = pr.id "
            + "WHERE p.codigo = ?";

    /**
     * Crea el componente de consulta. La conexión se abre en cada búsqueda,
     * con los datos de {@code config/bd.properties}.
     */
    public ConsultaProductoJdbc() {
    }

    /**
     * Busca en MySQL el producto que tiene ese código.
     * <p>
     * Antes de buscar, al código se le quitan los espacios de los extremos y se pasa a mayúsculas.
     *
     * @param codigo código del producto, por ejemplo {@code "TEC-001"}
     * @return un {@link Optional} con el producto (incluidos los nombres de su categoría y su
     *         proveedor) si existe, o un {@code Optional} vacío si no hay ningún producto con ese código
     * @throws IllegalArgumentException si el código es {@code null} o está vacío
     * @throws SQLException si no se puede conectar con MySQL o falla la consulta
     *                      (por ejemplo, contraseña incorrecta o servidor apagado)
     */
    public Optional<ProductoDetalle> buscarPorCodigo(String codigo) throws SQLException {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código no puede estar vacío.");
        }

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_POR_CODIGO)) {
            ps.setString(1, codigo.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ProductoDetalle p = new ProductoDetalle(
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getBigDecimal("precio"),
                            rs.getInt("stock"),
                            rs.getInt("categoria_id"),
                            rs.getInt("proveedor_id"),
                            rs.getString("categoria"),
                            rs.getString("proveedor"));
                    return Optional.of(p);
                }
                return Optional.empty(); // no existe
            }
        }
    }
}
