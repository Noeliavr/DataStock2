package es.iesisidra.datastock.bd;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import es.iesisidra.datastock.config.ConexionBD;

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
}
