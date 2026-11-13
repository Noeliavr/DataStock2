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
// Programa independiente: tiene su propio main (no está en el menú de DataStock).
//
// CHULETA: sigue los comentarios "PASO 1" a "PASO 9" en orden.
// Cada paso dice qué escribir, qué tiene que salir y qué explicar si te preguntan.
public class AccesoAleatorioActividad {

    // =====================================================================
    // TEORÍA (si te preguntan por el tamaño del registro)
    //
    //   byte:  0                       24          32      36
    //          |------- código --------|-- precio --|-stock-|
    //               24 bytes              8 bytes    4 bytes
    //
    //   código: 12 caracteres x 2 bytes = 24  (en Java cada char ocupa 2 bytes, UTF-16)
    //   precio: double                  =  8
    //   stock:  int                     =  4
    //   TOTAL por registro              = 36 bytes
    //
    //   El registro n empieza en el byte n x 36:
    //   posición 0 -> byte 0 | 1 -> 36 | 2 -> 72 | 3 -> 108
    // =====================================================================

    // 12 = longitud máxima del código en DataStock (y VARCHAR(12) en MySQL): ningún código se recorta.
    static final int LONGITUD_CODIGO = 12;

    // 36 = código (24) + precio (8) + stock (4). Se calcula solo a partir de LONGITUD_CODIGO.
    // "¿Y si cambio la longitud del código?" -> cambia el tamaño del registro:
    //   código de 10 -> 32 bytes | código de 12 -> 36 bytes | código de 15 -> 42 bytes
    //   y los stock.bin antiguos ya no sirven: hay que regenerarlos.
    static final int TAM_REGISTRO = LONGITUD_CODIGO * 2 + 8 + 4;

    // 32 = código (24) + precio (8). NO es un tamaño, es la POSICIÓN donde empieza el stock
    // dentro del registro. "¿Por qué 32 si el código usa 24?" -> porque antes del stock
    // también está el precio: 24 + 8 = 32.
    static final int POSICION_STOCK = LONGITUD_CODIGO * 2 + 8;

    static Scanner teclado = new Scanner(System.in);

    // Copia de los productos con la que se genera stock.bin
    static ArrayList<Producto> productos = new ArrayList<Producto>();

    public static void main(String[] args) {
        System.out.println("===== ACCESO ALEATORIO =====");

        // ---------------------------------------------------------------
        // PASO 1. Al arrancar
        //   Escribes: datos/salidas/stock.bin
        //   Es el fichero con el que vas a trabajar. Todavía no existe: lo crea el PASO 3.
        //   (En el PASO 9, en otra ejecución, aquí escribirás datos/pruebas/raf/stock_invalido.bin)
        // ---------------------------------------------------------------
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
                case 1: // PASO 2
                    cargarProductos();
                    break;
                case 2: // PASO 3
                    generarFichero(rutaBin);
                    break;
                case 3: // PASO 5 y PASO 8 (y PASO 9 en otra ejecución)
                    consultarRegistro(rutaBin);
                    break;
                case 4: // PASO 6
                    modificarStock(rutaBin);
                    break;
                case 5: // PASO 4 y PASO 7
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

    // Repite la pregunta hasta que se escriba un número entero
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

    // =====================================================================
    // PASO 2. Opción 1: cargar productos
    //   Escribes: 1  y luego  datos/pruebas/dat/inventario_cuatro.dat
    //   Sale: "Productos cargados: 4"
    //   Si te preguntan por qué: este programa es aparte y no ve el inventario de App,
    //   así que lee el .dat que se guardó en DataStock con SERIALIZACIÓN
    //   (ObjectInputStream + readObject reconstruye la lista de productos).
    // =====================================================================
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
            // Comprobamos que lo leído es una lista de Producto
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

    // =====================================================================
    // PASO 3. Opción 2: generar el fichero binario          (prueba CP-RAF-01)
    //   Escribes: 2   (si dice que ya existe, responde S)
    //   Sale: "Fichero generado: 4 registros | Tamaño: 144 bytes"   -> 4 x 36 = 144
    //   Si te preguntan cómo: RandomAccessFile en modo "rw" (lectura y escritura),
    //   setLength(0) lo vacía, y por cada producto escribe 36 bytes:
    //   writeChars (código, 24) + writeDouble (precio, 8) + writeInt (stock, 4).
    // =====================================================================
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
                escribirCodigo(raf, p.getCodigo());          // 24 bytes (12 chars x 2)
                raf.writeDouble(p.getPrecio().doubleValue()); // 8 bytes
                raf.writeInt(p.getStock());                   // 4 bytes
            }
            System.out.println("Fichero generado: " + ordenados.size() + " registros | Tamaño: "
                    + raf.length() + " bytes");
        } catch (IOException e) {
            System.out.println("Error al escribir el fichero: " + e.getMessage());
        }
    }

    // Escribe el código con exactamente 12 caracteres (relleno con espacios, o recortado).
    // "¿Por qué se rellena?" -> para que TODOS los registros midan lo mismo; si no,
    // no se podría calcular posición x 36 para saltar a uno.
    static void escribirCodigo(RandomAccessFile raf, String codigo) throws IOException {
        if (codigo.length() > LONGITUD_CODIGO) {
            codigo = codigo.substring(0, LONGITUD_CODIGO);
        }
        while (codigo.length() < LONGITUD_CODIGO) {
            codigo = codigo + " ";
        }
        raf.writeChars(codigo); // cada char ocupa 2 bytes -> 12 x 2 = 24 bytes
    }

    // Lee los 12 caracteres del código (readChar lee 2 bytes cada vez)
    // y quita los espacios de relleno con trim()
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

    // Comprueba el fichero y devuelve cuántos registros tiene (-1 si no vale).
    // La usan los PASOS 4 a 9 antes de leer.
    // Aquí es donde falla el PASO 9: 37 bytes no es múltiplo de 36.
    static long contarRegistros(RandomAccessFile raf) throws IOException {
        long tamano = raf.length();
        if (tamano % TAM_REGISTRO != 0) {
            System.out.println("Fichero inválido: su tamaño (" + tamano + " bytes) no es múltiplo de " + TAM_REGISTRO + ".");
            return -1;
        }
        long registros = tamano / TAM_REGISTRO; // ejemplo: 144 / 36 = 4 registros
        System.out.println("Registros: " + registros + " | Tamaño: " + tamano + " bytes");
        if (registros == 0) {
            System.out.println("El fichero no tiene registros.");
            return -1;
        }
        return registros;
    }

    // =====================================================================
    // PASO 5. Opción 3: consultar una posición  -> ACCESO ALEATORIO  (CP-RAF-02)
    //   Escribes: 3  y luego  2
    //   Sale: el tercer registro, "RAT-002 | 14.5 € | stock 25"
    //   Explica: seek(2 x 36) = seek(72) salta DIRECTAMENTE al byte 72,
    //   sin leer los dos primeros registros. Eso es el acceso aleatorio.
    //
    // PASO 8. Opción 3 otra vez, con posiciones que no existen   (CP-RAF-04)
    //   Escribes: 3 -> 4      y después     3 -> -1
    //   Sale: "Error: posición fuera de rango."  (solo existen de 0 a 3)
    //   Luego 0 para salir.
    //
    // PASO 9. Otra ejecución: fichero inválido                    (CP-RAF-05)
    //   Vuelve a ejecutar. En el PASO 1 escribe: datos/pruebas/raf/stock_invalido.bin
    //   Pulsa 3.
    //   Sale: "Fichero inválido: su tamaño (37 bytes) no es múltiplo de 36."
    //   Explica: si el tamaño no es múltiplo de 36, el fichero está dañado o no es
    //   de este formato, porque las posiciones no cuadrarían.
    // =====================================================================
    static void consultarRegistro(String rutaBin) {
        File f = new File(rutaBin);
        if (!f.isFile()) {
            System.out.println("Error: el fichero " + rutaBin + " no existe. Genéralo con la opción 2.");
            return;
        }
        // "r" = solo lectura
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
            raf.seek(pos * TAM_REGISTRO);     // salto directo al inicio del registro
            String codigo = leerCodigo(raf);  // 24 bytes
            double precio = raf.readDouble(); // 8 bytes
            int stock = raf.readInt();        // 4 bytes
            System.out.println(codigo + " | " + precio + " € | stock " + stock);
        } catch (IOException e) {
            System.out.println("Error de lectura: " + e.getMessage());
        }
    }

    // =====================================================================
    // PASO 6. Opción 4: modificar SOLO el stock                   (CP-RAF-03)
    //   Escribes: 4  ->  posición 2  ->  nuevo stock 15
    //   Sale: "Actualización correcta: stock 25 → 15"
    //   Explica: seek(2 x 36 + 32) = seek(104) salta directamente al campo stock
    //   (se salta el código, 24, y el precio, 8) y writeInt escribe solo esos 4 bytes.
    //   El código y el precio no se tocan.
    //   OJO: NO cambia el inventario de DataStock; stock.bin es una copia independiente.
    // =====================================================================
    static void modificarStock(String rutaBin) {
        File f = new File(rutaBin);
        if (!f.isFile()) {
            System.out.println("Error: el fichero " + rutaBin + " no existe. Genéralo con la opción 2.");
            return;
        }
        // "rw" = lectura y escritura
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

    // =====================================================================
    // PASO 4. Opción 5: ver todos ANTES de tocar nada
    //   Escribes: 5
    //   Sale: [0] ALT-010 ... [1] MON-003 ... [2] RAT-002 ... stock 25 ... [3] TEC-001 ...
    //   Sirve para enseñar qué hay en cada posición.
    //
    // PASO 7. Opción 5 otra vez, DESPUÉS de modificar             (CP-RAF-03)
    //   Escribes: 5
    //   Sale: lo mismo, pero [2] RAT-002 ahora tiene stock 15. Nada más ha cambiado.
    //
    //   Explica: esto es ACCESO SECUENCIAL: lee desde el principio, un registro detrás
    //   de otro, SIN seek. Diferencia: secuencial = leer todo en orden (opción 5);
    //   aleatorio = saltar a una posición con seek (opciones 3 y 4).
    // =====================================================================
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
