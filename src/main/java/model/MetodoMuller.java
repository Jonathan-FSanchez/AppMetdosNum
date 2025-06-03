package model;

import com.example.prueba.ConvertidorLatex;
import javafx.application.Platform;
import javafx.scene.control.TableView;

public class MetodoMuller {

    /**
     * Método de Muller que actualiza directamente la tabla de resultados
     */
    public void metodoMuller(double x0, double x1, double x2, double error, int maxIteraciones, 
                            TableView<InfoMetodoMuller> tabla, String function) {
        int iteracion = 0;
        double h0, h1, delta0, delta1, a, b, c, discriminante, denominador, x3, ea;

        do {
            h0 = x1 - x0;
            h1 = x2 - x1;
            delta0 = (evaluateFunction(function, x1) - evaluateFunction(function, x0)) / h0;
            delta1 = (evaluateFunction(function, x2) - evaluateFunction(function, x1)) / h1;
            
            // Calculamos los coeficientes del polinomio de interpolación cuadrática
            a = (delta1 - delta0) / (h1 + h0);
            b = a * h1 + delta1;
            c = evaluateFunction(function, x2);
            
            discriminante = Math.sqrt(b * b - 4 * a * c);
            
            // Elegimos el denominador más grande en valor absoluto
            if (Math.abs(b + discriminante) > Math.abs(b - discriminante)) {
                denominador = b + discriminante;
            } else {
                denominador = b - discriminante;
            }
            
            // Nueva aproximación
            x3 = x2 - (2 * c) / denominador;

            // Calculamos el error aproximado
            ea = Math.abs((x3 - x2) / x3);
            
            // Crear objeto para la tabla
            InfoMetodoMuller info = new InfoMetodoMuller();
            info.setIteracion(iteracion + 1);
            info.setX0(x0);
            info.setX1(x1);
            info.setX2(x2);
            info.setX3(x3);
            info.setError(ea);
            
            // Añadir a la tabla
            final InfoMetodoMuller infoFinal = info;
            Platform.runLater(() -> {
                tabla.getItems().add(infoFinal);
            });
            
            // Actualizamos los valores para la siguiente iteración
            x0 = x1;
            x1 = x2;
            x2 = x3;
            
            iteracion++;

        } while (ea > error && iteracion < maxIteraciones);
    }
    
    /**
     * Evalúa la función en un punto dado
     */
    private double evaluateFunction(String function, double x) {
        return ConvertidorLatex.evaluate(function, x);
    }
    
    /**
     * Función de ejemplo para pruebas
     */
    public static double funcion(double x) {
        return Math.pow(x, 3) - 13 * x - 12; // Ejemplo: f(x) = x^3 - 13x - 12
    }
}