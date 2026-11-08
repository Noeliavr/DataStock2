package es.iesisidra.datastock.config;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {
    private static final String FICHERO = "config/bd.properties";

    public static Connection getConexion() throws SQLException {
        Properties datos = new Properties();
        try (FileInputStream entrada = new FileInputStream(FICHERO)){
            datos.load(entrada);
        } catch (Exception e) {
            throw new SQLException("No se encuentra "+ FICHERO + ". Copia config/bd.properties.ejemplo como bd.properties y pon tus datos.");
        }
        String url = datos.getProperty("url");
        String usuario = datos.getProperty("usuario");
        String contrasena = datos.getProperty("contrasena");
        if (url == null || usuario == null || contrasena == null) {
            throw new SQLException("Faltan datos en " + FICHERO + " (url, usuario y contrasena).");
        }

        return DriverManager.getConnection(url, usuario, contrasena);
    }
}
