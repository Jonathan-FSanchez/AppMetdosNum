package controllers;

import com.example.prueba.ConvertidorLatex;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.plugins.DataPointTooltip;
import de.gsi.chart.plugins.Panner;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.spi.DoubleDataSet;
import javafx.animation.FillTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import model.InfoMetodoFalsaPosicion;
import model.MetodoFalsaPosicion;

public class CtrlMetodoFalsaPosicion extends TopBarController {

    @FXML private TableColumn<InfoMetodoFalsaPosicion, Float> colA;
    @FXML private TableColumn<InfoMetodoFalsaPosicion, Float> colB;
    @FXML private TableColumn<InfoMetodoFalsaPosicion, Float> colC;
    @FXML private TableColumn<InfoMetodoFalsaPosicion, Float> colError;
    @FXML private TableColumn<InfoMetodoFalsaPosicion, Float> colFa;
    @FXML private TableColumn<InfoMetodoFalsaPosicion, Float> colFb;
    @FXML private TableColumn<InfoMetodoFalsaPosicion, Float> colFc;
    @FXML private TableColumn<InfoMetodoFalsaPosicion, Integer> colN;
    @FXML private TableView<InfoMetodoFalsaPosicion> tblMFalsaPosicion;
    @FXML private MenuItem menuMFalsaPosicion;
    @FXML private TextField txtA;
    @FXML private TextField txtB;
    @FXML private TextField txtErMax;
    @FXML private TextField txtIMax;
    @FXML private javafx.scene.control.Label lblCurrentFunction;
    @FXML private javafx.scene.control.Label lblFinalResult;
    @FXML private StackPane chartContainer;

    // Chart components
    private XYChart chart;
    private DefaultNumericAxis xAxis;
    private DefaultNumericAxis yAxis;
    private DoubleDataSet functionDataSet;
    private DoubleDataSet falsaPosicionPointsDataSet;
    private DoubleDataSet rootDataSet;

    // Animation components
    private Rectangle leftInterval;
    private Rectangle rightInterval;
    private Line midpointLine;

    // Constants for chart
    private static final double CHART_PADDING = 0.5;
    private static final String[] COLORS = {"#3f51b5", "#e91e63", "#009688", "#ff5722", "#607d8b"};

    private String userFunction; // Variable para almacenar la función ingresada

    @FXML
    void initialize() {
        initializeTopBar();

        // Configuración de las columnas
        colN.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getiMax()).asObject());
        colA.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty((float)cellData.getValue().getA()).asObject());
        colB.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty((float)cellData.getValue().getB()).asObject());
        colC.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty((float)cellData.getValue().getC()).asObject());
        colFa.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty((float) evaluateFunction(cellData.getValue().getA())).asObject());
        colFb.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty((float) evaluateFunction(cellData.getValue().getB())).asObject());
        colFc.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty((float) evaluateFunction(cellData.getValue().getC())).asObject());
        colError.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty((float)cellData.getValue().getErAct()).asObject());

        // Configurar el gráfico
        setupChart();
    }

    /**
     * Configura el gráfico para visualizar la función y el proceso de falsa posición
     */
    private void setupChart() {
        try {
            // Crear ejes con etiquetas y rangos iniciales
            xAxis = new DefaultNumericAxis(-10, 10, 1);
            yAxis = new DefaultNumericAxis(-10, 10, 1);
            xAxis.setName("x");
            yAxis.setName("y");

            // Configurar opciones de ejes
            xAxis.setAnimated(false);
            yAxis.setAnimated(false);
            xAxis.setAutoRangeRounding(true);
            yAxis.setAutoRangeRounding(true);

            // Configurar ejes para que se crucen en el origen
            xAxis.setForceZeroInRange(true);
            yAxis.setForceZeroInRange(true);

            // Crear gráfico XY
            chart = new XYChart(xAxis, yAxis);

            // Configurar el estilo sin cuadrícula
            chart.getGridRenderer().setDrawOnTop(false);
            chart.getGridRenderer().setVisible(false);
            chart.setAnimated(false);

            // Añadir renderer optimizado
            ErrorDataSetRenderer renderer = new ErrorDataSetRenderer();
            renderer.setDrawMarker(false);
            renderer.setErrorType(ErrorStyle.NONE);
            chart.getRenderers().add(renderer);

            // Añadir plugins interactivos
            chart.getPlugins().add(new Zoomer());
            chart.getPlugins().add(new Panner());
            chart.getPlugins().add(new DataPointTooltip());

            // Crear conjuntos de datos
            functionDataSet = new DoubleDataSet("Función");
            falsaPosicionPointsDataSet = new DoubleDataSet("Puntos de Falsa Posición");
            rootDataSet = new DoubleDataSet("Raíz");

            // Inicializar elementos visuales para la animación
            leftInterval = new Rectangle();
            leftInterval.getStyleClass().add("interval-highlight");
            leftInterval.setOpacity(0.3);

            rightInterval = new Rectangle();
            rightInterval.getStyleClass().add("interval-highlight");
            rightInterval.setOpacity(0.3);

            midpointLine = new Line();
            midpointLine.getStyleClass().add("midpoint-line");
            midpointLine.setStrokeWidth(2);
            midpointLine.setStroke(Color.ORANGE);

            // Añadir gráfico al contenedor
            if (chartContainer != null) {
                chartContainer.getChildren().clear();
                chartContainer.getChildren().add(chart);

                // Añadir elementos visuales al contenedor
                chartContainer.getChildren().addAll(leftInterval, rightInterval, midpointLine);

                // Inicialmente ocultos
                leftInterval.setVisible(false);
                rightInterval.setVisible(false);
                midpointLine.setVisible(false);
            }
        } catch (Exception e) {
            System.err.println("Error al configurar el gráfico: " + e.getMessage());
        }
    }

    /**
     * Grafica la función actual en el chart
     */
    private void graficarFuncion() {
        if (userFunction == null || userFunction.trim().isEmpty()) {
            return;
        }

        try {
            // Limpiar datos anteriores
            functionDataSet.clearData();

            // Obtener rango de los valores de entrada o usar valores predeterminados
            double a, b;
            try {
                a = Double.parseDouble(txtA.getText());
                b = Double.parseDouble(txtB.getText());
            } catch (Exception e) {
                // Usar valores predeterminados si no hay valores válidos
                a = -5.0;
                b = 5.0;
            }

            // Ajustar el rango para mostrar un poco más allá de los límites
            double min = Math.min(a, b) - CHART_PADDING;
            double max = Math.max(a, b) + CHART_PADDING;

            // Actualizar ejes
            xAxis.set(min, max);

            // Calcular puntos para la función
            int points = 200;
            double step = (max - min) / points;

            // Calcular valores mínimos y máximos de y para ajustar el eje y
            double minY = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;

            // Calcular puntos
            for (double x = min; x <= max; x += step) {
                try {
                    double y = evaluateFunction(x);

                    // Actualizar min/max de y
                    if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                        minY = Math.min(minY, y);
                        maxY = Math.max(maxY, y);
                        functionDataSet.add(x, y);
                    }
                } catch (Exception e) {
                    // Ignorar puntos que no se pueden evaluar
                }
            }

            // Ajustar eje y con padding
            if (minY != Double.MAX_VALUE && maxY != Double.MIN_VALUE) {
                double yPadding = (maxY - minY) * 0.2;
                yAxis.set(minY - yPadding, maxY + yPadding);
            }

            // Añadir el conjunto de datos al gráfico si no está ya
            if (!chart.getDatasets().contains(functionDataSet)) {
                chart.getDatasets().add(functionDataSet);
                functionDataSet.setStyle("strokeColor=" + COLORS[0] + ";strokeWidth=2;");
            }
        } catch (Exception e) {
            System.err.println("Error al graficar la función: " + e.getMessage());
        }
    }

    @FXML
    void ejecutar(ActionEvent event) {
        tblMFalsaPosicion.getItems().clear();

        // Limpiar gráficos anteriores
        falsaPosicionPointsDataSet.clearData();
        rootDataSet.clearData();

        // Resetear el resultado final
        lblFinalResult.setText("Ejecute el método para ver el resultado");

        // Validar que se haya ingresado una función
        if (userFunction == null || userFunction.trim().isEmpty()) {
            mostrarError("No se ha ingresado una función. Usa la vista principal para ingresarla.");
            return;
        }

        // Validar los campos de entrada
        try {
            // Validar que los campos no estén vacíos
            if (txtA.getText().trim().isEmpty() || txtB.getText().trim().isEmpty() || 
                txtIMax.getText().trim().isEmpty() || txtErMax.getText().trim().isEmpty()) {
                mostrarError("Todos los campos son obligatorios");
                return;
            }

            // Parsear valores
            double a = Double.parseDouble(txtA.getText());
            double b = Double.parseDouble(txtB.getText());
            int iMax = Integer.parseInt(txtIMax.getText());
            double erMax = Double.parseDouble(txtErMax.getText());

            // Validar valores
            if (a >= b) {
                mostrarError("El valor de 'a' debe ser menor que 'b'");
                return;
            }

            if (iMax <= 0) {
                mostrarError("La iteración máxima debe ser mayor que 0");
                return;
            }

            if (erMax <= 0) {
                mostrarError("El error máximo debe ser mayor que 0");
                return;
            }

            // Graficar la función
            graficarFuncion();

            // Añadir los conjuntos de datos para falsa posición y raíz si no están ya
            if (!chart.getDatasets().contains(falsaPosicionPointsDataSet)) {
                chart.getDatasets().add(falsaPosicionPointsDataSet);
                falsaPosicionPointsDataSet.setStyle("strokeColor=" + COLORS[1] + ";strokeWidth=0;markerSize=5;markerType=CIRCLE");
            } else {
                falsaPosicionPointsDataSet.clearData();
            }

            if (!chart.getDatasets().contains(rootDataSet)) {
                chart.getDatasets().add(rootDataSet);
                rootDataSet.setStyle("strokeColor=" + COLORS[2] + ";strokeWidth=0;markerSize=8;markerType=DIAMOND");
            } else {
                rootDataSet.clearData();
            }

            // Crear objeto con los parámetros
            InfoMetodoFalsaPosicion inf = new InfoMetodoFalsaPosicion();
            inf.setA(a);
            inf.setB(b);
            inf.setC((a + b) / 2); // Valor inicial de c (se actualizará en el método)
            inf.setiMax(iMax);
            inf.setErMax(erMax);

            // Ejecutar el método de falsa posición directamente para mostrar las iteraciones en el datagrid
            MetodoFalsaPosicion metodo = new MetodoFalsaPosicion();
            metodo.metodoFalsaPosicion(inf, tblMFalsaPosicion, userFunction);

            // Mostrar el resultado final
            if (!tblMFalsaPosicion.getItems().isEmpty()) {
                InfoMetodoFalsaPosicion ultimaIteracion = tblMFalsaPosicion.getItems().get(tblMFalsaPosicion.getItems().size() - 1);
                double raiz = ultimaIteracion.getC();
                double error = ultimaIteracion.getErAct();
                double valorFuncion = evaluateFunction(raiz);

                // Mostrar el resultado final
                lblFinalResult.setText(String.format("Raíz aproximada: %.6f | Error: %.6f | f(raíz): %.6f", 
                                                   raiz, error, valorFuncion));

                // Ejecutar el método de falsa posición con animación para visualizar el proceso en la minigráfica
                ejecutarMetodoFalsaPosicionConAnimacion(inf);
            }

        } catch (NumberFormatException e) {
            mostrarError("Por favor, ingrese valores numéricos válidos");
        } catch (Exception e) {
            mostrarError("Error al ejecutar el método: " + e.getMessage());
        }
    }

    /**
     * Actualiza el gráfico con los puntos de falsa posición y la raíz
     */
    private void actualizarGraficoConResultados() {
        try {
            // Limpiar datos anteriores
            falsaPosicionPointsDataSet.clearData();
            rootDataSet.clearData();

            // Obtener todas las iteraciones de la tabla
            for (InfoMetodoFalsaPosicion iter : tblMFalsaPosicion.getItems()) {
                // Añadir puntos a, b y c al gráfico
                falsaPosicionPointsDataSet.add(iter.getA(), evaluateFunction(iter.getA()));
                falsaPosicionPointsDataSet.add(iter.getB(), evaluateFunction(iter.getB()));
                falsaPosicionPointsDataSet.add(iter.getC(), evaluateFunction(iter.getC()));
            }

            // Añadir la raíz (último punto c) al conjunto de datos de raíz
            if (!tblMFalsaPosicion.getItems().isEmpty()) {
                InfoMetodoFalsaPosicion ultimaIteracion = tblMFalsaPosicion.getItems().get(tblMFalsaPosicion.getItems().size() - 1);
                double raiz = ultimaIteracion.getC();
                rootDataSet.add(raiz, evaluateFunction(raiz));
            }
        } catch (Exception e) {
            System.err.println("Error al actualizar el gráfico: " + e.getMessage());
        }
    }

    /**
     * Ejecuta el método de falsa posición con animaciones visuales
     */
    private void ejecutarMetodoFalsaPosicionConAnimacion(InfoMetodoFalsaPosicion inf) {
        try {
            // Crear secuencia de animaciones
            SequentialTransition sequentialTransition = new SequentialTransition();

            // Añadir animación inicial para mostrar los puntos a y b
            ParallelTransition initialAnimation = new ParallelTransition();

            // Añadir puntos iniciales a y b
            final double a = inf.getA();
            final double b = inf.getB();
            final double fa = evaluateFunction(a);
            final double fb = evaluateFunction(b);

            falsaPosicionPointsDataSet.add(a, fa);
            falsaPosicionPointsDataSet.add(b, fb);

            // Mostrar el intervalo inicial
            Timeline showInitialIntervalAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    // Hacer visibles los elementos visuales
                    leftInterval.setVisible(true);
                    rightInterval.setVisible(true);

                    // Calcular posiciones en el gráfico
                    double minY = Math.min(fa, fb);
                    double maxY = Math.max(fa, fb);
                    double yPadding = (maxY - minY) * 0.5;

                    // Ajustar para que cubra todo el rango visible de y
                    minY = Math.min(minY, yAxis.getMin());
                    maxY = Math.max(maxY, yAxis.getMax());

                    // Configurar rectángulos para mostrar el intervalo
                    updateIntervalRectangles(a, b, minY, maxY);
                }),
                new KeyFrame(Duration.millis(300))
            );
            initialAnimation.getChildren().add(showInitialIntervalAnimation);

            // Pausa inicial
            PauseTransition initialPause = new PauseTransition(Duration.millis(500));
            initialAnimation.getChildren().add(initialPause);
            sequentialTransition.getChildren().add(initialAnimation);

            // Lista para almacenar las iteraciones
            java.util.List<InfoMetodoFalsaPosicion> iteraciones = new java.util.ArrayList<>();

            // Ejecutar el método de falsa posición y almacenar cada iteración
            MetodoFalsaPosicion metodo = new MetodoFalsaPosicion();
            metodo.metodoFalsaPosicionAnimado(inf, iteraciones, userFunction);

            // Crear animaciones para cada iteración
            for (int i = 0; i < iteraciones.size(); i++) {
                InfoMetodoFalsaPosicion iter = iteraciones.get(i);

                // Crear animación para esta iteración
                ParallelTransition iterationAnimation = new ParallelTransition();

                // Añadir punto c (punto de falsa posición)
                final double iterA = iter.getA();
                final double iterB = iter.getB();
                final double c = iter.getC();
                final double fc = evaluateFunction(c);
                final double iterFa = evaluateFunction(iterA);
                final double iterFb = evaluateFunction(iterB);

                // Animación para añadir el punto c
                Timeline addPointAnimation = new Timeline(
                    new KeyFrame(Duration.ZERO, e -> {
                        falsaPosicionPointsDataSet.add(c, fc);

                        // Actualizar la línea del punto medio
                        midpointLine.setVisible(true);
                        midpointLine.setStartX(chart.getXAxis().getDisplayPosition(c));
                        midpointLine.setEndX(chart.getXAxis().getDisplayPosition(c));
                        midpointLine.setStartY(chart.getYAxis().getDisplayPosition(yAxis.getMin()));
                        midpointLine.setEndY(chart.getYAxis().getDisplayPosition(yAxis.getMax()));

                        // Actualizar los rectángulos del intervalo
                        double minY = Math.min(Math.min(iterFa, iterFb), fc);
                        double maxY = Math.max(Math.max(iterFa, iterFb), fc);
                        double yPadding = (maxY - minY) * 0.5;

                        // Ajustar para que cubra todo el rango visible de y
                        minY = Math.min(minY, yAxis.getMin());
                        maxY = Math.max(maxY, yAxis.getMax());

                        updateIntervalRectangles(iterA, iterB, minY, maxY);
                    }),
                    new KeyFrame(Duration.millis(300))
                );
                iterationAnimation.getChildren().add(addPointAnimation);

                // Añadir pausa entre iteraciones
                PauseTransition pause = new PauseTransition(Duration.millis(500));
                iterationAnimation.getChildren().add(pause);

                // No añadimos la iteración a la tabla porque ya lo hizo metodoFalsaPosicion
                // Solo hacemos scroll a la fila correspondiente
                final int index = i;
                Timeline scrollToRowAnimation = new Timeline(
                    new KeyFrame(Duration.ZERO, e -> {
                        if (index < tblMFalsaPosicion.getItems().size()) {
                            tblMFalsaPosicion.scrollTo(index);
                        }
                    })
                );
                iterationAnimation.getChildren().add(scrollToRowAnimation);

                // Añadir esta animación a la secuencia
                sequentialTransition.getChildren().add(iterationAnimation);
            }

            // Animación final para mostrar la raíz
            if (!iteraciones.isEmpty()) {
                InfoMetodoFalsaPosicion ultimaIteracion = iteraciones.get(iteraciones.size() - 1);
                double raiz = ultimaIteracion.getC();
                double error = ultimaIteracion.getErAct();
                double valorFuncion = evaluateFunction(raiz);

                // Añadir animación para resaltar la raíz
                Timeline rootAnimation = new Timeline(
                    new KeyFrame(Duration.ZERO, e -> {
                        // Añadir el punto raíz con estilo destacado
                        rootDataSet.add(raiz, valorFuncion);

                        // Actualizar la línea del punto medio para que coincida con la raíz
                        midpointLine.setStartX(chart.getXAxis().getDisplayPosition(raiz));
                        midpointLine.setEndX(chart.getXAxis().getDisplayPosition(raiz));

                        // Cambiar el color de la línea para destacar que es la raíz
                        midpointLine.setStroke(Color.GREEN);
                        midpointLine.setStrokeWidth(3);

                        // Actualizar el texto con el resultado final
                        lblFinalResult.setText(String.format("Raíz aproximada: %.6f | Error: %.6f | f(raíz): %.6f", 
                                                           raiz, error, valorFuncion));
                    }),
                    new KeyFrame(Duration.millis(500))
                );
                sequentialTransition.getChildren().add(rootAnimation);

                // Añadir una pausa antes de la animación final
                sequentialTransition.getChildren().add(new PauseTransition(Duration.millis(1000)));

                // Animación final para limpiar los elementos visuales excepto la raíz
                Timeline cleanupAnimation = new Timeline(
                    new KeyFrame(Duration.ZERO, e -> {
                        // Ocultar los rectángulos del intervalo pero mantener la línea de la raíz
                        leftInterval.setVisible(false);
                        rightInterval.setVisible(false);

                        // Añadir un efecto de destello a la línea de la raíz
                        FillTransition fillTransition = new FillTransition(
                            Duration.millis(500), 
                            midpointLine, 
                            Color.GREEN, 
                            Color.LIGHTGREEN
                        );
                        fillTransition.setCycleCount(3);
                        fillTransition.setAutoReverse(true);
                        fillTransition.play();
                    })
                );
                sequentialTransition.getChildren().add(cleanupAnimation);
            }

            // Reproducir la secuencia de animaciones
            sequentialTransition.play();

        } catch (Exception e) {
            mostrarError("Error en la animación: " + e.getMessage());
        }
    }

    // Método para mostrar mensajes de error
    private void mostrarError(String mensaje) {
        System.out.println(mensaje);
        lblFinalResult.setText("Error: " + mensaje);
        // Opcional: Mostrar un diálogo de alerta
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Método para evaluar la función ingresada por el usuario
    private double evaluateFunction(double x) {
        // Manejo especial para x=0 para evitar problemas de precisión
        if (Math.abs(x) < 1e-10) {
            x = 0.0; // Asegurar que es exactamente 0
        }
        return ConvertidorLatex.evaluate(userFunction, x);
    }

    // Método para establecer la función desde CtrlMenu (ajusta según tu lógica)
    public void setUserFunction(String function) {
        this.userFunction = function;
        String latexFunction = ConvertidorLatex.toLatex(function);
        System.out.println("Función traducida a LaTeX: " + latexFunction);

        // Actualizar la etiqueta con la función actual
        if (lblCurrentFunction != null) {
            lblCurrentFunction.setText(function);
        }

        // Actualizar el gráfico con la nueva función
        if (chart != null && functionDataSet != null) {
            graficarFuncion();
        }
    }

    /**
     * Actualiza los rectángulos que muestran el intervalo actual [a, b]
     * @param a Límite inferior del intervalo
     * @param b Límite superior del intervalo
     * @param minY Valor mínimo de y para los rectángulos
     * @param maxY Valor máximo de y para los rectángulos
     */
    private void updateIntervalRectangles(double a, double b, double minY, double maxY) {
        try {
            // Convertir coordenadas de datos a coordenadas de pantalla
            double aX = chart.getXAxis().getDisplayPosition(a);
            double bX = chart.getXAxis().getDisplayPosition(b);
            double minYPos = chart.getYAxis().getDisplayPosition(maxY); // Invertido porque en pantalla Y crece hacia abajo
            double maxYPos = chart.getYAxis().getDisplayPosition(minY);
            double height = maxYPos - minYPos;

            // Actualizar rectángulo izquierdo (desde el inicio del gráfico hasta a)
            double leftX = chart.getXAxis().getDisplayPosition(xAxis.getMin());
            double leftWidth = aX - leftX;
            leftInterval.setX(leftX);
            leftInterval.setY(minYPos);
            leftInterval.setWidth(leftWidth);
            leftInterval.setHeight(height);

            // Actualizar rectángulo derecho (desde b hasta el final del gráfico)
            double rightWidth = chart.getXAxis().getDisplayPosition(xAxis.getMax()) - bX;
            rightInterval.setX(bX);
            rightInterval.setY(minYPos);
            rightInterval.setWidth(rightWidth);
            rightInterval.setHeight(height);
        } catch (Exception e) {
            System.err.println("Error al actualizar los rectángulos del intervalo: " + e.getMessage());
        }
    }
}