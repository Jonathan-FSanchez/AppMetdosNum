package model;

public class InfoMetodoBiseccion {
    private float a;
    private float b;
    private float c;
    private float erAct;
    private float erMax;
    private int iMax, i;

    public InfoMetodoBiseccion(float a, float b, float c, float erAct, float erMax, int iMax) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.erAct = erAct;
        this.erMax = erMax;
        this.iMax = iMax;
    }

    public InfoMetodoBiseccion() {
    }

    public float getA() {
        return a;
    }

    public void setA(float a) {
        this.a = a;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }

    public float getC() {
        return c;
    }

    public void setC(float c) {
        this.c = c;
    }

    public float getErAct() {
        return erAct;
    }

    public void setErAct(float erAct) {
        this.erAct = erAct;
    }

    public float getErMax() {
        return erMax;
    }

    public void setErMax(float erMax) {
        this.erMax = erMax;
    }

    public int getiMax() {
        return iMax;
    }

    public void setiMax(int iMax) {
        this.iMax = iMax;
    }
}
