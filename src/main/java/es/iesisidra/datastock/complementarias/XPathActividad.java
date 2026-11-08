package es.iesisidra.datastock.complementarias;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.File;
import java.math.BigDecimal;
import java.util.Scanner;

// RF-FIC-07. Consultas XPath sobre un XML (sin importarlo al inventario).
// Programa independiente: tiene su propio main.
public class XPathActividad {

    static Scanner teclado = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("===== CONSULTAS XPATH =====");
        System.out.print("Fichero: ");
        String ruta = teclado.nextLine().trim();

        File fichero = new File(ruta);
        if (!fichero.isFile()) {
            System.out.println("Error: el fichero no existe o no es un fichero.");
            return;
        }

        // 1) Abrir el XML con DOM de forma segura (sin DTD)
        Document doc;
        try {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder constructor = fabrica.newDocumentBuilder();
            doc = constructor.parse(fichero);
        } catch (Exception e) {
            System.out.println("Error: XML mal formado o con DTD (no permitida).");
            System.out.println("Detalle: " + e.getMessage());
            return;
        }

        if (!doc.getDocumentElement().getNodeName().equals("catalogo")) {
            System.out.println("Error: la raíz del XML debe ser <catalogo>.");
            return;
        }

        // 2) Objeto que ejecuta las expresiones XPath
        XPath xpath = XPathFactory.newInstance().newXPath();

        // 3) Menú: se pueden hacer varias consultas sobre el mismo documento
        int opcion;
        do {
            System.out.println();
            System.out.println("1. Por código | 2. Por categoría | 3. Precio superior | 4. Contar | 0. Salir");
            opcion = leerNumero("Opción: ");
            switch (opcion) {
                case 1:
                    buscarPorCodigo(doc, xpath);
                    break;
                case 2:
                    buscarPorCategoria(doc, xpath);
                    break;
                case 3:
                    buscarPorPrecio(doc, xpath);
                    break;
                case 4:
                    contarProductos(doc, xpath);
                    break;
                case 0:
                    System.out.println("Fin de las consultas.");
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

    // Texto de una etiqueta hija, o "?" si falta
    static String leerCampo(Element producto, String etiqueta) {
        NodeList nodos = producto.getElementsByTagName(etiqueta);
        if (nodos.getLength() == 0) {
            return "?";
        }
        return nodos.item(0).getTextContent().trim();
    }

    // Muestra los productos de una NodeList
    static void mostrarResultado(NodeList productos) {
        for (int i = 0; i < productos.getLength(); i++) {
            Element p = (Element) productos.item(i);
            System.out.println(leerCampo(p, "codigo") + "  " + leerCampo(p, "nombre") + "  "
                    + leerCampo(p, "precio") + " €  (categoría " + leerCampo(p, "categoriaId") + ")");
        }
        if (productos.getLength() == 0) {
            System.out.println("No hay coincidencias.");
        } else {
            System.out.println("Productos encontrados: " + productos.getLength());
        }
    }

    // 1. Por código: XPath trae todos los productos y el código se compara en Java
    static void buscarPorCodigo(Document doc, XPath xpath) {
        System.out.print("Código: ");
        String codigo = teclado.nextLine().trim();
        if (codigo.isEmpty()) {
            System.out.println("Error: el código no puede estar vacío.");
            return;
        }
        try {
            NodeList todos = (NodeList) xpath.evaluate("/catalogo/producto", doc, XPathConstants.NODESET);
            int encontrados = 0;
            for (int i = 0; i < todos.getLength(); i++) {
                Element p = (Element) todos.item(i);
                if (leerCampo(p, "codigo").equalsIgnoreCase(codigo)) {
                    System.out.println(leerCampo(p, "codigo") + "  " + leerCampo(p, "nombre") + "  "
                            + leerCampo(p, "precio") + " €  stock " + leerCampo(p, "stock"));
                    encontrados++;
                }
            }
            if (encontrados == 0) {
                System.out.println("No existe ningún producto con el código " + codigo);
            }
        } catch (XPathExpressionException e) {
            System.out.println("Error en la consulta XPath: " + e.getMessage());
        }
    }

    // 2. Por categoría: /catalogo/producto[categoriaId=N]
    static void buscarPorCategoria(Document doc, XPath xpath) {
        int categoria = leerNumero("Categoría: ");
        if (categoria <= 0) {
            System.out.println("Error: la categoría debe ser un número positivo.");
            return;
        }
        // Es seguro concatenar porque categoria ya es un int
        String expresion = "/catalogo/producto[categoriaId=" + categoria + "]";
        try {
            NodeList resultado = (NodeList) xpath.evaluate(expresion, doc, XPathConstants.NODESET);
            mostrarResultado(resultado);
        } catch (XPathExpressionException e) {
            System.out.println("Error en la consulta XPath: " + e.getMessage());
        }
    }

    // 3. Precio superior: /catalogo/producto[precio > X]  (X exacto no entra)
    static void buscarPorPrecio(Document doc, XPath xpath) {
        System.out.print("Precio mínimo: ");
        String texto = teclado.nextLine().trim();
        BigDecimal minimo;
        try {
            minimo = new BigDecimal(texto);
        } catch (NumberFormatException e) {
            System.out.println("Error: el precio no es un número válido.");
            return;
        }
        if (minimo.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Error: el precio no puede ser negativo.");
            return;
        }
        // Es seguro concatenar porque ya hemos comprobado que es un número
        String expresion = "/catalogo/producto[precio > " + minimo.toPlainString() + "]";
        try {
            NodeList resultado = (NodeList) xpath.evaluate(expresion, doc, XPathConstants.NODESET);
            mostrarResultado(resultado);
        } catch (XPathExpressionException e) {
            System.out.println("Error en la consulta XPath: " + e.getMessage());
        }
    }

    // 4. Contar: count(/catalogo/producto) devuelve un número
    static void contarProductos(Document doc, XPath xpath) {
        try {
            Double total = (Double) xpath.evaluate("count(/catalogo/producto)", doc, XPathConstants.NUMBER);
            System.out.println("Número de productos en el catálogo: " + total.intValue());
        } catch (XPathExpressionException e) {
            System.out.println("Error en la consulta XPath: " + e.getMessage());
        }
    }
}
