package es.iesisidra.datastock.complementarias;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import es.iesisidra.datastock.util.Colores;

// Actividad comparativa (UT2): SQLite, un gestor EMBEBIDO.
// No hay servidor: la base de datos es un fichero dentro del proyecto.
// Programa independiente con su propio main (no está en los menús de DataStock).
public class DemoSQLite {

    static final String RUTA_FICHERO = "datos/sqlite/productos_demo.db";
    static final String URL = "jdbc:sqlite:" + RUTA_FICHERO;

    public static void main(String[] args) {
        // La carpeta tiene que existir; el fichero .db lo crea SQLite al conectar
        new File("datos/sqlite").mkdirs();

        String[] codigos = {"SQL-001", "SQL-002"};
        String[] nombres = {"Producto demo A", "Producto demo B"};

        // Sin usuario ni contraseña: solo la ruta del fichero
        try (Connection con = DriverManager.getConnection(URL);
             Statement st = con.createStatement()) {

            Colores.ok("Conexión SQLite abierta: " + RUTA_FICHERO);

            st.executeUpdate("CREATE TABLE IF NOT EXISTS producto_demo ("
                    + "codigo TEXT PRIMARY KEY NOT NULL, "
                    + "nombre TEXT NOT NULL)");
            Colores.ok("Tabla producto_demo disponible.");

            // INSERT OR IGNORE: si el código ya existe, no inserta y devuelve 0
            int insertados = 0;
            int existentes = 0;
            String sql = "INSERT OR IGNORE INTO producto_demo (codigo, nombre) VALUES (?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (int i = 0; i < codigos.length; i++) {
                    ps.setString(1, codigos[i]);
                    ps.setString(2, nombres[i]);
                    if (ps.executeUpdate() == 1) {
                        insertados++;
                    } else {
                        existentes++;
                    }
                }
            }
            System.out.println("Insertados: " + insertados + " | Ya existentes: " + existentes);

            try (ResultSet rs = st.executeQuery("SELECT codigo, nombre FROM producto_demo ORDER BY codigo")) {
                while (rs.next()) {
                    System.out.println(rs.getString("codigo") + " | " + rs.getString("nombre"));
                }
            }
        } catch (SQLException e) {
            Colores.error("Error con SQLite: " + e.getMessage());
            return;
        }

        // Aquí la conexión ya está cerrada (try-with-resources) y el fichero sigue existiendo
        File fichero = new File(RUTA_FICHERO);
        Colores.ok("Conexión cerrada. El fichero sigue en disco: " + fichero.getAbsolutePath()
                + " (" + fichero.length() + " bytes)");
    }
}