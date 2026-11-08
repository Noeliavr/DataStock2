package es.iesisidra.datastock;

import java.sql.SQLException;

import es.iesisidra.datastock.bd.OperacionesBD;
import es.iesisidra.datastock.util.Colores;


public class MenuMySQL {

    public static void mostrarMenu() {
        int opcion;
        do {
            System.out.println();
            System.out.println("================================================");
            System.out.println(" GESTIÓN MYSQL - UT2");
            System.out.println("================================================");
            System.out.println("1. Comprobar conexión y mostrar información de la BD");
            System.out.println("2. Gestionar categorías");
            System.out.println("3. Gestionar proveedores");
            System.out.println("4. Gestionar productos (CRUD)");
            System.out.println("5. Consultar y buscar productos");
            System.out.println("6. Registrar entrada o salida de stock");
            System.out.println("7. Consultar historial de movimientos");
            System.out.println("8. Importar productos de memoria a MySQL");
            System.out.println("9. Demostración de transacción con commit/rollback");
            System.out.println("0. Volver al menú principal");
            System.out.println("------------------------------------------------");
            opcion = App.leerNumero("Selecciona una opción: ");

            switch (opcion) {
                case 1:
                    comprobarConexion();
                    break;
                case 0:
                    break;
                default:
                    Colores.error("Opción no válida o todavía no hecha.");
                    break;
            }
        } while (opcion != 0);
    }


    static boolean confirmar(String pregunta) {
        return App.confirmar(Colores.AMARILLO + "[AVISO] " + pregunta + Colores.RESET);
    }

    // Traduce los errores de MySQL a mensajes comprensibles (sin mostrar la contraseña)
    static String explicarError(SQLException e) {
        int codigo = e.getErrorCode();
        String estado = e.getSQLState();
        String mensaje = e.getMessage();

        if (mensaje != null && mensaje.contains("No suitable driver")) {
            return "Falta el conector MySQL (revisa la dependencia mysql-connector-j del pom.xml).";
        }
        if (mensaje != null && mensaje.contains("Public Key Retrieval")) {
            return "MySQL pide permitir la clave pública: añade &allowPublicKeyRetrieval=true al final de la url en config/bd.properties.";
        }
        if (codigo == 1045) {
            return "Usuario o contraseña incorrectos. Revisa config/bd.properties.";
        }
        if (codigo == 1049) {
            return "La base de datos datastock no existe. Ejecuta el script DataStock_BD_MySQL.sql.";
        }
        if (estado != null && estado.startsWith("08")) {
            return "No se puede conectar con MySQL. Comprueba que el servidor está arrancado.";
        }
        if (codigo == 1062) {
            return "Ese valor ya existe en la base de datos (dato duplicado).";
        }
        if (codigo == 1451) {
            return "No se puede borrar porque otros registros dependen de él (clave foránea).";
        }
        if (codigo == 1452) {
            return "La categoría o el proveedor indicado no existe (clave foránea).";
        }
        if (codigo == 3819) {
            return "Los datos no cumplen una restricción CHECK de la tabla.";
        }
        if ("45000".equals(estado)) {
            return mensaje; // mensajes del procedimiento: Stock insuficiente, Producto inexistente...
        }
        if (estado == null) {
            return mensaje; // errores nuestros, por ejemplo falta config/bd.properties
        }
        return "Error de base de datos: " + mensaje;
    }

    // ================================================================
    // 7.1  RF-JDBC-01. COMPROBAR CONEXIÓN
    // ================================================================

    static void comprobarConexion() {
        System.out.println("===== COMPROBAR CONEXIÓN =====");
        try {
            String[] datos = OperacionesBD.infoConexion();
            Colores.ok("Conexión establecida correctamente.");
            System.out.println("Base de datos: " + datos[0]);
            System.out.println("Gestor: " + datos[1]);
            System.out.println("Versión: " + datos[2]);
            System.out.println("Categorías: " + datos[3] + " | Proveedores: " + datos[4]);
            System.out.println("Productos almacenados: " + datos[5] + " | Movimientos: " + datos[6]);
        } catch (SQLException e) {
            Colores.error(explicarError(e));
        }
    }
}