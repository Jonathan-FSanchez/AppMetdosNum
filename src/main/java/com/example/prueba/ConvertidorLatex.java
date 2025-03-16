package com.example.prueba; // Ajusta el paquete según tu estructura

import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IExpr;

public class ConvertidorLatex {
    public static String toLatex(String input) {
        try {
            // Limpiar la entrada y manejar multiplicaciones implícitas
            String formulaAjustada = input.trim();
            if (formulaAjustada.isEmpty()) {
                return "Esperando...";
            }

            // Reemplazar multiplicaciones implícitas (ej. "5x" -> "5*x")
            formulaAjustada = formulaAjustada.replaceAll("(\\d)([a-zA-Z])", "$1*$2");

            // Verificar si hay un "^" sin exponente (ej. "5x^")
            if (formulaAjustada.matches(".*\\^$")) {
                return "Esperando...";
            }

            // Intentar evaluar con Symja
            ExprEvaluator evaluator = new ExprEvaluator();
            IExpr result = evaluator.eval("TeXForm[" + formulaAjustada + "]");
            String latex = result.toString().replaceAll("\"", "");
            return latex.isEmpty() ? "Esperando..." : latex; // Si falla la conversión, devolver "Esperando..."
        } catch (Exception e) {
            System.out.println("Error en ConvertidorLatex: " + e.getMessage()); // Depuración
            return "Esperando..."; // Mostrar "Esperando..." en lugar de un error detallado
        }
    }

    public static double evaluate(String input, double x) {
        try {
            String formulaAjustada = input.replace("sqrt", "Sqrt").replace("x", String.valueOf(x));
            System.out.println(formulaAjustada );
            ExprEvaluator evaluator = new ExprEvaluator();
            IExpr result = evaluator.eval(formulaAjustada);
            return result.evalf();

        } catch (Exception e) {
            System.out.println("Error al evaluar: " + e.getMessage());
            return Double.NaN;
        }
    }
}