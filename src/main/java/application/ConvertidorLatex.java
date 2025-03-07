package application;

import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IExpr;

public class ConvertidorLatex {

    public static String toLatex(String input) {
        try {
            String formulaAjustada = input.replace("sqrt", "Sqrt");

            ExprEvaluator evaluator = new ExprEvaluator();
            IExpr result = evaluator.eval("TeXForm[" + formulaAjustada + "]");

            String latex = result.toString().replaceAll("\"", "");
            return latex;
        } catch (Exception e) {
            return "Error en la ecuación: " + e.getMessage();
        }
    }
}
