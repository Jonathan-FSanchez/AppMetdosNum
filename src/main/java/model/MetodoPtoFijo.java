package model;

import com.example.prueba.ConvertidorLatex;
import javafx.application.Platform;
import javafx.scene.control.TableView;

public class MetodoPtoFijo {
    public void metodoPtoFijo(InfoMetodoPtoFijo inf, TableView<InfoMetodoPtoFijo> tblPtoFijo, String function) {
        double erAct;
        double x = inf.getX();
        System.out.println("\n");
        System.out.println("x[0]: " + x);

        // Verificar si hay punto fijo en el intervalo
        if (hayPuntoFijo(function, inf.getA(), inf.getB())) {
            if (cumpleCondicionContraccion(function, inf.getA(), inf.getB())) {
                System.out.println("La función cumple la condición de contracción en el intervalo.");
                for (int i = 0; i < inf.getiMax() ; i++){
                    InfoMetodoPtoFijo iter = new InfoMetodoPtoFijo();
                    iter.setN((i + 1));
                    System.out.println("\n");
                    System.out.println("x[" + (i) + "]: " + x);
                    iter.setX(x);

                    erAct = Math.abs((evaluateFunction(function, x) - x) / evaluateFunction(function, x));
                    System.out.println("erAct[" + (i) + "]: " + erAct);
                    iter.setErAct(erAct);
                    x = evaluateFunction(function, x);
                    System.out.println("\n" + x);

                    iter.setNextX(x);
                    Platform.runLater(() -> {
                        tblPtoFijo.getItems().add(iter);
                    });
                    if(Math.abs(erAct) <= inf.getErMax()){
                        System.out.println("Terminando Codigo");
                        break;
                    }
                }
            } else {
                System.out.println("Advertencia: No se garantiza convergencia debido a que no cumple la condición de contracción.");
            }
        } else {
            System.out.println("Imposible calcular: El intervalo no contiene un punto fijo.");

        }


    }

    private double evaluateFunction(String function, double x) {
        // Asumimos que g(x) = f(x) para simplificar (ajústalo según tu función)
        return ConvertidorLatex.evaluate(function, x);
    }

    private boolean hayPuntoFijo(String function, double a, double b) {
        double ga = evaluateFunction(function, a);
        double gb = evaluateFunction(function, b);

        double fa = ga - a;
        double fb = gb - b;

        System.out.println("g(a) = " + ga + ", g(b) = " + gb);
        System.out.println("g(a)-a = " + fa + ", g(b)-b = " + fb);

        return fa * fb <= 0;
    }

    private boolean cumpleCondicionContraccion(String function, double a, double b) {
        int pasos = 100;
        double paso = (b - a) / pasos;
        for (int i = 0; i <= pasos; i++) {
            double x = a + i * paso;
            // Aproximación de la derivada usando diferencias finitas
            double derivada = (evaluateFunction(function, x + 0.001) - evaluateFunction(function, x)) / 0.001;
            if (Math.abs(derivada) >= 1) {
                return false; // No cumple condición de contracción
            }
        }
        return true; // Sí cumple en todo el intervalo
    }
}