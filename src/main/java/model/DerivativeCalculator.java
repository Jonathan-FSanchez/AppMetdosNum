package model;

import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;

/**
 * Clase para calcular derivadas de funciones matemáticas
 * Utiliza la biblioteca Symja para realizar diferenciación simbólica
 */
public class DerivativeCalculator {
    private ExprEvaluator evaluator;

    /**
     * Constructor que inicializa el evaluador de expresiones
     */
    public DerivativeCalculator() {
        this.evaluator = new ExprEvaluator();
    }

    /**
     * Calcula la derivada simbólica de una función
     * @param function La función a derivar (como String)
     * @return La función derivada (como String)
     */
    public String differentiate(String function) {
        try {
            // Reemplazar notación de potencia si es necesario
            function = function.replaceAll("\\^", "**");
            
            // Crear la expresión de derivada: D[function, x]
            String diffExpr = "D[" + function + ", x]";
            
            // Evaluar la expresión
            IExpr result = evaluator.eval(diffExpr);
            
            // Convertir el resultado a String y formatear
            String derivative = result.toString();
            
            // Reemplazar notación de potencia de vuelta si es necesario
            derivative = derivative.replaceAll("\\*\\*", "^");
            
            return derivative;
        } catch (Exception e) {
            System.err.println("Error al calcular la derivada: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Evalúa la derivada de una función en un punto específico
     * @param function La función a derivar
     * @param x El valor de x donde evaluar la derivada
     * @return El valor de la derivada en el punto x
     */
    public double evaluateDerivativeAt(String function, double x) {
        try {
            // Obtener la expresión de la derivada
            String derivative = differentiate(function);
            
            // Reemplazar x con el valor específico
            String expr = derivative.replaceAll("(?<![a-zA-Z0-9_])x(?![a-zA-Z0-9_])", "(" + x + ")");
            
            // Evaluar la expresión
            IExpr result = evaluator.eval(expr);
            
            // Convertir el resultado a un número
            return Double.parseDouble(result.toString());
        } catch (Exception e) {
            System.err.println("Error al evaluar la derivada en x=" + x + ": " + e.getMessage());
            return Double.NaN;
        }
    }
}