package model;

public class InfoMetodoSecante {
    private int iteracion;
    private double x0;
    private double x1;
    private double x2;
    private double error;

    public InfoMetodoSecante() {
    }

    public InfoMetodoSecante(int iteracion, double x0, double x1, double x2, double error) {
        this.iteracion = iteracion;
        this.x0 = x0;
        this.x1 = x1;
        this.x2 = x2;
        this.error = error;
    }

    public int getIteracion() {
        return iteracion;
    }

    public void setIteracion(int iteracion) {
        this.iteracion = iteracion;
    }

    public double getX0() {
        return x0;
    }

    public void setX0(double x0) {
        this.x0 = x0;
    }

    public double getX1() {
        return x1;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public double getX2() {
        return x2;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public double getError() {
        return error;
    }

    public void setError(double error) {
        this.error = error;
    }
}