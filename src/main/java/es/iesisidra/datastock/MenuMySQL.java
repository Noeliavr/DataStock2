package es.iesisidra.datastock;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;

import es.iesisidra.datastock.bd.OperacionesBD;
import es.iesisidra.datastock.modelo.Categoria;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.modelo.ProductoDetalle;
import es.iesisidra.datastock.modelo.Proveedor;
import es.iesisidra.datastock.util.Colores;


public class MenuMySQL {
        // Precio máximo que cabe en DECIMAL(10,2)
    static final BigDecimal PRECIO_MAXIMO = new BigDecimal("99999999.99");

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
                case 4:
                    menuProductos();
                    break;
                case 5:
                    menuConsultas();
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


    static String validarCodigo(String codigo) {
        if (codigo.isEmpty()) {
            return "El código es obligatorio.";
        }
        if (codigo.length() > 12) {
            return "El código no puede tener más de 12 caracteres.";
        }
        if (!codigo.matches("[A-Z0-9-]+")) {
            return "El código solo admite letras A-Z, números y guiones.";
        }
        return null;
    }

    static String validarNombreProducto(String nombre) {
        if (nombre.length() < 3 || nombre.length() > 100) {
            return "El nombre debe tener entre 3 y 100 caracteres.";
        }
        return null;
    }

    static String validarPrecio(BigDecimal precio) {
        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            return "El precio debe ser mayor que 0.";
        }
        // stripTrailingZeros quita ceros del final: 12.50 -> 12.5 ; scale() = nº de decimales
        if (precio.stripTrailingZeros().scale() > 2) {
            return "El precio no puede tener más de 2 decimales.";
        }
        if (precio.compareTo(PRECIO_MAXIMO) > 0) {
            return "El precio es demasiado grande (máximo 99999999.99).";
        }
        return null;
    }

    // Convierte el texto a BigDecimal; acepta coma o punto. Devuelve null si no es un número.
    static BigDecimal convertirPrecio(String texto) {
        try {
            return new BigDecimal(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Convierte el texto a entero. Devuelve null si no es un número.
    static Integer convertirEntero(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Muestra todos los datos de un producto leído de MySQL
    static void mostrarFicha(ProductoDetalle p) {
        System.out.println("Código: " + p.getCodigo());
        System.out.println("Nombre: " + p.getNombre());
        System.out.println("Precio: " + p.getPrecio() + " €");
        System.out.println("Stock: " + p.getStock());
        System.out.println("Categoría: " + p.getCategoriaId() + " - " + p.getNombreCategoria());
        System.out.println("Proveedor: " + p.getProveedorId() + " - " + p.getNombreProveedor());
        System.out.println("------------------------------------------------------------");
    }

    static void menuProductos() {
        int opcion;
        do {
            System.out.println();
            System.out.println("------------- MENÚ DE PRODUCTOS -------------");
            System.out.println("1. Dar de alta un producto");
            System.out.println("2. Consultar producto por código");
            System.out.println("3. Modificar un producto");
            System.out.println("4. Eliminar un producto");
            System.out.println("0. Volver al menú MySQL");
            opcion = App.leerNumero("Selecciona una opción: ");

            switch (opcion) {
                case 1:
                    altaProducto();
                    break;
                case 2:
                    consultarProducto();
                    break;
                case 3:
                    modificarProducto();
                    break;
                case 4:
                    eliminarProducto();
                    break;
                case 0:
                    break;
                default:
                    Colores.error("Opción no válida.");
                    break;
            }
        } while (opcion != 0);
    }

    static void altaProducto() {
        System.out.println("===== ALTA DE PRODUCTO =====");

        // 1) Pedir y validar los datos en Java
        String codigo = App.leerTexto("Código (máx. 12: A-Z, 0-9 y -): ").trim().toUpperCase();
        String error = validarCodigo(codigo);
        if (error != null) {
            Colores.error(error);
            return;
        }
        String nombre = App.leerTexto("Nombre (3 a 100 caracteres): ").trim();
        error = validarNombreProducto(nombre);
        if (error != null) {
            Colores.error(error);
            return;
        }
        BigDecimal precio = convertirPrecio(App.leerTexto("Precio (ej. 49.90): "));
        if (precio == null) {
            Colores.error("El precio debe ser un número.");
            return;
        }
        error = validarPrecio(precio);
        if (error != null) {
            Colores.error(error);
            return;
        }
        int stock = App.leerNumero("Stock inicial: ");
        if (stock < 0) {
            Colores.error("El stock no puede ser negativo.");
            return;
        }
        int categoriaId = App.leerNumero("ID de categoría: ");
        int proveedorId = App.leerNumero("ID de proveedor: ");
        if (categoriaId <= 0 || proveedorId <= 0) {
            Colores.error("Los ID de categoría y proveedor deben ser enteros positivos.");
            return;
        }

        // 2) Comprobar en MySQL: código repetido y que existan categoría y proveedor
        try {
            if (OperacionesBD.buscarProducto(codigo) != null) {
                Colores.error("El producto " + codigo + " ya existe en MySQL. No se sobrescribe.");
                return;
            }
            if (OperacionesBD.buscarCategoria(categoriaId) == null) {
                Colores.error("No existe la categoría con ID " + categoriaId + ". Dala de alta primero.");
                return;
            }
            if (OperacionesBD.buscarProveedor(proveedorId) == null) {
                Colores.error("No existe el proveedor con ID " + proveedorId + ". Dalo de alta primero.");
                return;
            }

            // 3) Insertar
            OperacionesBD.insertarProducto(new Producto(codigo, nombre, precio, stock, categoriaId, proveedorId));
            Colores.ok("Producto " + codigo + " guardado correctamente.");
        } catch (SQLException e) {
            Colores.error(explicarError(e));
        }
    }

    static void consultarProducto() {
        System.out.println("===== CONSULTAR PRODUCTO =====");
        String codigo = App.leerTexto("Código: ").trim().toUpperCase();
        if (codigo.isEmpty()) {
            Colores.error("El código no puede estar vacío.");
            return;
        }
        try {
            ProductoDetalle p = OperacionesBD.buscarProducto(codigo);
            if (p == null) {
                Colores.aviso("No existe ningún producto con el código " + codigo + ".");
            } else {
                mostrarFicha(p);
            }
        } catch (SQLException e) {
            Colores.error(explicarError(e));
        }
    }

    static void modificarProducto() {
        System.out.println("===== MODIFICAR PRODUCTO =====");
        String codigo = App.leerTexto("Código: ").trim().toUpperCase();
        try {
            ProductoDetalle actual = OperacionesBD.buscarProducto(codigo);
            if (actual == null) {
                Colores.error("No existe ningún producto con el código " + codigo + ".");
                return;
            }
            mostrarFicha(actual);
            System.out.println("(Pulsa Intro para conservar cada valor. El código no se puede cambiar.)");
            System.out.println("(El stock solo se cambia desde 'Registrar entrada o salida de stock'.)");

            // Nombre
            String nombre = App.leerTexto("Nuevo nombre: ").trim();
            if (nombre.isEmpty()) {
                nombre = actual.getNombre();
            } else {
                String error = validarNombreProducto(nombre);
                if (error != null) {
                    Colores.error(error);
                    return;
                }
            }

            // Precio
            BigDecimal precio = actual.getPrecio();
            String texto = App.leerTexto("Nuevo precio: ").trim();
            if (!texto.isEmpty()) {
                precio = convertirPrecio(texto);
                if (precio == null) {
                    Colores.error("El precio debe ser un número.");
                    return;
                }
                String error = validarPrecio(precio);
                if (error != null) {
                    Colores.error(error);
                    return;
                }
            }

            // Categoría
            int categoriaId = actual.getCategoriaId();
            texto = App.leerTexto("Nuevo ID de categoría: ").trim();
            if (!texto.isEmpty()) {
                Integer numero = convertirEntero(texto);
                if (numero == null || numero <= 0) {
                    Colores.error("El ID de categoría debe ser un entero positivo.");
                    return;
                }
                if (OperacionesBD.buscarCategoria(numero) == null) {
                    Colores.error("No existe la categoría con ID " + numero + ".");
                    return;
                }
                categoriaId = numero;
            }

            // Proveedor
            int proveedorId = actual.getProveedorId();
            texto = App.leerTexto("Nuevo ID de proveedor: ").trim();
            if (!texto.isEmpty()) {
                Integer numero = convertirEntero(texto);
                if (numero == null || numero <= 0) {
                    Colores.error("El ID de proveedor debe ser un entero positivo.");
                    return;
                }
                if (OperacionesBD.buscarProveedor(numero) == null) {
                    Colores.error("No existe el proveedor con ID " + numero + ".");
                    return;
                }
                proveedorId = numero;
            }

            if (!confirmar("¿Guardar los cambios del producto " + codigo + "?")) {
                Colores.aviso("Operación cancelada. No se ha modificado nada.");
                return;
            }
            // El stock se pasa tal cual, pero el UPDATE no lo usa
            Producto nuevo = new Producto(codigo, nombre, precio, actual.getStock(), categoriaId, proveedorId);
            int filas = OperacionesBD.modificarProducto(nuevo);
            if (filas == 1) {
                Colores.ok("Producto " + codigo + " modificado correctamente.");
            } else {
                Colores.error("No se ha modificado ninguna fila (el producto ya no existe).");
            }
        } catch (SQLException e) {
            Colores.error(explicarError(e));
        }
    }

    static void eliminarProducto() {
        System.out.println("===== ELIMINAR PRODUCTO =====");
        String codigo = App.leerTexto("Código: ").trim().toUpperCase();
        try {
            ProductoDetalle actual = OperacionesBD.buscarProducto(codigo);
            if (actual == null) {
                Colores.error("No existe ningún producto con el código " + codigo + ".");
                return;
            }
            mostrarFicha(actual);
            if (!confirmar("¿Confirmas la eliminación?")) {
                Colores.aviso("Operación cancelada. El producto sigue en MySQL.");
                return;
            }
            int filas = OperacionesBD.eliminarProducto(codigo);
            if (filas == 1) {
                Colores.ok("Producto " + codigo + " eliminado correctamente.");
            } else {
                Colores.error("No se ha borrado ninguna fila (el producto ya no existe).");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                Colores.error("El producto tiene movimientos de stock. No se borra para conservar el historial.");
            } else {
                Colores.error(explicarError(e));
            }
        }
    }
    static void menuConsultas() {
        int opcion;
        do {
            System.out.println();
            System.out.println("------------- MENÚ DE CONSULTAS -------------");
            System.out.println("1. Listar todos los productos");
            System.out.println("2. Buscar producto por código");
            System.out.println("3. Buscar productos por nombre");
            System.out.println("4. Consultar productos con stock inferior a un umbral");
            System.out.println("0. Volver al menú MySQL");
            opcion = App.leerNumero("Selecciona una opción: ");

            switch (opcion) {
                case 1:
                    listarProductos();
                    break;
                case 2:
                    consultarProducto();
                    break;
                case 3:
                    buscarPorNombre();
                    break;
                case 4:
                    stockInferior();
                    break;
                case 0:
                    break;
                default:
                    Colores.error("Opción no válida.");
                    break;
            }
        } while (opcion != 0);
    }

    // Muestra una lista de productos o un aviso si está vacía
    static void mostrarLista(ArrayList<ProductoDetalle> lista) {
        if (lista.isEmpty()) {
            Colores.aviso("No se han encontrado productos.");
            return;
        }
        System.out.println("------------------------------------------------------------");
        for (ProductoDetalle p : lista) {
            mostrarFicha(p);
        }
        System.out.println("Total de productos encontrados: " + lista.size());
    }

    static void listarProductos() {
        System.out.println("===== PRODUCTOS ALMACENADOS EN MYSQL =====");
        try {
            mostrarLista(OperacionesBD.listarProductos());
        } catch (SQLException e) {
            Colores.error(explicarError(e));
        }
    }

    static void buscarPorNombre() {
        System.out.println("===== BUSCAR POR NOMBRE =====");
        String texto = App.leerTexto("Texto a buscar: ").trim();
        if (texto.isEmpty()) {
            Colores.error("El texto de búsqueda no puede estar vacío.");
            return;
        }
        try {
            mostrarLista(OperacionesBD.buscarPorNombre(texto));
        } catch (SQLException e) {
            Colores.error(explicarError(e));
        }
    }

    static void stockInferior() {
        System.out.println("===== STOCK INFERIOR A UN UMBRAL =====");
        int umbral = App.leerNumero("Umbral (se muestran los productos con stock menor): ");
        if (umbral < 0) {
            Colores.error("El umbral debe ser mayor o igual que 0.");
            return;
        }
        try {
            mostrarLista(OperacionesBD.stockInferior(umbral));
        } catch (SQLException e) {
            Colores.error(explicarError(e));
        }
    }
}