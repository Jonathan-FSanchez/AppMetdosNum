package model;

public class InfoMetodoNewtonRaphson {
    private int iteracion;
    private double x;
    private double fx;
    private double dfx;
    private double nextX;
    private double error;

    public InfoMetodoNewtonRaphson() {
    }

    public InfoMetodoNewtonRaphson(int iteracion, double x, double fx, double dfx, double nextX, double error) {
        this.iteracion = iteracion;
        this.x = x;
        this.fx = fx;
        this.dfx = dfx;
        this.nextX = nextX;
        this.error = error;
    }

    public int getIteracion() {
        return iteracion;
    }

    public void setIteracion(int iteracion) {
        this.iteracion = iteracion;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getFx() {
        return fx;
    }

    public void setFx(double fx) {
        this.fx = fx;
    }

    public double getDfx() {
        return dfx;
    }

    public void setDfx(double dfx) {
        this.dfx = dfx;
    }

    public double getNextX() {
        return nextX;
    }

    public void setNextX(double nextX) {
        this.nextX = nextX;
    }

    public double getError() {
        return error;
    }

    public void setError(double error) {
        this.error = error;
    }
}