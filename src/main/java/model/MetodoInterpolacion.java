package model;

/**
 * Implementación del Método de Interpolación de Lagrange
 */
public class MetodoInterpolacion {

    private final double[] x; // Valores conocidos de x
    private final double[] y; // Valores conocidos de y
    private final int n;      // Cantidad de puntos

    /**
     * Constructor que inicializa los puntos para interpolación.
     *
     * @param puntosX Array de valores x
     * @param puntosY Array de valores y correspondientes
     */
    public MetodoInterpolacion(double[] puntosX, double[] puntosY) {
        if (puntosX.length != puntosY.length) {
            throw new IllegalArgumentException("Los arrays de x e y deben tener la misma longitud.");
        }

        if (tieneDuplicados(puntosX)) {
            throw new IllegalArgumentException("Los valores del array x no deben estar duplicados.");
        }

        this.x = puntosX.clone();
        this.y = puntosY.clone();
        this.n = puntosX.length;
    }

    /**
     * Evalúa el polinomio de Lagrange en un valor x dado.
     *
     * @param valorX Punto donde evaluar el polinomio
     * @return Resultado interpolado en valorX
     */
    public double interpolar(double valorX) {
        double resultado = 0.0;

        for (int i = 0; i < n; i++) {
            double termino = y[i]; // Inicializar con el valor y correspondiente

            for (int j = 0; j < n; j++) {
                if (i != j) {
                    termino *= (valorX - x[j]) / (x[i] - x[j]);
                }
            }

            resultado += termino;
        }

        return resultado;
    }

    /**
     * Genera el polinomio de Lagrange como una cadena legible.
     *
     * @return Representación del polinomio P(x)
     */
    public String obtenerPolinomio() {
        StringBuilder polinomio = new StringBuilder("P(x) = ");

        for (int i = 0; i < n; i++) {
            if (i > 0) {
                polinomio.append(" + ");
            }

            polinomio.append(String.format("%.4f", y[i]));

            for (int j = 0; j < n; j++) {
                if (i != j) {
                    polinomio.append(String.format(" * (x - %.4f) / (%.4f - %.4f)", x[j], x[i], x[j]));
                }
            }
        }

        return polinomio.toString();
    }

    /**
     * Verifica si los valores de x contienen duplicados.
     *
     * @param array Array de valores x
     * @return true si hay duplicados, false en caso contrario
     */
    private boolean tieneDuplicados(double[] array) {
        for (int i = 0; i < array.length; i++) {
            for (int j = i + 1; j < array.length; j++) {
                if (array[i] == array[j]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Valida si un punto dado está dentro del rango de interpolación.
     *
     * @param valorX Punto a verificar
     * @return true si está en el rango, false si no
     */
    public boolean esDentroDelRango(double valorX) {
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;

        for (double xi : x) {
            if (xi < minX) minX = xi;
            if (xi > maxX) maxX = xi;
        }

        return valorX >= minX && valorX <= maxX;
    }

    /**
     * Devuelve una descripción detallada del método y los puntos ingresados.
     *
     * @return String con información del método
     */
    public String obtenerInformacion() {
        StringBuilder info = new StringBuilder("=== INTERPOLACIÓN DE LAGRANGE ===\n");
        info.append(String.format("Número de puntos: %d\n", n));
        info.append("Puntos utilizados:\n");

        for (int i = 0; i < n; i++) {
            info.append(String.format("P%d: (%.4f, %.4f)\n", i + 1, x[i], y[i]));
        }

        return info.toString();
    }
}