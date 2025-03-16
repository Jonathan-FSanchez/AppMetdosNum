package model;

import com.example.prueba.ConvertidorLatex;
import javafx.application.Platform;
import javafx.scene.control.TableView;

public class MetodoBiseccion {
    public void metodoBisec(InfoMetodoBiseccion inf, TableView<InfoMetodoBiseccion> tblMBiseccion, String function) {
        int i = 0;
        float c, erAct;
        double v1 = inf.getA();
        double v2 = inf.getB();
        double v3 = inf.getC();


        System.out.println("v1 = " + v1);
        System.out.println("v2 = " + v2);
        System.out.println("v3 = " + v3);
        System.out.println("resultado a: " + evaluateFunction(function, v1));
        System.out.println("resultado a: " + evaluateFunction(function, v2));
        if (evaluateFunction(function, v1) * evaluateFunction(function, v2) > 0) {
            System.out.println("Imposible calcular: El intervalo no contiene una raíz.");
        } else {
            while (i < inf.getiMax()) {
                c = (inf.getA() + inf.getB()) / 2;
                erAct = Math.abs(inf.getB() - inf.getA()) / 2;

                InfoMetodoBiseccion iter = new InfoMetodoBiseccion();
                iter.setA(inf.getA());
                iter.setB(inf.getB());
                iter.setC(c);
                iter.setErAct(erAct);
                iter.setiMax(i + 1);

                Platform.runLater(() -> {
                    tblMBiseccion.getItems().add(iter);
                });

                if (erAct <= inf.getErMax()) {
                    break;
                }

                if (evaluateFunction(function, inf.getA()) * evaluateFunction(function, c) < 0) {
                    inf.setB(c);
                } else {
                    inf.setA(c);
                }
                i++;
            }
        }
    }

    private double evaluateFunction(String function, double x) {
        return ConvertidorLatex.evaluate(function, x);
    }

    public static double f(double x) {
        return 2 * Math.pow(x, 3) - Math.pow(x, 2) + x - 1;
    }
}









/*
package model;


import javafx.application.Platform;
import javafx.scene.control.TableView;

public class MetodoBiseccion {
    public void metodoBisec(InfoMetodoBiseccion inf, TableView<InfoMetodoBiseccion> tblMBiseccion){
        int i = 0;
        float c, erAct;
        if(f(inf.getA()) * f(inf.getB()) > 0){
            System.out.println("Imposible calcular");
        } else {
            while(i < inf.getiMax()){
                System.out.println("--------> i: " + (i + 1));
                c = (inf.getA() + inf.getB()) / 2;
                erAct = (inf.getB() - inf.getA()) / 2;

                InfoMetodoBiseccion iter = new InfoMetodoBiseccion();
                iter.setA(inf.getA());
                iter.setB(inf.getB());
                iter.setC(c);
                iter.setErAct(erAct);
                iter.setiMax(i + 1);

                Platform.runLater(() -> {
                    tblMBiseccion.getItems().add(iter);
                });

                if (erAct <= inf.getErMax()){
                    break;
                }

                if(f(inf.getA()) * f(c) < 0){
                    inf.setB(c);
                } else{
                    inf.setA(c);
                }
                i++;
            }

        }
    }

    public static double f(double x){
        return 2 * Math.pow(x, 3) - Math.pow(x, 2) + x - 1;
    }
}
*/