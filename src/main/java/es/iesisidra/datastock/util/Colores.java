package es.iesisidra.datastock.util;

public class Colores {
    
    public static final String RESET = "\u001B[0m";
    public static final String ROJO = "\u001B[31m";
    public static final String VERDE = "\u001B[31m";
    public static final String AMARILLO = "\u001B[33m";

    public static void ok(String mensaje){
        System.out.println(VERDE + "[OK]" + mensaje + RESET);
    }

    public static void error(String mensaje){
        System.out.println(ROJO + "[ERROR]" + mensaje + RESET);
    }

    public static void aviso(String mensaje){
        System.out.println(AMARILLO + "[Aviso]" + mensaje + RESET);
    }
}
