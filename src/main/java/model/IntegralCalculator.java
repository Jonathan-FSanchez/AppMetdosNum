package model;

import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;

/**
 * Clase para calcular integrales de funciones matemáticas
 * Utiliza la biblioteca Symja para realizar integración simbólica
 */
public class IntegralCalculator {
    private ExprEvaluator evaluator;

    /**
     * Constructor que inicializa el evaluador de expresiones
     */
    public IntegralCalculator() {
        this.evaluator = new ExprEvaluator();
    }

    /**
     * Calcula la integral indefinida (antiderivada) de una función
     * @param function La función a integrar (como String)
     * @return La función integrada (como String)
     */
    public String integrate(String function) {
        try {
            // Reemplazar notación de potencia si es necesario
            function = function.replaceAll("\\^", "**");
            
            // Crear la expresión de integral: Integrate[function, x]
            String intExpr = "Integrate[" + function + ", x]";
            
            // Evaluar la expresión
            IExpr result = evaluator.eval(intExpr);
            
            // Convertir el resultado a String y formatear
            String integral = result.toString();
            
            // Reemplazar notación de potencia de vuelta si es necesario
            integral = integral.replaceAll("\\*\\*", "^");
            
            return integral;
        } catch (Exception e) {
            System.err.println("Error al calcular la integral: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Calcula la integral definida de una función en un intervalo
     * @param function La función a integrar
     * @param lowerBound Límite inferior de integración
     * @param upperBound Límite superior de integración
     * @return El valor de la integral definida
     */
    public double evaluateDefiniteIntegral(String function, double lowerBound, double upperBound) {
        try {
            // Reemplazar notación de potencia si es necesario
            function = function.replaceAll("\\^", "**");
            
            // Crear la expresión de integral definida: Integrate[function, {x, lowerBound, upperBound}]
            String intExpr = "Integrate[" + function + ", {x, " + lowerBound + ", " + upperBound + "}]";
            
            // Evaluar la expresión
            IExpr result = evaluator.eval(intExpr);
            
            // Convertir el resultado a un número
            return Double.parseDouble(result.toString());
        } catch (Exception e) {
            System.err.println("Error al calcular la integral definida: " + e.getMessage());
            return Double.NaN;
        }
    }
    
    /**
     * Evalúa la integral (antiderivada) de una función en un punto específico
     * @param function La función a integrar
     * @param x El valor de x donde evaluar la integral
     * @return El valor de la integral en el punto x
     */
    public double evaluateIntegralAt(String function, double x) {
        try {
            // Obtener la expresión de la integral
            String integral = integrate(function);
            
            // Reemplazar x con el valor específico
            String expr = integral.replaceAll("(?<![a-zA-Z0-9_])x(?![a-zA-Z0-9_])", "(" + x + ")");
            
            // Evaluar la expresión
            IExpr result = evaluator.eval(expr);
            
            // Convertir el resultado a un número
            return Double.parseDouble(result.toString());
        } catch (Exception e) {
            System.err.println("Error al evaluar la integral en x=" + x + ": " + e.getMessage());
            return Double.NaN;
        }
    }
}