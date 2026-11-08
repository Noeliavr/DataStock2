package es.iesisidra.datastock.bd;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.sql.Types;
import java.sql.CallableStatement;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Categoria;
import es.iesisidra.datastock.modelo.MovimientoStock;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.modelo.ProductoDetalle;
import es.iesisidra.datastock.modelo.Proveedor;

public class OperacionesBD {
   public static String[] infoConexion() throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM categoria), (SELECT COUNT(*) FROM proveedor), "
                + "(SELECT COUNT(*) FROM producto), (SELECT COUNT(*) FROM movimiento_stock)";
        try (Connection con = ConexionBD.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            DatabaseMetaData meta = con.getMetaData();
            String[] datos = new String[7];
            datos[0] = con.getCatalog();
            datos[1] = meta.getDatabaseProductName();
            datos[2] = meta.getDatabaseProductVersion();
            rs.next();
            datos[3] = String.valueOf(rs.getInt(1));
            datos[4] = String.valueOf(rs.getInt(2));
            datos[5] = String.valueOf(rs.getInt(3));
            datos[6] = String.valueOf(rs.getInt(4));
            return datos;
        }
    }

    // Devuelve la categoría o null si no existe
    public static Categoria buscarCategoria(int id) throws SQLException {
        String sql = "SELECT id, nombre FROM categoria WHERE id = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Categoria(rs.getInt("id"), rs.getString("nombre"));
                }
                return null;
            }
        }
    }

    // ¿Hay OTRA categoría (con id distinto) que ya use este nombre?
    public static boolean existeNombreCategoria(String nombre, int idExcluido) throws SQLException {
        String sql = "SELECT COUNT(*) FROM categoria WHERE nombre = ? AND id <> ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, idExcluido);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public static void insertarCategoria(Categoria c) throws SQLException {
        String sql = "INSERT INTO categoria (id, nombre) VALUES (?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, c.getId());
            ps.setString(2, c.getNombre());
            ps.executeUpdate();
        }
    }

    public static ArrayList<Categoria> listarCategorias() throws SQLException {
        ArrayList<Categoria> lista = new ArrayList<Categoria>();
        String sql = "SELECT id, nombre FROM categoria ORDER BY id";
        try (Connection con = ConexionBD.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Categoria(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return lista;
    }

    // Devuelve cuántas filas se han modificado (1 si existía, 0 si no)
    public static int modificarCategoria(int id, String nuevoNombre) throws SQLException {
        String sql = "UPDATE categoria SET nombre = ? WHERE id = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoNombre);
            ps.setInt(2, id);
            return ps.executeUpdate();
        }
    }

    public static int eliminarCategoria(int id) throws SQLException {
        String sql = "DELETE FROM categoria WHERE id = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    public static Proveedor buscarProveedor(int id) throws SQLException {
        String sql = "SELECT id, nombre, email FROM proveedor WHERE id = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Proveedor(rs.getInt("id"), rs.getString("nombre"), rs.getString("email"));
                }
                return null;
            }
        }
    }

    // ¿Hay OTRO proveedor que ya use este email?
    public static boolean existeEmail(String email, int idExcluido) throws SQLException {
        String sql = "SELECT COUNT(*) FROM proveedor WHERE email = ? AND id <> ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, idExcluido);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public static void insertarProveedor(Proveedor p) throws SQLException {
        String sql = "INSERT INTO proveedor (id, nombre, email) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getId());
            ps.setString(2, p.getNombre());
            ponerEmail(ps, 3, p.getEmail());
            ps.executeUpdate();
        }
    }

    public static ArrayList<Proveedor> listarProveedores() throws SQLException {
        ArrayList<Proveedor> lista = new ArrayList<Proveedor>();
        String sql = "SELECT id, nombre, email FROM proveedor ORDER BY id";
        try (Connection con = ConexionBD.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Proveedor(rs.getInt("id"), rs.getString("nombre"), rs.getString("email")));
            }
        }
        return lista;
    }

    public static int modificarProveedor(Proveedor p) throws SQLException {
        String sql = "UPDATE proveedor SET nombre = ?, email = ? WHERE id = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ponerEmail(ps, 2, p.getEmail());
            ps.setInt(3, p.getId());
            return ps.executeUpdate();
        }
    }

    public static int eliminarProveedor(int id) throws SQLException {
        String sql = "DELETE FROM proveedor WHERE id = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    // El email es opcional: si es null se guarda NULL en MySQL
    private static void ponerEmail(PreparedStatement ps, int posicion, String email) throws SQLException {
        if (email == null) {
            ps.setNull(posicion, Types.VARCHAR);
        } else {
            ps.setString(posicion, email);
        }
    }


    // SELECT común con JOIN para traer también los nombres de categoría y proveedor
    private static final String SELECT_PRODUCTO =
            "SELECT p.codigo, p.nombre, p.precio, p.stock, p.categoria_id, p.proveedor_id, "
            + "c.nombre AS categoria, pr.nombre AS proveedor "
            + "FROM producto p "
            + "JOIN categoria c ON p.categoria_id = c.id "
            + "JOIN proveedor pr ON p.proveedor_id = pr.id ";

    private static final String INSERT_PRODUCTO =
            "INSERT INTO producto (codigo, nombre, precio, stock, categoria_id, proveedor_id) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    // Convierte la fila actual del ResultSet en un objeto Java
    private static ProductoDetalle leerProducto(ResultSet rs) throws SQLException {
        return new ProductoDetalle(
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getBigDecimal("precio"),
                rs.getInt("stock"),
                rs.getInt("categoria_id"),
                rs.getInt("proveedor_id"),
                rs.getString("categoria"),
                rs.getString("proveedor"));
    }

    // Rellena los 6 ? del INSERT con los datos del producto
    private static void prepararInsert(PreparedStatement ps, Producto p) throws SQLException {
        ps.setString(1, p.getCodigo());
        ps.setString(2, p.getNombre());
        ps.setBigDecimal(3, p.getPrecio());
        ps.setInt(4, p.getStock());
        ps.setInt(5, p.getCategoriaId());
        ps.setInt(6, p.getProveedorId());
    }

    // Devuelve el producto o null si no existe
    public static ProductoDetalle buscarProducto(String codigo) throws SQLException {
        String sql = SELECT_PRODUCTO + "WHERE p.codigo = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return leerProducto(rs);
                }
                return null;
            }
        }
    }

    public static void insertarProducto(Producto p) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(INSERT_PRODUCTO)) {
            prepararInsert(ps, p);
            ps.executeUpdate();
        }
    }

    // Modifica nombre, precio, categoría y proveedor. El stock y el código NO se tocan.
    public static int modificarProducto(Producto p) throws SQLException {
        String sql = "UPDATE producto SET nombre = ?, precio = ?, categoria_id = ?, proveedor_id = ? WHERE codigo = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setBigDecimal(2, p.getPrecio());
            ps.setInt(3, p.getCategoriaId());
            ps.setInt(4, p.getProveedorId());
            ps.setString(5, p.getCodigo());
            return ps.executeUpdate();
        }
    }

    public static int eliminarProducto(String codigo) throws SQLException {
        String sql = "DELETE FROM producto WHERE codigo = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            return ps.executeUpdate();
        }
    }
        public static ArrayList<ProductoDetalle> listarProductos() throws SQLException {
        ArrayList<ProductoDetalle> lista = new ArrayList<ProductoDetalle>();
        String sql = SELECT_PRODUCTO + "ORDER BY p.codigo";
        try (Connection con = ConexionBD.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(leerProducto(rs));
            }
        }
        return lista;
    }

    // Búsqueda parcial: LIKE '%texto%' (el texto va como parámetro, no pegado al SQL)
    public static ArrayList<ProductoDetalle> buscarPorNombre(String texto) throws SQLException {
        ArrayList<ProductoDetalle> lista = new ArrayList<ProductoDetalle>();
        String sql = SELECT_PRODUCTO + "WHERE p.nombre LIKE ? ORDER BY p.codigo";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(leerProducto(rs));
                }
            }
        }
        return lista;
    }

    // Productos con stock estrictamente menor que el umbral
    public static ArrayList<ProductoDetalle> stockInferior(int umbral) throws SQLException {
        ArrayList<ProductoDetalle> lista = new ArrayList<ProductoDetalle>();
        String sql = SELECT_PRODUCTO + "WHERE p.stock < ? ORDER BY p.stock, p.codigo";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, umbral);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(leerProducto(rs));
                }
            }
        }
        return lista;
    }
     // Llama al procedimiento almacenado. Él hace la transacción (UPDATE + INSERT).
    public static void registrarMovimiento(String codigo, String tipo, int cantidad) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             CallableStatement cs = con.prepareCall("{call registrar_movimiento(?, ?, ?)}")) {
            cs.setString(1, codigo);
            cs.setString(2, tipo);
            cs.setInt(3, cantidad);
            cs.execute();
        }
    }

    private static final String SELECT_MOVIMIENTO =
            "SELECT id, producto_codigo, tipo, cantidad, fecha, stock_anterior, stock_posterior "
            + "FROM movimiento_stock ";

    // Convierte la fila actual del ResultSet en un MovimientoStock
    private static MovimientoStock leerMovimiento(ResultSet rs) throws SQLException {
        return new MovimientoStock(
                rs.getLong("id"),
                rs.getString("producto_codigo"),
                MovimientoStock.TipoMovimiento.valueOf(rs.getString("tipo")),
                rs.getInt("cantidad"),
                rs.getTimestamp("fecha").toLocalDateTime(),
                rs.getInt("stock_anterior"),
                rs.getInt("stock_posterior"));
    }

    // Último movimiento de un producto (o null si no tiene)
    public static MovimientoStock ultimoMovimiento(String codigo) throws SQLException {
        String sql = SELECT_MOVIMIENTO + "WHERE producto_codigo = ? ORDER BY id DESC LIMIT 1";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return leerMovimiento(rs);
                }
                return null;
            }
        }
    }

    public static ArrayList<MovimientoStock> listarMovimientos() throws SQLException {
        ArrayList<MovimientoStock> lista = new ArrayList<MovimientoStock>();
        String sql = SELECT_MOVIMIENTO + "ORDER BY fecha, id";
        try (Connection con = ConexionBD.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(leerMovimiento(rs));
            }
        }
        return lista;
    }

    public static ArrayList<MovimientoStock> listarMovimientosDe(String codigo) throws SQLException {
        ArrayList<MovimientoStock> lista = new ArrayList<MovimientoStock>();
        String sql = SELECT_MOVIMIENTO + "WHERE producto_codigo = ? ORDER BY fecha, id";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(leerMovimiento(rs));
                }
            }
        }
        return lista;
    }
    // Inserta todos los productos. Si falla uno, rollback: no se guarda NINGUNO.
    // forzarError = true repite el primer producto al final para provocar
    // un error de clave duplicada (solo para la prueba CP-JDBC-18).
    public static void insertarLote(ArrayList<Producto> productos, boolean forzarError) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(INSERT_PRODUCTO)) {

            con.setAutoCommit(false); // empieza la transacción
            try {
                for (Producto p : productos) {
                    prepararInsert(ps, p);
                    ps.executeUpdate();
                }
                if (forzarError) {
                    prepararInsert(ps, productos.get(0));
                    ps.executeUpdate(); // aquí salta la SQLException (código duplicado)
                }
                con.commit(); // todo bien: se guardan
            } catch (SQLException e) {
                con.rollback(); // algo falló: se deshace todo el lote
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
    // Primer id de categoría libre a partir de "desde"
    public static int primerIdCategoriaLibre(int desde) throws SQLException {
        int id = desde;
        while (buscarCategoria(id) != null) {
            id++;
        }
        return id;
    }

    // Inserta varias categorías en UNA transacción: o se guardan todas o ninguna
    public static void insertarCategoriasEnTransaccion(ArrayList<Categoria> categorias) throws SQLException {
        String sql = "INSERT INTO categoria (id, nombre) VALUES (?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            con.setAutoCommit(false);
            try {
                for (Categoria c : categorias) {
                    ps.setInt(1, c.getId());
                    ps.setString(2, c.getNombre());
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}
