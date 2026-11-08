package es.iesisidra.datastock;

import java.sql.SQLException;
import java.util.ArrayList;

import es.iesisidra.datastock.bd.OperacionesBD;
import es.iesisidra.datastock.modelo.Categoria;
import es.iesisidra.datastock.modelo.Proveedor;
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
                case 2:
                    menuCategorias();
                    break;
                case 3:
                    menuProveedores();
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


    static void menuCategorias() {
        int opcion;
        do {
            System.out.println();
            System.out.println("------------- MENÚ DE CATEGORÍAS -------------");
            System.out.println("1. Dar de alta una categoría");
            System.out.println("2. Listar categorías");
            System.out.println("3. Modificar el nombre de una categoría");
            System.out.println("4. Eliminar una categoría");
            System.out.println("0. Volver al menú MySQL");
            opcion = App.leerNumero("Selecciona una opción: ");

            switch (opcion) {
                case 1:
                    altaCategoria();
                    break;
                case 2:
                    listarCategorias();
                    break;
                case 3:
                    modificarCategoria();
                    break;
                case 4:
                    eliminarCategoria();
                    break;
                case 0:
                    break;
                default:
                    Colores.error("Opción no válida.");
                    break;
            }
        } while (opcion != 0);
    }

    // Comprueba el nombre de categoría (1 a 60 caracteres). Devuelve null si está bien.
    static String validarNombreCategoria(String nombre) {
        if (nombre.isEmpty()) {
            return "El nombre es obligatorio.";
        }
        if (nombre.length() > 60) {
            return "El nombre no puede tener más de 60 caracteres.";
        }
        return null;
    }

    static void altaCategoria() {
        System.out.println("===== ALTA DE CATEGORÍA =====");
        int id = App.leerNumero("ID de categoría: ");
        if (id <= 0) {
            Colores.error("El ID debe ser un número entero positivo.");
            return;
        }
        String nombre = App.leerTexto("Nombre: ").trim();
        String error = validarNombreCategoria(nombre);
        if (error != null) {
            Colores.error(error);
            return;
        }
        try {
            if (OperacionesBD.buscarCategoria(id) != null) {
                Colores.error("Ya existe una categoría con el ID " + id + ".");
                return;
            }
            if (OperacionesBD.existeNombreCategoria(nombre, 0)) {
                Colores.error("Ya existe una categoría llamada \"" + nombre + "\".");
                return;
            }
            OperacionesBD.insertarCategoria(new Categoria(id, nombre));
            Colores.ok("Categoría " + id + " - " + nombre + " registrada correctamente.");
        } catch (SQLException e) {
            Colores.error(explicarError(e));
        }
    }

    static void listarCategorias() {
        System.out.println("===== LISTADO DE CATEGORÍAS =====");
        try {
            ArrayList<Categoria> lista = OperacionesBD.listarCategorias();
            if (lista.isEmpty()) {
                Colores.aviso("No hay categorías registradas.");
                return;
            }
            System.out.println("ID     NOMBRE");
            System.out.println("----------------------------------------");
            for (Categoria c : lista) {
                System.out.println(c.getId() + "      " + c.getNombre());
            }
            System.out.println("Total: " + lista.size());
        } catch (SQLException e) {
            Colores.error(explicarError(e));
        }
    }

    static void modificarCategoria() {
        System.out.println("===== MODIFICAR CATEGORÍA =====");
        int id = App.leerNumero("ID de la categoría: ");
        try {
            Categoria actual = OperacionesBD.buscarCategoria(id);
            if (actual == null) {
                Colores.error("No existe ninguna categoría con el ID " + id + ".");
                return;
            }
            System.out.println("Nombre actual: " + actual.getNombre());
            String nombre = App.leerTexto("Nuevo nombre: ").trim();
            String error = validarNombreCategoria(nombre);
            if (error != null) {
                Colores.error(error);
                return;
            }
            if (OperacionesBD.existeNombreCategoria(nombre, id)) {
                Colores.error("Ya existe otra categoría llamada \"" + nombre + "\".");
                return;
            }
            if (!confirmar("¿Cambiar el nombre a \"" + nombre + "\"?")) {
                Colores.aviso("Operación cancelada. No se ha modificado nada.");
                return;
            }
            int filas = OperacionesBD.modificarCategoria(id, nombre);
            if (filas == 1) {
                Colores.ok("Categoría " + id + " modificada correctamente.");
            } else {
                Colores.error("No se ha modificado ninguna fila (la categoría ya no existe).");
            }
        } catch (SQLException e) {
            Colores.error(explicarError(e));
        }
    }

    static void eliminarCategoria() {
        System.out.println("===== ELIMINAR CATEGORÍA =====");
        int id = App.leerNumero("ID de la categoría: ");
        try {
            Categoria actual = OperacionesBD.buscarCategoria(id);
            if (actual == null) {
                Colores.error("No existe ninguna categoría con el ID " + id + ".");
                return;
            }
            System.out.println("Categoría: " + actual.getId() + " - " + actual.getNombre());
            if (!confirmar("¿Confirmas la eliminación?")) {
                Colores.aviso("Operación cancelada. No se ha borrado nada.");
                return;
            }
            int filas = OperacionesBD.eliminarCategoria(id);
            if (filas == 1) {
                Colores.ok("Categoría " + id + " eliminada correctamente.");
            } else {
                Colores.error("No se ha borrado ninguna fila (la categoría ya no existe).");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                Colores.error("La categoría tiene productos asociados. MySQL impide borrarla (clave foránea).");
            } else {
                Colores.error(explicarError(e));
            }
        }
    }

    static void menuProveedores() {
        int opcion;
        do {
            System.out.println();
            System.out.println("------------- MENÚ DE PROVEEDORES -------------");
            System.out.println("1. Dar de alta un proveedor");
            System.out.println("2. Listar proveedores");
            System.out.println("3. Modificar un proveedor");
            System.out.println("4. Eliminar un proveedor");
            System.out.println("0. Volver al menú MySQL");
            opcion = App.leerNumero("Selecciona una opción: ");

            switch (opcion) {
                case 1:
                    altaProveedor();
                    break;
                case 2:
                    listarProveedores();
                    break;
                case 3:
                    modificarProveedor();
                    break;
                case 4:
                    eliminarProveedor();
                    break;
                case 0:
                    break;
                default:
                    Colores.error("Opción no válida.");
                    break;
            }
        } while (opcion != 0);
    }

    static String validarNombreProveedor(String nombre) {
        if (nombre.isEmpty()) {
            return "El nombre es obligatorio.";
        }
        if (nombre.length() > 100) {
            return "El nombre no puede tener más de 100 caracteres.";
        }
        return null;
    }

    // Formato básico: algo@algo.algo, sin espacios
    static String validarEmail(String email) {
        if (email.length() > 120) {
            return "El email no puede tener más de 120 caracteres.";
        }
        if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            return "El email no tiene un formato válido (ejemplo: nombre@empresa.com).";
        }
        return null;
    }

    static void altaProveedor() {
        System.out.println("===== ALTA DE PROVEEDOR =====");
        int id = App.leerNumero("ID de proveedor: ");
        if (id <= 0) {
            Colores.error("El ID debe ser un número entero positivo.");
            return;
        }
        String nombre = App.leerTexto("Nombre: ").trim();
        String error = validarNombreProveedor(nombre);
        if (error != null) {
            Colores.error(error);
            return;
        }
        String email = App.leerTexto("Email (opcional, Intro para dejarlo vacío): ").trim();
        if (email.isEmpty()) {
            email = null; // se guardará como NULL
        } else {
            error = validarEmail(email);
            if (error != null) {
                Colores.error(error);
                return;
            }
        }
        try {
            if (OperacionesBD.buscarProveedor(id) != null) {
                Colores.error("Ya existe un proveedor con el ID " + id + ".");
                return;
            }
            if (email != null && OperacionesBD.existeEmail(email, 0)) {
                Colores.error("El email " + email + " ya está registrado en otro proveedor.");
                return;
            }
            OperacionesBD.insertarProveedor(new Proveedor(id, nombre, email));
            Colores.ok("Proveedor " + id + " - " + nombre + " registrado correctamente.");
        } catch (SQLException e) {
            Colores.error(explicarError(e));
        }
    }

    static void listarProveedores() {
        System.out.println("===== LISTADO DE PROVEEDORES =====");
        try {
            ArrayList<Proveedor> lista = OperacionesBD.listarProveedores();
            if (lista.isEmpty()) {
                Colores.aviso("No hay proveedores registrados.");
                return;
            }
            System.out.println("ID     NOMBRE  |  EMAIL");
            System.out.println("----------------------------------------");
            for (Proveedor p : lista) {
                String email = p.getEmail();
                if (email == null) {
                    email = "—";
                }
                System.out.println(p.getId() + "      " + p.getNombre() + "  |  " + email);
            }
            System.out.println("Total: " + lista.size());
        } catch (SQLException e) {
            Colores.error(explicarError(e));
        }
    }

    static void modificarProveedor() {
        System.out.println("===== MODIFICAR PROVEEDOR =====");
        int id = App.leerNumero("ID del proveedor: ");
        try {
            Proveedor actual = OperacionesBD.buscarProveedor(id);
            if (actual == null) {
                Colores.error("No existe ningún proveedor con el ID " + id + ".");
                return;
            }
            String emailActual = actual.getEmail();
            if (emailActual == null) {
                emailActual = "—";
            }
            System.out.println("Nombre actual: " + actual.getNombre());
            System.out.println("Email actual: " + emailActual);
            System.out.println("(Pulsa Intro para conservar el valor actual. En el email, escribe - para dejarlo vacío.)");

            // Nombre
            String nombre = App.leerTexto("Nuevo nombre: ").trim();
            if (nombre.isEmpty()) {
                nombre = actual.getNombre();
            } else {
                String error = validarNombreProveedor(nombre);
                if (error != null) {
                    Colores.error(error);
                    return;
                }
            }

            // Email
            String email = App.leerTexto("Nuevo email: ").trim();
            if (email.isEmpty()) {
                email = actual.getEmail(); // conservar
            } else if (email.equals("-")) {
                email = null; // quitar el email
            } else {
                String error = validarEmail(email);
                if (error != null) {
                    Colores.error(error);
                    return;
                }
                if (OperacionesBD.existeEmail(email, id)) {
                    Colores.error("El email " + email + " ya está registrado en otro proveedor.");
                    return;
                }
            }

            if (!confirmar("¿Guardar los cambios del proveedor " + id + "?")) {
                Colores.aviso("Operación cancelada. No se ha modificado nada.");
                return;
            }
            int filas = OperacionesBD.modificarProveedor(new Proveedor(id, nombre, email));
            if (filas == 1) {
                Colores.ok("Proveedor " + id + " modificado correctamente.");
            } else {
                Colores.error("No se ha modificado ninguna fila (el proveedor ya no existe).");
            }
        } catch (SQLException e) {
            Colores.error(explicarError(e));
        }
    }

    static void eliminarProveedor() {
        System.out.println("===== ELIMINAR PROVEEDOR =====");
        int id = App.leerNumero("ID del proveedor: ");
        try {
            Proveedor actual = OperacionesBD.buscarProveedor(id);
            if (actual == null) {
                Colores.error("No existe ningún proveedor con el ID " + id + ".");
                return;
            }
            System.out.println("Proveedor: " + actual.getId() + " - " + actual.getNombre());
            if (!confirmar("¿Confirmas la eliminación?")) {
                Colores.aviso("Operación cancelada. No se ha borrado nada.");
                return;
            }
            int filas = OperacionesBD.eliminarProveedor(id);
            if (filas == 1) {
                Colores.ok("Proveedor " + id + " eliminado correctamente.");
            } else {
                Colores.error("No se ha borrado ninguna fila (el proveedor ya no existe).");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                Colores.error("El proveedor tiene productos asociados. MySQL impide borrarlo (clave foránea).");
            } else {
                Colores.error(explicarError(e));
            }
        }
    }
}