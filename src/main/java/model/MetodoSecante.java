package model;

import com.example.prueba.ConvertidorLatex;
import javafx.application.Platform;
import javafx.scene.control.TableView;

public class MetodoSecante {

    /**
     * Método de la Secante que actualiza directamente la tabla de resultados
     */
    public void metodoSecante(double x0, double x1, double error, int maxIteraciones, 
                            TableView<InfoMetodoSecante> tabla, String function) {
        int iteracion = 0;
        double x2, ea; // Aproximación de la raíz y el error aproximado

        do {
            // Verificar división por cero
            if (evaluateFunction(function, x1) - evaluateFunction(function, x0) == 0) {
                System.out.println("División por cero detectada.");
                return; // Evita división por cero
            }

            // Fórmula del método de la secante
            x2 = x1 - (evaluateFunction(function, x1) * (x1 - x0)) / (evaluateFunction(function, x1) - evaluateFunction(function, x0));

            // Error relativo aproximado
            ea = Math.abs((x2 - x1) / x2);

            // Crear objeto para la tabla
            InfoMetodoSecante info = new InfoMetodoSecante();
            info.setIteracion(iteracion + 1);
            info.setX0(x0);
            info.setX1(x1);
            info.setX2(x2);
            info.setError(ea);
            
            // Añadir a la tabla
            final InfoMetodoSecante infoFinal = info;
            Platform.runLater(() -> {
                tabla.getItems().add(infoFinal);
            });

            // Actualizar los valores para la siguiente iteración
            x0 = x1;
            x1 = x2;
            
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