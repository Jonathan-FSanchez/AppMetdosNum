package controllers;

import application.App;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.plugins.DataPointTooltip;
import de.gsi.chart.plugins.Panner;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.spi.DoubleDataSet;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import model.MetodoInterpolacion;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para el método de interpolación de Lagrange
 */
public class CtrlMetodoInterpolacion extends TopBarController {

    @FXML private StackPane chartContainer;
    @FXML private Label lblCurrentPoints;
    @FXML private Label lblResultado;
    @FXML private Label lblPolinomio;
    @FXML private TextField txtX;
    @FXML private TextField txtY;
    @FXML private TextField txtValorInterpolar;
    @FXML private TableView<Punto> tblPuntos;
    @FXML private TableColumn<Punto, Integer> colIndice;
    @FXML private TableColumn<Punto, Double> colX;
    @FXML private TableColumn<Punto, Double> colY;

    private XYChart chart;
    private List<Punto> puntos = new ArrayList<>();
    private MetodoInterpolacion interpolacion;

    /**
     * Clase para representar un punto (x,y) en la tabla
     */
    public static class Punto {
        private final int indice;
        private final double x;
        private final double y;

        public Punto(int indice, double x, double y) {
            this.indice = indice;
            this.x = x;
            this.y = y;
        }

        public int getIndice() {
            return indice;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }

    @FXML
    public void initialize() {
        initializeTopBar();
        setupChart();
        setupTable();
        actualizarLabelPuntos();
    }

    /**
     * Configura la tabla de puntos
     */
    private void setupTable() {
        colIndice.setCellValueFactory(new PropertyValueFactory<>("indice"));
        colX.setCellValueFactory(new PropertyValueFactory<>("x"));
        colY.setCellValueFactory(new PropertyValueFactory<>("y"));
        tblPuntos.setItems(javafx.collections.FXCollections.observableArrayList(puntos));
    }

    /**
     * Configura el gráfico para visualizar los puntos y la interpolación
     */
    private void setupChart() {
        DefaultNumericAxis xAxis = new DefaultNumericAxis("x", "");
        DefaultNumericAxis yAxis = new DefaultNumericAxis("y", "");

        chart = new XYChart(xAxis, yAxis);
        chart.getPlugins().add(new Zoomer());
        chart.getPlugins().add(new Panner());
        chart.getPlugins().add(new DataPointTooltip());

        ErrorDataSetRenderer renderer = new ErrorDataSetRenderer();
        renderer.setErrorType(ErrorStyle.NONE);
        chart.getRenderers().add(renderer);

        chartContainer.getChildren().add(chart);
    }

    /**
     * Actualiza el gráfico con los puntos actuales
     */
    private void actualizarGrafico() {
        if (puntos.isEmpty()) {
            chart.getDatasets().clear();
            return;
        }

        DoubleDataSet dataSet = new DoubleDataSet("Puntos");
        for (Punto punto : puntos) {
            dataSet.add(punto.getX(), punto.getY());
        }

        chart.getDatasets().clear();
        chart.getDatasets().add(dataSet);
    }

    /**
     * Actualiza el gráfico con la curva de interpolación
     */
    private void actualizarGraficoConInterpolacion(double valorX, double resultado) {
        if (puntos.size() < 2) return;

        // Encontrar el rango de x
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        for (Punto punto : puntos) {
            if (punto.getX() < minX) minX = punto.getX();
            if (punto.getX() > maxX) maxX = punto.getX();
        }

        // Ampliar un poco el rango para visualización
        double rango = maxX - minX;
        minX -= rango * 0.1;
        maxX += rango * 0.1;

        // Crear dataset para la curva de interpolación
        DoubleDataSet curvaDataSet = new DoubleDataSet("Interpolación");
        double paso = (maxX - minX) / 100;
        for (double x = minX; x <= maxX; x += paso) {
            try {
                double y = interpolacion.interpolar(x);
                curvaDataSet.add(x, y);
            } catch (Exception e) {
                // Ignorar puntos que no se puedan interpolar
            }
        }

        // Crear dataset para el punto interpolado
        DoubleDataSet puntoInterpolado = new DoubleDataSet("Punto interpolado");
        puntoInterpolado.add(valorX, resultado);

        // Actualizar gráfico
        chart.getDatasets().clear();

        // Primero agregar la curva
        chart.getDatasets().add(curvaDataSet);

        // Luego agregar los puntos originales
        DoubleDataSet puntosOriginales = new DoubleDataSet("Puntos originales");
        for (Punto punto : puntos) {
            puntosOriginales.add(punto.getX(), punto.getY());
        }
        chart.getDatasets().add(puntosOriginales);

        // Finalmente agregar el punto interpolado
        chart.getDatasets().add(puntoInterpolado);
    }

    /**
     * Actualiza la etiqueta que muestra los puntos actuales
     */
    private void actualizarLabelPuntos() {
        if (puntos.isEmpty()) {
            lblCurrentPoints.setText("No se han ingresado puntos");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < puntos.size(); i++) {
                Punto p = puntos.get(i);
                sb.append(String.format("P%d(%.2f, %.2f)", i + 1, p.getX(), p.getY()));
                if (i < puntos.size() - 1) {
                    sb.append(", ");
                }
            }
            lblCurrentPoints.setText(sb.toString());
        }
    }

    /**
     * Agrega un nuevo punto a la lista
     */
    @FXML
    public void agregarPunto() {
        try {
            double x = Double.parseDouble(txtX.getText().trim());
            double y = Double.parseDouble(txtY.getText().trim());

            // Verificar si ya existe un punto con el mismo valor x
            for (Punto punto : puntos) {
                if (punto.getX() == x) {
                    mostrarError("Error", "Ya existe un punto con el valor X = " + x);
                    return;
                }
            }

            Punto nuevoPunto = new Punto(puntos.size() + 1, x, y);
            puntos.add(nuevoPunto);

            // Actualizar la tabla
            tblPuntos.getItems().clear();
            tblPuntos.getItems().addAll(puntos);

            // Actualizar el gráfico
            actualizarGrafico();

            // Actualizar la etiqueta de puntos
            actualizarLabelPuntos();

            // Limpiar los campos
            txtX.clear();
            txtY.clear();
            txtX.requestFocus();

        } catch (NumberFormatException e) {
            mostrarError("Error", "Los valores X e Y deben ser números válidos");
        }
    }

    /**
     * Limpia todos los puntos
     */
    @FXML
    public void limpiarPuntos() {
        puntos.clear();
        tblPuntos.getItems().clear();
        chart.getDatasets().clear();
        actualizarLabelPuntos();
        lblResultado.setText("Ejecute la interpolación para ver el resultado");
        lblPolinomio.setText("Ejecute la interpolación para ver el polinomio");
    }

    /**
     * Realiza la interpolación con el valor ingresado
     */
    @FXML
    public void interpolar() {
        if (puntos.size() < 2) {
            mostrarError("Error", "Se necesitan al menos 2 puntos para realizar la interpolación");
            return;
        }

        try {
            double valorX = Double.parseDouble(txtValorInterpolar.getText().trim());

            // Preparar los arrays para la interpolación
            double[] puntosX = new double[puntos.size()];
            double[] puntosY = new double[puntos.size()];

            for (int i = 0; i < puntos.size(); i++) {
                puntosX[i] = puntos.get(i).getX();
                puntosY[i] = puntos.get(i).getY();
            }

            // Crear el objeto de interpolación
            interpolacion = new MetodoInterpolacion(puntosX, puntosY);

            // Verificar si el punto está dentro del rango
            boolean dentroRango = interpolacion.esDentroDelRango(valorX);

            // Realizar la interpolación
            double resultado = interpolacion.interpolar(valorX);

            // Mostrar el resultado
            lblResultado.setText(String.format("f(%.4f) = %.6f %s", 
                valorX, resultado, 
                dentroRango ? "" : " (Advertencia: fuera del rango de interpolación)"));

            // Mostrar el polinomio
            lblPolinomio.setText(interpolacion.obtenerPolinomio());

            // Actualizar el gráfico con la interpolación
            actualizarGraficoConInterpolacion(valorX, resultado);

            // Animar el resultado
            animarResultado();

        } catch (NumberFormatException e) {
            mostrarError("Error", "El valor a interpolar debe ser un número válido");
        } catch (Exception e) {
            mostrarError("Error", "Error al realizar la interpolación: " + e.getMessage());
        }
    }

    /**
     * Anima el resultado para destacarlo
     */
    private void animarResultado() {
        // Animación para el resultado
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), lblResultado);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.3);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), lblResultado);
        fadeIn.setFromValue(0.3);
        fadeIn.setToValue(1.0);

        SequentialTransition sequence = new SequentialTransition(fadeOut, fadeIn, fadeOut, fadeIn);
        sequence.play();

        // Animación para el polinomio
        FadeTransition fadeOutPoly = new FadeTransition(Duration.millis(200), lblPolinomio);
        fadeOutPoly.setFromValue(1.0);
        fadeOutPoly.setToValue(0.3);

        FadeTransition fadeInPoly = new FadeTransition(Duration.millis(200), lblPolinomio);
        fadeInPoly.setFromValue(0.3);
        fadeInPoly.setToValue(1.0);

        SequentialTransition sequencePoly = new SequentialTransition(fadeOutPoly, fadeInPoly);
        sequencePoly.setDelay(Duration.millis(400));
        sequencePoly.play();
    }

    /**
     * Muestra un diálogo de error
     */
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Acción para el botón de volver al menú principal
     */
    @FXML
    public void btnVMain(ActionEvent event) {
        try {
            App.app.setScene(utils.Paths.MENU_PRINCIPAL);
        } catch (Exception e) {
            mostrarError("Error", "Error al cargar la pantalla principal: " + e.getMessage());
        }
    }

    /**
     * Acción para el botón de salir
     */
    @FXML
    public void btnExit(ActionEvent event) {
        System.exit(0);
    }
}
