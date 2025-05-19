package controllers;

import com.example.prueba.ConvertidorLatex;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.embed.swing.SwingNode;
import javafx.util.Duration;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import application.App;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class CtrlGrafica extends TopBarController {
    @FXML private TextField txtFunction;
    @FXML private SwingNode latexRender;
    @FXML private HBox symbolButtons;
    @FXML private LineChart<Number, Number> grafica;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private TextField txtXMin;
    @FXML private TextField txtXMax;
    @FXML private TextField txtYMin;
    @FXML private TextField txtYMax;
    @FXML private Button btnGraficar;
    @FXML private Button btnActualizarRango;
    @FXML private Slider zoomSlider;
    @FXML private Button btnAddFunction;
    @FXML private Button btnDeleteFunction;
    @FXML private Button btnEnlargeGraph;

    // Variable para almacenar la función seleccionada actualmente
    private int selectedFunctionIndex = -1;

    // Variables para el desplazamiento (panning)
    private double dragStartX;
    private double dragStartY;
    private boolean isDragging = false;

    private ExprEvaluator evaluator;
    private double xMin = -10;
    private double xMax = 10;
    private double yMin = -10;
    private double yMax = 10;
    private static final int POINTS = 1000; // Número de puntos para la gráfica

    // Lista para almacenar múltiples funciones
    private List<String> functions = new ArrayList<>();
    // Colores para las diferentes funciones
    private final String[] colors = {"#f3622d", "#fba71b", "#57b757", "#41a9c9", "#4258c9", "#9a42c8", "#c84164"};
    // Estado de la gráfica ampliada
    private boolean isEnlarged = false;
    // Tamaño original de la gráfica
    private double originalHeight;

    @FXML
    void initialize() {
        initializeTopBar();

        // Inicializar el evaluador de expresiones
        evaluator = new ExprEvaluator();

        // Configurar ejes
        configurarEjes();

        // Configurar el renderizador LaTeX
        setupLatexRender();

        // Configurar botones de símbolos
        setupSymbolButtons();

        // Guardar la altura original de la gráfica
        originalHeight = grafica.getPrefHeight();

        // Configurar el slider de zoom
        setupZoomSlider();

        // Configurar eventos para desplazamiento (panning)
        setupPanningEvents();

        // Cargar función si existe
        String function = App.app.getFunction();
        if (function != null && !function.isEmpty()) {
            txtFunction.setText(function);
            updateLatexRender(function);
        }

        // Listener para actualizar el renderizado LaTeX al escribir
        txtFunction.textProperty().addListener((observable, oldValue, newValue) -> {
            updateLatexRender(newValue);
        });
    }

    private void setupZoomSlider() {
        // Configurar el slider de zoom
        zoomSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double zoomFactor = newValue.doubleValue();
            applyZoom(zoomFactor);
        });
    }

    /**
     * Configura los eventos de ratón para permitir el desplazamiento (panning) en la gráfica
     */
    private void setupPanningEvents() {
        // Evento cuando se presiona el botón del ratón
        grafica.setOnMousePressed(event -> {
            // Solo iniciar el arrastre con el botón primario (izquierdo)
            if (event.isPrimaryButtonDown()) {
                dragStartX = event.getX();
                dragStartY = event.getY();
                isDragging = true;

                // Cambiar el cursor para indicar que se puede arrastrar
                grafica.setCursor(javafx.scene.Cursor.MOVE);
            }
        });

        // Evento cuando se mueve el ratón mientras está presionado
        grafica.setOnMouseDragged(event -> {
            if (isDragging) {
                double deltaX = event.getX() - dragStartX;
                double deltaY = event.getY() - dragStartY;

                // Convertir el desplazamiento en píxeles a unidades de la gráfica
                double rangeX = xAxis.getUpperBound() - xAxis.getLowerBound();
                double rangeY = yAxis.getUpperBound() - yAxis.getLowerBound();

                // Calcular el factor de escala (cuánto se mueve la gráfica por píxel)
                double scaleX = rangeX / grafica.getWidth();
                double scaleY = rangeY / grafica.getHeight();

                // Calcular el desplazamiento en unidades de la gráfica (negativo porque el arrastre es en dirección opuesta)
                double moveX = -deltaX * scaleX;
                double moveY = deltaY * scaleY;  // Invertido en Y porque el eje Y crece hacia arriba

                // Actualizar los límites de los ejes
                xAxis.setLowerBound(xAxis.getLowerBound() + moveX);
                xAxis.setUpperBound(xAxis.getUpperBound() + moveX);
                yAxis.setLowerBound(yAxis.getLowerBound() + moveY);
                yAxis.setUpperBound(yAxis.getUpperBound() + moveY);

                // Actualizar las variables globales
                xMin = xAxis.getLowerBound();
                xMax = xAxis.getUpperBound();
                yMin = yAxis.getLowerBound();
                yMax = yAxis.getUpperBound();

                // No actualizamos los campos de texto para mantener los valores originales
                // y permitir que el botón "Actualizar Rango" funcione correctamente

                // Actualizar el punto de inicio para el próximo movimiento
                dragStartX = event.getX();
                dragStartY = event.getY();
            }
        });

        // Evento cuando se suelta el botón del ratón
        grafica.setOnMouseReleased(event -> {
            if (isDragging) {
                isDragging = false;

                // Restaurar el cursor
                grafica.setCursor(javafx.scene.Cursor.DEFAULT);

                // No volvemos a graficar para evitar parpadeos al navegar
            }
        });
    }

    private void applyZoom(double zoomFactor) {
        // Calcular el centro actual de la vista
        double centerX = (xAxis.getLowerBound() + xAxis.getUpperBound()) / 2;
        double centerY = (yAxis.getLowerBound() + yAxis.getUpperBound()) / 2;

        // Calcular el rango actual
        double rangeX = xAxis.getUpperBound() - xAxis.getLowerBound();
        double rangeY = yAxis.getUpperBound() - yAxis.getLowerBound();

        // Calcular el nuevo rango basado en el factor de zoom
        // Invertimos la lógica para que al aumentar el slider, se haga zoom in
        double newRangeX = rangeX / zoomFactor;
        double newRangeY = rangeY / zoomFactor;

        // Calcular los nuevos límites manteniendo el centro
        double newXMin = centerX - (newRangeX / 2);
        double newXMax = centerX + (newRangeX / 2);
        double newYMin = centerY - (newRangeY / 2);
        double newYMax = centerY + (newRangeY / 2);

        // Actualizar los ejes
        xAxis.setLowerBound(newXMin);
        xAxis.setUpperBound(newXMax);
        yAxis.setLowerBound(newYMin);
        yAxis.setUpperBound(newYMax);

        // No actualizamos los campos de texto para mantener los valores originales
        // y permitir que el botón "Actualizar Rango" funcione correctamente

        // Actualizar las variables globales
        xMin = newXMin;
        xMax = newXMax;
        yMin = newYMin;
        yMax = newYMax;

        // No volvemos a graficar para evitar parpadeos al hacer zoom

        // Animación sutil de zoom (solo un pequeño efecto visual)
        FadeTransition fade = new FadeTransition(Duration.millis(100), grafica);
        fade.setFromValue(0.95);
        fade.setToValue(1.0);
        fade.play();
    }

    private void configurarEjes() {
        xAxis.setLabel("X");
        yAxis.setLabel("Y");
        xAxis.setAutoRanging(false);
        yAxis.setAutoRanging(false);
        xAxis.setLowerBound(xMin);
        xAxis.setUpperBound(xMax);
        yAxis.setLowerBound(yMin);
        yAxis.setUpperBound(yMax);
        xAxis.setTickUnit((xMax - xMin) / 20);
        yAxis.setTickUnit((yMax - yMin) / 20);

        // Leer valores iniciales de los campos de texto
        try {
            xMin = Double.parseDouble(txtXMin.getText());
            xMax = Double.parseDouble(txtXMax.getText());
            yMin = Double.parseDouble(txtYMin.getText());
            yMax = Double.parseDouble(txtYMax.getText());
            actualizarRango();
        } catch (NumberFormatException e) {
            // Usar valores predeterminados si hay error
            txtXMin.setText(String.valueOf(xMin));
            txtXMax.setText(String.valueOf(xMax));
            txtYMin.setText(String.valueOf(yMin));
            txtYMax.setText(String.valueOf(yMax));
        }
    }

    @FXML
    void graficarFuncion() {
        String function = txtFunction.getText().trim();
        if (function.isEmpty()) {
            mostrarErrorAnimacion(txtFunction);
            return;
        }

        // Validar que la función sea evaluable antes de graficarla
        if (!validarFuncion(function)) {
            mostrarErrorAnimacion(txtFunction);
            System.out.println("Error: La función ingresada no es válida. Verifique la sintaxis.");
            return;
        }

        // Guardar la función en la aplicación
        App.app.setFunction(function);

        // Limpiar gráfica anterior y lista de funciones
        grafica.getData().clear();
        functions.clear();

        // Añadir la función a la lista
        functions.add(function);

        // Graficar la función
        graficarFunciones();
    }

    /**
     * Valida que una función sea evaluable
     * @param function Función a validar
     * @return true si la función es válida, false en caso contrario
     */
    private boolean validarFuncion(String function) {
        try {
            // Intentar evaluar la función en algunos puntos de prueba
            double[] testPoints = {0, 1, -1, 0.5, -0.5};
            int validPoints = 0;

            for (double x : testPoints) {
                double y = evaluarFuncion(function, x);
                if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                    validPoints++;
                }
            }

            // Si al menos algunos puntos son válidos, consideramos la función como válida
            return validPoints > 0;
        } catch (Exception e) {
            System.out.println("Error validando la función: " + e.getMessage());
            return false;
        }
    }

    @FXML
    void addNewFunction() {
        String function = txtFunction.getText().trim();
        if (function.isEmpty()) {
            mostrarErrorAnimacion(txtFunction);
            return;
        }

        // Validar que la función sea evaluable antes de añadirla
        if (!validarFuncion(function)) {
            mostrarErrorAnimacion(txtFunction);
            System.out.println("Error: La función ingresada no es válida. Verifique la sintaxis.");
            return;
        }

        // Verificar si la función ya está en la lista
        if (functions.contains(function)) {
            // Mostrar animación de error
            mostrarErrorAnimacion(txtFunction);
            System.out.println("La función ya está graficada.");
            return;
        }

        // Añadir la función a la lista
        functions.add(function);

        // Graficar todas las funciones
        graficarFunciones();

        // Limpiar el campo de texto para la siguiente función
        txtFunction.clear();

        // Animación de éxito
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), btnAddFunction);
        scaleUp.setToX(1.2);
        scaleUp.setToY(1.2);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), btnAddFunction);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        scaleUp.setOnFinished(e -> scaleDown.play());
        scaleUp.play();
    }

    @FXML
    void enlargeGraph() {
        if (isEnlarged) {
            // Reducir la gráfica a su tamaño original
            grafica.setPrefHeight(originalHeight);
            btnEnlargeGraph.setText("Ampliar Gráfica");
        } else {
            // Ampliar la gráfica
            grafica.setPrefHeight(originalHeight * 1.5);
            btnEnlargeGraph.setText("Reducir Gráfica");
        }

        // Cambiar el estado
        isEnlarged = !isEnlarged;

        // Animación
        FadeTransition fade = new FadeTransition(Duration.millis(300), grafica);
        fade.setFromValue(0.7);
        fade.setToValue(1.0);
        fade.play();
    }

    private void graficarFunciones() {
        // Limpiar gráfica anterior
        grafica.getData().clear();

        // Lista para almacenar las funciones que no se pudieron graficar
        List<String> funcionesConError = new ArrayList<>();

        // Graficar cada función en la lista
        for (int i = 0; i < functions.size(); i++) {
            try {
                String function = functions.get(i);

                // Crear serie de datos
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName(function);

                // Calcular puntos
                List<XYChart.Data<Number, Number>> dataPoints = calcularPuntos(function);

                // Verificar si se obtuvieron puntos válidos
                if (dataPoints.isEmpty()) {
                    System.out.println("Advertencia: No se pudieron calcular puntos válidos para la función: " + function);
                    funcionesConError.add(function);
                    continue;
                }

                // Añadir puntos a la serie
                series.getData().addAll(dataPoints);

                // Añadir serie a la gráfica
                grafica.getData().add(series);

                // Aplicar color a la serie (si hay suficientes colores)
                if (i < colors.length) {
                    String color = colors[i];
                    applyColorToSeries(series, color);
                }

                // Hacer la serie seleccionable
                final int index = i;
                // Necesitamos esperar a que JavaFX cree el nodo para la serie
                Platform.runLater(() -> {
                    if (series.getNode() != null) {
                        // Añadir estilo para indicar que es seleccionable
                        series.getNode().getStyleClass().add("selectable-series");

                        // Añadir evento de clic para seleccionar la función
                        series.getNode().setOnMouseClicked(event -> {
                            // Detener la propagación del evento para evitar conflictos con el panning
                            event.consume();
                            selectFunction(index);

                            // Mostrar mensaje de confirmación visual
                            System.out.println("Función seleccionada: " + functions.get(index));
                        });
                    }

                    // También hacer seleccionables los puntos de datos individuales
                    for (XYChart.Data<Number, Number> data : series.getData()) {
                        if (data.getNode() != null) {
                            data.getNode().setOnMouseClicked(event -> {
                                event.consume();
                                selectFunction(index);
                            });
                        }
                    }
                });
            } catch (Exception e) {
                System.out.println("Error al graficar la función " + functions.get(i) + ": " + e.getMessage());
                funcionesConError.add(functions.get(i));
            }
        }

        // Eliminar las funciones que no se pudieron graficar de la lista
        if (!funcionesConError.isEmpty()) {
            for (String funcion : funcionesConError) {
                int index = functions.indexOf(funcion);
                if (index >= 0) {
                    functions.remove(index);
                    System.out.println("Se eliminó la función con errores: " + funcion);

                    // Ajustar el índice seleccionado si es necesario
                    if (selectedFunctionIndex >= index) {
                        selectedFunctionIndex = (selectedFunctionIndex > 0) ? selectedFunctionIndex - 1 : -1;
                    }
                }
            }

            // Mostrar mensaje de error si hubo problemas
            if (!functions.isEmpty()) {
                System.out.println("Algunas funciones no se pudieron graficar correctamente y fueron eliminadas.");
            } else {
                System.out.println("No se pudo graficar ninguna función correctamente.");
                mostrarErrorAnimacion(txtFunction);
            }
        }

        // Resaltar la función seleccionada si hay alguna
        if (selectedFunctionIndex >= 0 && selectedFunctionIndex < grafica.getData().size()) {
            highlightSelectedSeries(selectedFunctionIndex);
        }

        // Animación de entrada única (solo se ejecuta una vez al graficar)
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), grafica);
        fadeIn.setFromValue(0.3);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    /**
     * Selecciona una función de la gráfica
     * @param index Índice de la función a seleccionar
     */
    private void selectFunction(int index) {
        if (index >= 0 && index < functions.size()) {
            // Actualizar el índice seleccionado
            selectedFunctionIndex = index;

            // Mostrar la función seleccionada en el campo de texto
            txtFunction.setText(functions.get(index));

            // Resaltar la serie seleccionada
            highlightSelectedSeries(index);
        }
    }

    /**
     * Resalta visualmente la serie seleccionada
     * @param index Índice de la serie a resaltar
     */
    private void highlightSelectedSeries(int index) {
        // Restaurar todas las series a su estado normal
        Platform.runLater(() -> {
            for (int i = 0; i < grafica.getData().size(); i++) {
                XYChart.Series<Number, Number> series = grafica.getData().get(i);
                String color = (i < colors.length) ? colors[i] : "#000000";

                // Aplicar estilo normal si el nodo existe
                if (series.getNode() != null) {
                    series.getNode().setStyle("-fx-stroke-width: 1.5px; -fx-stroke-dash-array: none;");

                    // Quitar cualquier clase de estilo de selección
                    series.getNode().getStyleClass().remove("selected-series");
                }

                // Aplicar color normal a los puntos
                applyColorToSeries(series, color);

                // Restaurar el tamaño normal de los puntos
                for (XYChart.Data<Number, Number> data : series.getData()) {
                    if (data.getNode() != null) {
                        data.getNode().setStyle("-fx-background-radius: 3px; -fx-padding: 3px; -fx-background-color: " + color + ";");
                    }
                }
            }

            // Resaltar la serie seleccionada
            if (index >= 0 && index < grafica.getData().size()) {
                XYChart.Series<Number, Number> selectedSeries = grafica.getData().get(index);

                // Aplicar estilo resaltado (línea más gruesa y con efecto) si el nodo existe
                if (selectedSeries.getNode() != null) {
                    // Añadir clase de estilo para la selección
                    selectedSeries.getNode().getStyleClass().add("selected-series");

                    // Aplicar estilo directo para mayor visibilidad
                    String color = (index < colors.length) ? colors[index] : "#000000";
                    selectedSeries.getNode().setStyle(
                        "-fx-stroke-width: 3.5px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 10, 0.5, 0, 0);"
                    );

                    // Mostrar mensaje informativo
                    System.out.println("Función '" + functions.get(index) + "' seleccionada. Puede eliminarla con el botón 'Eliminar Función'.");

                    // Resaltar también los puntos de datos
                    for (XYChart.Data<Number, Number> data : selectedSeries.getData()) {
                        if (data.getNode() != null) {
                            data.getNode().setStyle(
                                "-fx-background-radius: 5px; " +
                                "-fx-padding: 5px; " +
                                "-fx-background-color: " + color + "; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 5, 0.5, 0, 0);"
                            );
                        }
                    }
                }
            }
        });
    }

    /**
     * Elimina la función seleccionada actualmente
     */
    @FXML
    void deleteSelectedFunction() {
        if (selectedFunctionIndex >= 0 && selectedFunctionIndex < functions.size()) {
            // Guardar el nombre de la función para mostrar mensaje
            String functionName = functions.get(selectedFunctionIndex);

            // Eliminar la función de la lista
            functions.remove(selectedFunctionIndex);

            // Resetear el índice seleccionado
            selectedFunctionIndex = -1;

            // Limpiar el campo de texto
            txtFunction.clear();

            // Volver a graficar las funciones restantes
            graficarFunciones();

            // Mostrar mensaje de confirmación
            System.out.println("Función '" + functionName + "' eliminada correctamente.");

            // Animación de éxito
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), btnDeleteFunction);
            scaleUp.setToX(1.2);
            scaleUp.setToY(1.2);

            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), btnDeleteFunction);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);

            // Añadir efecto de fade para mayor feedback visual
            FadeTransition fadeOut = new FadeTransition(Duration.millis(100), btnDeleteFunction);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.7);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(100), btnDeleteFunction);
            fadeIn.setFromValue(0.7);
            fadeIn.setToValue(1.0);

            // Encadenar las animaciones
            scaleUp.setOnFinished(e -> {
                fadeOut.play();
                fadeOut.setOnFinished(f -> {
                    fadeIn.play();
                    fadeIn.setOnFinished(g -> scaleDown.play());
                });
            });

            scaleUp.play();
        } else {
            // Mostrar error si no hay función seleccionada
            mostrarErrorAnimacion(btnDeleteFunction);

            // Mostrar mensaje de ayuda
            System.out.println("Error: No hay función seleccionada. Primero seleccione una función haciendo clic en ella en la gráfica.");

            // Añadir un efecto visual para indicar que debe seleccionar una función
            Timeline pulseAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, evt -> grafica.setOpacity(1.0)),
                new KeyFrame(Duration.millis(200), evt -> grafica.setOpacity(0.8)),
                new KeyFrame(Duration.millis(400), evt -> grafica.setOpacity(1.0))
            );
            pulseAnimation.setCycleCount(2);
            pulseAnimation.play();
        }
    }

    private void applyColorToSeries(XYChart.Series<Number, Number> series, String color) {
        // Aplicar color a todos los nodos de la serie
        Platform.runLater(() -> {
            for (XYChart.Data<Number, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-background-color: " + color + ";");
                }
            }
        });
    }

    private List<XYChart.Data<Number, Number>> calcularPuntos(String function) {
        List<XYChart.Data<Number, Number>> dataPoints = new ArrayList<>();
        double step = (xMax - xMin) / POINTS;

        // Variables para detectar discontinuidades
        Double lastY = null;
        Double lastX = null;
        double maxJump = (yMax - yMin) / 10; // Umbral para detectar saltos grandes

        for (double x = xMin; x <= xMax; x += step) {
            try {
                // Evaluar la función en x
                double y = evaluarFuncion(function, x);

                // Verificar si y es un valor válido (no NaN o infinito)
                if (Double.isNaN(y) || Double.isInfinite(y)) {
                    // Marcar discontinuidad
                    lastY = null;
                    lastX = null;
                    continue;
                }

                // Verificar si y está dentro del rango visible
                if (y >= yMin && y <= yMax) {
                    // Verificar si hay un salto grande (discontinuidad)
                    if (lastY != null && lastX != null) {
                        double jump = Math.abs(y - lastY);
                        if (jump > maxJump) {
                            // Hay un salto grande, añadir un punto nulo para romper la línea
                            dataPoints.add(new XYChart.Data<>(null, null));
                        }
                    }

                    // Añadir el punto actual
                    dataPoints.add(new XYChart.Data<>(x, y));

                    // Actualizar último punto válido
                    lastY = y;
                    lastX = x;
                } else {
                    // Punto fuera del rango visible, marcar discontinuidad
                    lastY = null;
                    lastX = null;
                }
            } catch (Exception e) {
                // Ignorar puntos que causan error (como divisiones por cero)
                // Marcar discontinuidad
                lastY = null;
                lastX = null;
                continue;
            }
        }

        return dataPoints;
    }

    private double evaluarFuncion(String function, double x) {
        try {
            // Validar que x no cause problemas en la evaluación
            if (Double.isNaN(x) || Double.isInfinite(x)) {
                return Double.NaN;
            }

            // Reemplazar la variable x con el valor actual, asegurando que se use paréntesis
            // para evitar problemas con expresiones como x^2 o -x
            String expr = function.replaceAll("(?<![a-zA-Z0-9_])x(?![a-zA-Z0-9_])", "(" + x + ")");

            // Evaluar la expresión
            IExpr result = evaluator.eval(expr);

            // Verificar si el resultado es un número válido
            if (result == null || !result.isNumeric()) {
                return Double.NaN;
            }

            // Convertir el resultado a un número
            double value = Double.parseDouble(result.toString());

            // Verificar si el resultado es un valor válido
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                return Double.NaN;
            }

            return value;
        } catch (Exception e) {
            // En lugar de lanzar una excepción, devolver NaN para indicar un punto no válido
            System.out.println("Error evaluando la función en x=" + x + ": " + e.getMessage());
            return Double.NaN;
        }
    }

    @FXML
    void actualizarRango() {
        try {
            // Leer valores de los campos de texto
            xMin = Double.parseDouble(txtXMin.getText());
            xMax = Double.parseDouble(txtXMax.getText());
            yMin = Double.parseDouble(txtYMin.getText());
            yMax = Double.parseDouble(txtYMax.getText());

            // Validar rangos
            if (xMin >= xMax || yMin >= yMax) {
                throw new IllegalArgumentException("Los valores mínimos deben ser menores que los máximos");
            }

            // Aplicar una breve animación de desvanecimiento antes de actualizar
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), grafica);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.7);

            fadeOut.setOnFinished(e -> {
                // Actualizar ejes
                xAxis.setLowerBound(xMin);
                xAxis.setUpperBound(xMax);
                yAxis.setLowerBound(yMin);
                yAxis.setUpperBound(yMax);
                xAxis.setTickUnit((xMax - xMin) / 20);
                yAxis.setTickUnit((yMax - yMin) / 20);

                // Volver a graficar si hay funciones
                if (!functions.isEmpty()) {
                    graficarFunciones();
                } else {
                    // Si no hay funciones, solo aplicar la animación de entrada
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), grafica);
                    fadeIn.setFromValue(0.7);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                }
            });

            fadeOut.play();

        } catch (NumberFormatException e) {
            System.out.println("Error: Ingrese valores numéricos válidos");
            mostrarErrorAnimacion(txtXMin);
            mostrarErrorAnimacion(txtXMax);
            mostrarErrorAnimacion(txtYMin);
            mostrarErrorAnimacion(txtYMax);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            mostrarErrorAnimacion(txtXMin);
            mostrarErrorAnimacion(txtXMax);
            mostrarErrorAnimacion(txtYMin);
            mostrarErrorAnimacion(txtYMax);
        }
    }

    private void mostrarErrorAnimacion(TextField textField) {
        // Animación de aviso de error para TextField
        Timeline shakingAnimation = new Timeline(
            new KeyFrame(Duration.millis(0), evt -> textField.setTranslateX(0)),
            new KeyFrame(Duration.millis(50), evt -> textField.setTranslateX(5)),
            new KeyFrame(Duration.millis(100), evt -> textField.setTranslateX(-5)),
            new KeyFrame(Duration.millis(150), evt -> textField.setTranslateX(5)),
            new KeyFrame(Duration.millis(200), evt -> textField.setTranslateX(-5)),
            new KeyFrame(Duration.millis(250), evt -> textField.setTranslateX(0))
        );
        shakingAnimation.play();
    }

    private void mostrarErrorAnimacion(Button button) {
        // Animación de aviso de error para Button
        Timeline shakingAnimation = new Timeline(
            new KeyFrame(Duration.millis(0), evt -> button.setTranslateX(0)),
            new KeyFrame(Duration.millis(50), evt -> button.setTranslateX(5)),
            new KeyFrame(Duration.millis(100), evt -> button.setTranslateX(-5)),
            new KeyFrame(Duration.millis(150), evt -> button.setTranslateX(5)),
            new KeyFrame(Duration.millis(200), evt -> button.setTranslateX(-5)),
            new KeyFrame(Duration.millis(250), evt -> button.setTranslateX(0))
        );
        shakingAnimation.play();
    }

    private void setupLatexRender() {
        SwingUtilities.invokeLater(() -> {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(Color.WHITE);
            panel.setPreferredSize(new Dimension(600, 60));
            panel.setFocusable(false);
            latexRender.setContent(panel);
            latexRender.setDisable(false);
            updateLatexRender("");

            // Animación de entrada
            Platform.runLater(() -> {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(800), latexRender.getParent());
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.setDelay(Duration.millis(200));
                fadeIn.play();
            });
        });
    }

    private void updateLatexRender(String latex) {
        SwingUtilities.invokeLater(() -> {
            JPanel panel = (JPanel) latexRender.getContent();
            panel.removeAll();
            if (latex != null && !latex.trim().isEmpty()) {
                try {
                    String latexFunction = ConvertidorLatex.toLatex(latex);
                    if (latexFunction.startsWith("Esperando...")) {
                        panel.add(new JLabel(latexFunction), BorderLayout.CENTER);
                    } else {
                        TeXFormula formula = new TeXFormula(latexFunction);
                        TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20);
                        icon.setForeground(Color.BLACK);
                        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g2 = image.createGraphics();
                        icon.paintIcon(null, g2, 0, 0);
                        g2.dispose();
                        JLabel label = new JLabel(new ImageIcon(image));
                        panel.add(label, BorderLayout.CENTER);
                    }
                } catch (Exception e) {
                    panel.add(new JLabel("Esperando..."), BorderLayout.CENTER);
                }
            } else {
                panel.add(new JLabel("Esperando..."), BorderLayout.CENTER);
            }
            panel.revalidate();
            panel.repaint();
        });
    }

    private void setupSymbolButtons() {
        String[] symbols = {"√", "π", "sin", "cos", "tan", "e", "^", "(", ")", "+", "-", "*", "/"};
        for (int i = 0; i < symbols.length; i++) {
            String symbol = symbols[i];
            Button button = new Button(symbol);
            button.setPrefSize(40, 40);
            button.getStyleClass().add("symbol-button");
            button.setFocusTraversable(false);

            // Animación de entrada
            final int index = i;
            Platform.runLater(() -> {
                button.setTranslateY(50);
                button.setOpacity(0);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(600), button);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                ParallelTransition parallelTransition = new ParallelTransition(fadeIn);
                parallelTransition.setDelay(Duration.millis(100 + (index * 60)));
                parallelTransition.play();
            });

            button.setOnAction(event -> {
                // Animación de clic
                ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button);
                scaleDown.setToX(0.8);
                scaleDown.setToY(0.8);

                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button);
                scaleUp.setToX(1.0);
                scaleUp.setToY(1.0);

                scaleDown.setOnFinished(e -> {
                    String currentText = txtFunction.getText();
                    txtFunction.setText(currentText + symbol);
                    updateLatexRender(currentText + symbol);
                    scaleUp.play();

                    Platform.runLater(() -> {
                        txtFunction.requestFocus();
                        txtFunction.positionCaret(txtFunction.getText().length());
                    });
                });

                scaleDown.play();
            });

            symbolButtons.getChildren().add(button);
        }
    }
}
