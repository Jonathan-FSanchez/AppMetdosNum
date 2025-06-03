package model;

import com.example.prueba.ConvertidorLatex;
import javafx.application.Platform;
import javafx.scene.control.TableView;

public class MetodoNewtonRaphson {

    /**
     * Método de Newton-Raphson que actualiza directamente la tabla de resultados
     */
    public void metodoNewtonRaphson(double x0, double error, int maxIteraciones, 
                            TableView<InfoMetodoNewtonRaphson> tabla, String function) {
        int iteracion = 0;
        double x1, fx, dfx, ea; // Nueva aproximación y error aproximado

        do {
            // Calcular el valor de la función y su derivada en x0
            fx = evaluateFunction(function, x0);
            dfx = evaluateDerivative(function, x0);
            
            // Verificar si la derivada es cero para evitar división por cero
            if (dfx == 0) {
                System.out.println("La derivada es cero. No se puede continuar.");
                return;
            }

            // Fórmula del método de Newton-Raphson
            x1 = x0 - fx / dfx;

            // Error relativo aproximado
            ea = Math.abs((x1 - x0) / x1);
            
            // Crear objeto para la tabla
            InfoMetodoNewtonRaphson info = new InfoMetodoNewtonRaphson();
            info.setIteracion(iteracion + 1);
            info.setX(x0);
            info.setFx(fx);
            info.setDfx(dfx);
            info.setNextX(x1);
            info.setError(ea);
            
            // Añadir a la tabla
            final InfoMetodoNewtonRaphson infoFinal = info;
            Platform.runLater(() -> {
                tabla.getItems().add(infoFinal);
            });
            
            // Actualizar el valor para la siguiente iteración
            x0 = x1;
            
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
     * Calcula la derivada de la función en un punto dado usando diferencias finitas
     */
    private double evaluateDerivative(String function, double x) {
        double h = 0.0001; // Paso pequeño para la aproximación
        return (evaluateFunction(function, x + h) - evaluateFunction(function, x)) / h;
    }
    
    /**
     * Función de ejemplo para pruebas
     */
    public static double funcion(double x) {
        return Math.pow(x, 3) - 13 * x - 12; // Ejemplo: f(x) = x^3 - 13x - 12
    }
    
    /**
     * Derivada de la función de ejemplo
     */
    public static double derivadaFuncion(double x) {
        return 3 * Math.pow(x, 2) - 13; // Derivada: f'(x) = 3x^2 - 13
    }
}