package es.iesisidra.datastock.bd;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Categoria;

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
}
