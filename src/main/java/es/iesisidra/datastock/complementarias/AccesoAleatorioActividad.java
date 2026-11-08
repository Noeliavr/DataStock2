package es.iesisidra.datastock.complementarias;

import es.iesisidra.datastock.modelo.Producto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;

// RF-FIC-08. Acceso aleatorio a un fichero binario de registros de longitud fija.
// Programa independiente: tiene su propio main.
//
// Cada registro ocupa 36 bytes:
//   código: 12 caracteres x 2 bytes = 24 bytes  (bytes 0 a 23)
//   precio: double                  =  8 bytes  (bytes 24 a 31)
//   stock:  int                     =  4 bytes  (bytes 32 a 35)
public class AccesoAleatorioActividad {

    static final int LONGITUD_CODIGO = 12;
    static final int TAM_REGISTRO = 36;
    static final int POSICION_STOCK = 32; // el stock empieza en el byte 32 del registro

    static Scanner teclado = new Scanner(System.in);

    // Copia de los productos con la que se genera stock.bin
    static ArrayList<Producto> productos = new ArrayList<Producto>();

    public static void main(String[] args) {
        System.out.println("===== ACCESO ALEATORIO =====");
        System.out.print("Fichero binario (por ejemplo datos/salidas/stock.bin): ");
        String rutaBin = teclado.nextLine().trim();

        int opcion;
        do {
            System.out.println();
            System.out.println("Productos cargados en memoria: " + productos.size());
            System.out.println("1. Cargar productos desde un inventario .dat (opción 6.1 de DataStock)");
            System.out.println("2. Generar " + rutaBin + " con esos productos");
            System.out.println("3. Consultar un registro por posición");
            System.out.println("4. Modificar el stock de un registro");
            System.out.println("5. Mostrar todos los registros (lectura secuencial)");
            System.out.println("0. Salir");
            opcion = leerNumero("Opción: ");

            switch (opcion) {
                case 1:
                    cargarProductos();
                    break;
                case 2:
                    generarFichero(rutaBin);
                    break;
                case 3:
                    consultarRegistro(rutaBin);
                    break;
                case 4:
                    modificarStock(rutaBin);
                    break;
                case 5:
                    mostrarTodos(rutaBin);
                    break;
                case 0:
                    System.out.println("Fin de la actividad.");
                    break;
                default:
                    System.out.println("Opción no válida.");
                    break;
            }
        } while (opcion != 0);
    }

    static int leerNumero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Integer.parseInt(teclado.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Debes escribir un número entero.");
            }
        }
    }

    // ---------- 1. Cargar una copia de los productos ----------

    static void cargarProductos() {
        System.out.print("Ruta del inventario .dat: ");
        File fichero = new File(teclado.nextLine().trim());
        if (!fichero.isFile()) {
            System.out.println("Error: el fichero no existe.");
            return;
        }
        ArrayList<Producto> leidos = new ArrayList<Producto>();
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(fichero))) {
            Object objeto = entrada.readObject();
            if (!(objeto instanceof ArrayList)) {
                System.out.println("Error: el fichero no contiene un inventario de DataStock.");
                return;
            }
            for (Object o : (ArrayList<?>) objeto) {
                if (!(o instanceof Producto)) {
                    System.out.println("Error: el fichero contiene objetos que no son productos.");
                    return;
                }
                leidos.add((Producto) o);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Error: el fichero no es compatible con DataStock.");
            return;
        } catch (IOException e) {
            System.out.println("Error: el fichero está dañado o no se puede leer.");
            return;
        }
        productos = leidos;
        System.out.println("Productos cargados: " + productos.size());
    }

    // ---------- 2. Generar el fichero binario ----------

    static void generarFichero(String rutaBin) {
        if (productos.isEmpty()) {
            System.out.println("No hay productos en memoria. Usa primero la opción 1. No se genera el fichero.");
            return;
        }
        File f = new File(rutaBin);
        File carpeta = f.getAbsoluteFile().getParentFile();
        if (carpeta == null || !carpeta.isDirectory()) {
            System.out.println("Error: el directorio de destino no existe.");
            return;
        }
        if (f.exists()) {
            System.out.print("El fichero ya existe. ¿Sobrescribirlo? (S/N): ");
            if (!teclado.nextLine().trim().equalsIgnoreCase("S")) {
                System.out.println("Operación cancelada.");
                return;
            }
        }

        ArrayList<Producto> ordenados = ordenarPorCodigo(productos);
        try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
            raf.setLength(0); // truncar: borra lo que hubiera antes
            for (Producto p : ordenados) {
                escribirCodigo(raf, p.getCodigo());          // 24 bytes
                raf.writeDouble(p.getPrecio().doubleValue()); // 8 bytes
                raf.writeInt(p.getStock());                   // 4 bytes
            }
            System.out.println("Fichero generado: " + ordenados.size() + " registros | Tamaño: "
                    + raf.length() + " bytes");
        } catch (IOException e) {
            System.out.println("Error al escribir el fichero: " + e.getMessage());
        }
    }

    // Escribe el código con exactamente 12 caracteres (relleno con espacios)
    static void escribirCodigo(RandomAccessFile raf, String codigo) throws IOException {
        if (codigo.length() > LONGITUD_CODIGO) {
            codigo = codigo.substring(0, LONGITUD_CODIGO);
        }
        while (codigo.length() < LONGITUD_CODIGO) {
            codigo = codigo + " ";
        }
        raf.writeChars(codigo); // cada char ocupa 2 bytes
    }

    // Lee los 12 caracteres del código y quita los espacios del final
    static String leerCodigo(RandomAccessFile raf) throws IOException {
        String codigo = "";
        for (int i = 0; i < LONGITUD_CODIGO; i++) {
            codigo = codigo + raf.readChar();
        }
        return codigo.trim();
    }

    // Burbuja: copia ordenada por código
    static ArrayList<Producto> ordenarPorCodigo(ArrayList<Producto> lista) {
        ArrayList<Producto> copia = new ArrayList<Producto>(lista);
        for (int i = 0; i < copia.size() - 1; i++) {
            for (int j = 0; j < copia.size() - 1 - i; j++) {
                if (copia.get(j).getCodigo().compareTo(copia.get(j + 1).getCodigo()) > 0) {
                    Producto aux = copia.get(j);
                    copia.set(j, copia.get(j + 1));
                    copia.set(j + 1, aux);
                }
            }
        }
        return copia;
    }

    // Comprueba el fichero y devuelve cuántos registros tiene (-1 si no vale)
    static long contarRegistros(RandomAccessFile raf) throws IOException {
        long tamano = raf.length();
        if (tamano % TAM_REGISTRO != 0) {
            System.out.println("Fichero inválido: su tamaño (" + tamano + " bytes) no es múltiplo de " + TAM_REGISTRO + ".");
            return -1;
        }
        long registros = tamano / TAM_REGISTRO;
        System.out.println("Registros: " + registros + " | Tamaño: " + tamano + " bytes");
        if (registros == 0) {
            System.out.println("El fichero no tiene registros.");
            return -1;
        }
        return registros;
    }

    // ---------- 3. Consultar un registro con seek ----------

    static void consultarRegistro(String rutaBin) {
        File f = new File(rutaBin);
        if (!f.isFile()) {
            System.out.println("Error: el fichero " + rutaBin + " no existe. Genéralo con la opción 2.");
            return;
        }
        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            long registros = contarRegistros(raf);
            if (registros == -1) {
                return;
            }
            int pos = leerNumero("Consultar posición (0-" + (registros - 1) + "): ");
            if (pos < 0 || pos >= registros) {
                System.out.println("Error: posición fuera de rango.");
                return;
            }
            raf.seek(pos * TAM_REGISTRO); // salto directo al inicio del registro
            String codigo = leerCodigo(raf);
            double precio = raf.readDouble();
            int stock = raf.readInt();
            System.out.println(codigo + " | " + precio + " € | stock " + stock);
        } catch (IOException e) {
            System.out.println("Error de lectura: " + e.getMessage());
        }
    }

    // ---------- 4. Modificar solo el stock con seek ----------

    static void modificarStock(String rutaBin) {
        File f = new File(rutaBin);
        if (!f.isFile()) {
            System.out.println("Error: el fichero " + rutaBin + " no existe. Genéralo con la opción 2.");
            return;
        }
        try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
            long registros = contarRegistros(raf);
            if (registros == -1) {
                return;
            }
            int pos = leerNumero("Posición a modificar (0-" + (registros - 1) + "): ");
            if (pos < 0 || pos >= registros) {
                System.out.println("Error: posición fuera de rango.");
                return;
            }

            // Leemos el registro para enseñar el stock actual
            raf.seek(pos * TAM_REGISTRO);
            String codigo = leerCodigo(raf);
            double precio = raf.readDouble();
            int stockAntes = raf.readInt();
            System.out.println(codigo + " | " + precio + " € | stock " + stockAntes);

            int nuevoStock = leerNumero("Nuevo stock: ");
            if (nuevoStock < 0) {
                System.out.println("Error: el stock no puede ser negativo.");
                return;
            }

            // Salto directo al campo stock (byte 32 del registro) y escribimos solo 4 bytes
            raf.seek(pos * TAM_REGISTRO + POSICION_STOCK);
            raf.writeInt(nuevoStock);
            System.out.println("Actualización correcta: stock " + stockAntes + " → " + nuevoStock);
            System.out.println("(El inventario de DataStock NO cambia: stock.bin es una copia independiente.)");
        } catch (IOException e) {
            System.out.println("Error de lectura/escritura: " + e.getMessage());
        }
    }

    // ---------- 5. Mostrar todos (lectura secuencial) ----------

    static void mostrarTodos(String rutaBin) {
        File f = new File(rutaBin);
        if (!f.isFile()) {
            System.out.println("Error: el fichero " + rutaBin + " no existe. Genéralo con la opción 2.");
            return;
        }
        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            long registros = contarRegistros(raf);
            if (registros == -1) {
                return;
            }
            // Sin seek: se lee un registro detrás de otro desde el principio
            for (int i = 0; i < registros; i++) {
                String codigo = leerCodigo(raf);
                double precio = raf.readDouble();
                int stock = raf.readInt();
                System.out.println("[" + i + "] " + codigo + " | " + precio + " € | stock " + stock);
            }
        } catch (IOException e) {
            System.out.println("Error de lectura: " + e.getMessage());
        }
    }
}
