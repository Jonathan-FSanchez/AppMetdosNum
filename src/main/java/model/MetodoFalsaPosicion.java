package model;

import com.example.prueba.ConvertidorLatex;
import javafx.application.Platform;
import javafx.scene.control.TableView;

import java.util.List;

public class MetodoFalsaPosicion {
    /**
     * Método de falsa posición que actualiza directamente la tabla de resultados
     */
    public void metodoFalsaPosicion(InfoMetodoFalsaPosicion inf, TableView<InfoMetodoFalsaPosicion> tblMFalsaPosicion, String function) {
        int i = 0;
        double c, erAct;
        double a = inf.getA();
        double b = inf.getB();

        // Lista para almacenar todas las iteraciones
        java.util.List<InfoMetodoFalsaPosicion> iteraciones = new java.util.ArrayList<>();

        System.out.println("a = " + a);
        System.out.println("b = " + b);
        System.out.println("f(a) = " + evaluateFunction(function, a));
        System.out.println("f(b) = " + evaluateFunction(function, b));

        if (evaluateFunction(function, a) * evaluateFunction(function, b) >= 0) {
            System.out.println("Imposible calcular: El intervalo no contiene una raíz.");
            return;
        }

        while (i < inf.getiMax()) {
            // Cálculo del punto c (falsa posición)
            c = b - (evaluateFunction(function, b) * (b - a)) / (evaluateFunction(function, b) - evaluateFunction(function, a));
            erAct = Math.abs(evaluateFunction(function, c));

            InfoMetodoFalsaPosicion iter = new InfoMetodoFalsaPosicion();
            iter.setA(a);
            iter.setB(b);
            iter.setC(c);
            iter.setErAct(erAct);
            iter.setiMax(i + 1);

            // Añadir a la lista de iteraciones
            iteraciones.add(iter);

            // Imprimir detalles de la iteración para depuración
            System.out.printf("Iteración %d: a = %.6f, b = %.6f, c = %.6f, f(c) = %.6f%n", 
                             i, a, b, c, evaluateFunction(function, c));

            if (erAct <= inf.getErMax()) {
                break;
            }

            if (evaluateFunction(function, a) * evaluateFunction(function, c) < 0) {
                b = c;
            } else {
                a = c;
            }
            i++;
        }

        // Añadir todas las iteraciones a la tabla de una vez
        Platform.runLater(() -> {
            tblMFalsaPosicion.getItems().addAll(iteraciones);
        });
    }

    /**
     * Método de falsa posición que almacena las iteraciones en una lista para animación
     * @param inf Objeto con los parámetros iniciales
     * @param iteraciones Lista donde se almacenarán las iteraciones
     * @param function Función a evaluar
     */
    public void metodoFalsaPosicionAnimado(InfoMetodoFalsaPosicion inf, List<InfoMetodoFalsaPosicion> iteraciones, String function) {
        int i = 0;
        double c, erAct;
        double a = inf.getA();
        double b = inf.getB();

        System.out.println("a = " + a);
        System.out.println("b = " + b);
        System.out.println("f(a) = " + evaluateFunction(function, a));
        System.out.println("f(b) = " + evaluateFunction(function, b));

        if (evaluateFunction(function, a) * evaluateFunction(function, b) >= 0) {
            System.out.println("Imposible calcular: El intervalo no contiene una raíz.");
            return;
        }

        while (i < inf.getiMax()) {
            // Cálculo del punto c (falsa posición)
            c = b - (evaluateFunction(function, b) * (b - a)) / (evaluateFunction(function, b) - evaluateFunction(function, a));
            erAct = Math.abs(evaluateFunction(function, c));

            InfoMetodoFalsaPosicion iter = new InfoMetodoFalsaPosicion();
            iter.setA(a);
            iter.setB(b);
            iter.setC(c);
            iter.setErAct(erAct);
            iter.setiMax(i + 1);

            // Añadir a la lista de iteraciones para animación
            iteraciones.add(iter);

            if (erAct <= inf.getErMax()) {
                break;
            }

            if (evaluateFunction(function, a) * evaluateFunction(function, c) < 0) {
                b = c;
            } else {
                a = c;
            }
            i++;
        }
    }

    private double evaluateFunction(String function, double x) {
        return ConvertidorLatex.evaluate(function, x);
    }

    // Función de ejemplo f(x), puedes modificarla según el problema
    public static double f(double x) {
        // Ejemplo: f(x) = x^3 - x - 2
        return Math.pow(x, 3) - x - 2;
    }
}
