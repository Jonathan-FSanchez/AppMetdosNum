package model;

public class InfoMetodoFalsaPosicion {
    private double a;
    private double b;
    private double c;
    private double erAct;
    private double erMax;
    private int iMax, i;

    public InfoMetodoFalsaPosicion(double a, double b, double c, double erAct, double erMax, int iMax) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.erAct = erAct;
        this.erMax = erMax;
        this.iMax = iMax;
    }

    public InfoMetodoFalsaPosicion() {
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getC() {
        return c;
    }

    public void setC(double c) {
        this.c = c;
    }

    public double getErAct() {
        return erAct;
    }

    public void setErAct(double erAct) {
        this.erAct = erAct;
    }

    public double getErMax() {
        return erMax;
    }

    public void setErMax(double erMax) {
        this.erMax = erMax;
    }

    public int getiMax() {
        return iMax;
    }

    public void setiMax(int iMax) {
        this.iMax = iMax;
    }
}