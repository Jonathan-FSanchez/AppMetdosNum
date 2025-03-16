package model;

public class InfoMetodoPtoFijo {
    private double a;
    private double b;
    private double x;
    private double erAct;
    private double erMax;
    private int iMax;
    private int N;
    private double NextX;
    private double Error;

    public InfoMetodoPtoFijo() {
    }

    public double getNextX() {
        return NextX;
    }

    public void setNextX(double nextX) {
        NextX = nextX;
    }

    public int getN() {
        return N;
    }

    public void setN(int n) {
        N = n;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
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

    public double getiMax() {
        return iMax;
    }

    public void setiMax(int iMax) {
        this.iMax = iMax;
    }


    public InfoMetodoPtoFijo(double a, double b, double x, double erAct, double erMax, int iMax) {
        this.a = a;
        this.b = b;
        this.x = x;
        this.erAct = erAct;
        this.erMax = erMax;
        this.iMax = iMax;
    }
}
