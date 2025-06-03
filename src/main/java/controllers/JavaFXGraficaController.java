package controllers;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.plugins.DataPointTooltip;
import de.gsi.chart.plugins.EditAxis;
import de.gsi.chart.plugins.Panner;
import de.gsi.chart.plugins.ParameterMeasurements;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.datareduction.DefaultDataReducer;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.spi.DoubleDataSet;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.embed.swing.SwingNode;
import org.matheclipse.core.eval.ExprEvaluator;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import com.example.prueba.ConvertidorLatex;
import model.DerivativeCalculator;
import model.IntegralCalculator;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaFXGraficaController extends TopBarController {

    // Mantener las declaraciones FXML existentes
    @FXML private TextField txtFunction;
    @FXML private SwingNode latexRender;
    @FXML private HBox symbolButtons;
    @FXML private StackPane chartContainer;
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
    @FXML private CheckBox chkShowGrid;
    @FXML private ToggleButton btnThemeToggle;
    @FXML private HBox functionInfoPanel;
    @FXML private Label lblSelectedFunction;
    @FXML private Button btnExportGraph;
    @FXML private Button btnGraficarNav;
    @FXML private Label lblCoordinates;
    @FXML private CheckBox chkInfiniteRange;
    @FXML private ListView<String> lstFunctions;
    @FXML private Button btnFindIntersection;
    @FXML private Button btnCalculateDerivative;
    @FXML private Button btnCalculateIntegral;
    @FXML private Button btnShowTable;
    @FXML private Button btnAddPoint;
    @FXML private Button btnSettings;

    // Sidebar components
    @FXML private VBox sidebar;
    @FXML private Button btnToggleSidebar;
    @FXML private VBox mainContent;

    // Sidebar state
    private boolean sidebarCollapsed = false;

    // Componentes Chart-FX
    private XYChart chart;
    private DefaultNumericAxis xAxis;
    private DefaultNumericAxis yAxis;

    // Variables para funciones
    private List<String> functions = new ArrayList<>();
    private int selectedFunctionIndex = -1;
    private Map<String, DoubleDataSet> dataSets = new HashMap<>();

    // Variables para rango y zoom
    private double xMin = -10;
    private double xMax = 10;
    private double yMin = -10;
    private double yMax = 10;
    private Double savedXMin = null;
    private Double savedXMax = null;
    private Double savedYMin = null;
    private Double savedYMax = null;
    private static final int POINTS = 1000;

    // Evaluación matemática
    private ExprEvaluator evaluator;
    private Map<String, Double> evaluationCache = new HashMap<>();

    // Número de puntos para graficar funciones
    private int numPoints = 500;

    // Colores para funciones
    private final String[] COLORS = {
        "#f3622d", "#fba71b", "#57b757", "#41a9c9", "#4258c9",
        "#9a42c8", "#c84164", "#888888", "#e45756", "#54a24b",
        "#eeca3b", "#b279a2", "#ff9da6", "#9d755d", "#bab0ac"
    };

    @FXML
    public void initialize() {
        // Inicializar evaluador de expresiones con constantes y funciones adicionales
        evaluator = new ExprEvaluator();

        // Definir constantes matemáticas adicionales
        evaluator.defineVariable("pi", Math.PI);
        evaluator.defineVariable("e", Math.E);
        evaluator.defineVariable("phi", (1 + Math.sqrt(5)) / 2); // Número áureo
        evaluator.defineVariable("gamma", 0.57721566490153286060); // Constante de Euler-Mascheroni

        // Definir funciones matemáticas adicionales
        // Nota: La versión actual de Symja no soporta defineFunction
        // Estas funciones se manejarán manualmente en el método evaluarFuncion
        // evaluator.defineFunction("sec", "1/cos(#1)");
        // evaluator.defineFunction("csc", "1/sin(#1)");
        // evaluator.defineFunction("cot", "1/tan(#1)");
        // evaluator.defineFunction("sinc", "If[#1==0, 1, sin(#1)/#1]");
        // evaluator.defineFunction("sign", "If[#1>0, 1, If[#1<0, -1, 0]]");
        // evaluator.defineFunction("step", "If[#1>=0, 1, 0]");

        // Configurar Chart-FX
        setupChartFX();

        // Configurar elementos UI
        setupUIElements();

        // Configurar manejadores de eventos
        setupEventHandlers();

        // Configurar renderizador LaTeX
        setupLatexRender();

        // Configurar botones de símbolos
        setupSymbolButtons();

        // Configurar atajos de teclado
        setupKeyboardShortcuts();

        // Configurar sidebar
        setupSidebar();

        // Mostrar mensaje de bienvenida con consejos
        showWelcomeMessage();
    }

    /**
     * Configura atajos de teclado para operaciones comunes
     */
    private void setupKeyboardShortcuts() {
        // Crear mapa de atajos de teclado
        javafx.scene.input.KeyCombination.Modifier controlModifier = 
            javafx.scene.input.KeyCombination.CONTROL_DOWN;
        javafx.scene.input.KeyCombination.Modifier shiftModifier = 
            javafx.scene.input.KeyCombination.SHIFT_DOWN;

        // Mapa de atajos y sus descripciones
        Map<javafx.scene.input.KeyCombination, Runnable> shortcuts = new HashMap<>();
        Map<javafx.scene.input.KeyCombination, String> shortcutDescriptions = new HashMap<>();

        // Graficar (Ctrl+G)
        javafx.scene.input.KeyCombination graphKeyComb = 
            new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.G, controlModifier);
        shortcuts.put(graphKeyComb, this::graficarFuncion);
        shortcutDescriptions.put(graphKeyComb, "Graficar función");

        // Añadir función (Ctrl+A)
        javafx.scene.input.KeyCombination addKeyComb = 
            new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.A, controlModifier);
        shortcuts.put(addKeyComb, this::addNewFunction);
        shortcutDescriptions.put(addKeyComb, "Añadir función");

        // Eliminar función (Ctrl+D)
        javafx.scene.input.KeyCombination deleteKeyComb = 
            new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.D, controlModifier);
        shortcuts.put(deleteKeyComb, this::deleteSelectedFunction);
        shortcutDescriptions.put(deleteKeyComb, "Eliminar función seleccionada");

        // Calcular derivada (Ctrl+Shift+D)
        javafx.scene.input.KeyCombination derivativeKeyComb = 
            new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.D, controlModifier, shiftModifier);
        shortcuts.put(derivativeKeyComb, this::calculateDerivative);
        shortcutDescriptions.put(derivativeKeyComb, "Calcular derivada");

        // Calcular integral (Ctrl+Shift+I)
        javafx.scene.input.KeyCombination integralKeyComb = 
            new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.I, controlModifier, shiftModifier);
        shortcuts.put(integralKeyComb, this::calculateIntegral);
        shortcutDescriptions.put(integralKeyComb, "Calcular integral");

        // Analizar función (Ctrl+Shift+A)
        javafx.scene.input.KeyCombination analyzeKeyComb = 
            new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.A, controlModifier, shiftModifier);
        shortcuts.put(analyzeKeyComb, this::analyzeFunction);
        shortcutDescriptions.put(analyzeKeyComb, "Analizar función");

        // Exportar gráfica (Ctrl+E)
        javafx.scene.input.KeyCombination exportKeyComb = 
            new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.E, controlModifier);
        shortcuts.put(exportKeyComb, this::exportGraph);
        shortcutDescriptions.put(exportKeyComb, "Exportar gráfica");

        // Mostrar/ocultar sidebar (Ctrl+B)
        javafx.scene.input.KeyCombination sidebarKeyComb = 
            new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.B, controlModifier);
        shortcuts.put(sidebarKeyComb, this::toggleSidebar);
        shortcutDescriptions.put(sidebarKeyComb, "Mostrar/ocultar panel lateral");

        // Mostrar tabla (Ctrl+T)
        javafx.scene.input.KeyCombination tableKeyComb = 
            new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.T, controlModifier);
        shortcuts.put(tableKeyComb, this::showTable);
        shortcutDescriptions.put(tableKeyComb, "Mostrar tabla de valores");

        // Mostrar ayuda (F1)
        javafx.scene.input.KeyCombination helpKeyComb = 
            new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.F1);
        shortcuts.put(helpKeyComb, this::showShortcutsHelp);
        shortcutDescriptions.put(helpKeyComb, "Mostrar ayuda");

        // Registrar manejador de eventos de teclado
        if (chart != null && chart.getScene() != null) {
            chart.getScene().setOnKeyPressed(event -> {
                for (javafx.scene.input.KeyCombination keyComb : shortcuts.keySet()) {
                    if (keyComb.match(event)) {
                        shortcuts.get(keyComb).run();
                        event.consume();
                        break;
                    }
                }
            });
        } else {
            // Si el chart aún no está en la escena, programar para más tarde
            javafx.application.Platform.runLater(() -> {
                if (chart != null && chart.getScene() != null) {
                    chart.getScene().setOnKeyPressed(event -> {
                        for (javafx.scene.input.KeyCombination keyComb : shortcuts.keySet()) {
                            if (keyComb.match(event)) {
                                shortcuts.get(keyComb).run();
                                event.consume();
                                break;
                            }
                        }
                    });
                }
            });
        }

        // Guardar descripciones para mostrar en la ayuda
        this.keyboardShortcuts = shortcutDescriptions;
    }

    // Variable para almacenar descripciones de atajos de teclado
    private Map<javafx.scene.input.KeyCombination, String> keyboardShortcuts;

    /**
     * Muestra un diálogo con los atajos de teclado disponibles
     */
    private void showShortcutsHelp() {
        // Crear diálogo
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Atajos de Teclado");
        dialog.setHeaderText("Atajos de teclado disponibles");

        // Botón de cerrar
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);

        // Crear tabla de atajos
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.setPadding(new javafx.geometry.Insets(20));

        // Crear tabla
        javafx.scene.control.TableView<javafx.util.Pair<String, String>> table = 
            new javafx.scene.control.TableView<>();

        // Columnas
        javafx.scene.control.TableColumn<javafx.util.Pair<String, String>, String> shortcutColumn = 
            new javafx.scene.control.TableColumn<>("Atajo");
        shortcutColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getKey()));

        javafx.scene.control.TableColumn<javafx.util.Pair<String, String>, String> descriptionColumn = 
            new javafx.scene.control.TableColumn<>("Descripción");
        descriptionColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getValue()));

        // Configurar columnas
        shortcutColumn.setPrefWidth(150);
        descriptionColumn.setPrefWidth(250);

        table.getColumns().add(shortcutColumn);
        table.getColumns().add(descriptionColumn);

        // Añadir datos
        javafx.collections.ObservableList<javafx.util.Pair<String, String>> data = 
            javafx.collections.FXCollections.observableArrayList();

        for (javafx.scene.input.KeyCombination keyComb : keyboardShortcuts.keySet()) {
            data.add(new javafx.util.Pair<>(keyComb.getDisplayText(), keyboardShortcuts.get(keyComb)));
        }

        table.setItems(data);

        // Añadir tabla al contenido
        content.getChildren().add(table);

        // Configurar diálogo
        dialog.getDialogPane().setContent(content);

        // Mostrar diálogo
        dialog.showAndWait();
    }

    /**
     * Muestra un mensaje de bienvenida con consejos útiles
     */
    private void showWelcomeMessage() {
        // Programar la visualización del diálogo para después de que se complete la inicialización de JavaFX
        Platform.runLater(() -> {
            // Crear diálogo
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Bienvenido a la Graficadora Mejorada");
            alert.setHeaderText("Graficadora de Funciones Matemáticas");

            // Mensaje con consejos
            String message = "¡Bienvenido a la graficadora mejorada!\n\n" +
                "Nuevas características:\n" +
                "• Derivadas de orden superior y detección de puntos críticos\n" +
                "• Integrales definidas con visualización de área\n" +
                "• Análisis completo de funciones (dominio, simetría, puntos críticos)\n" +
                "• Personalización de colores y estilos de línea\n" +
                "• Atajos de teclado para operaciones comunes\n\n" +
                "Presiona F1 en cualquier momento para ver los atajos de teclado disponibles.";

            alert.setContentText(message);

            // Mostrar diálogo
            alert.showAndWait();
        });
    }

    /**
     * Configura el panel lateral
     */
    private void setupSidebar() {
        // Asegurar que el sidebar comienza expandido
        sidebarCollapsed = false;

        if (btnToggleSidebar != null) {
            btnToggleSidebar.setText("<");
        }

        // Hacer que el panel de funciones sea visible
        if (functionInfoPanel != null) {
            functionInfoPanel.setVisible(true);
        }

        // Aplicar estilos iniciales
        if (sidebar != null) {
            sidebar.getStyleClass().remove("sidebar-collapsed");
        }

        if (mainContent != null) {
            mainContent.getStyleClass().remove("expanded-content");
        }
    }

    private void setupChartFX() {
        // Usar Platform.runLater para asegurar que la inicialización se realice en el hilo de JavaFX
        // después de que la escena esté completamente cargada
        Platform.runLater(() -> {
            try {
                System.out.println("Iniciando configuración del gráfico...");

                // Crear ejes con etiquetas y rangos iniciales que incluyan valores negativos
                xAxis = new DefaultNumericAxis(xMin, xMax, 1);
                yAxis = new DefaultNumericAxis(yMin, yMax, 1);
                xAxis.setName("x");
                yAxis.setName("y");

                // Configurar opciones de ejes
                xAxis.setAnimated(false);
                yAxis.setAnimated(false);
                xAxis.setAutoRangeRounding(true);
                yAxis.setAutoRangeRounding(true);

                // Configurar ejes para que se crucen en el origen (estilo cruz)
                xAxis.setForceZeroInRange(true);
                yAxis.setForceZeroInRange(true);

                // Crear gráfico XY
                chart = new XYChart(xAxis, yAxis);

                // Configurar el estilo de la cuadrícula para que se vea como una cruz
                chart.getGridRenderer().setDrawOnTop(true);
                chart.setAnimated(false);

                // Añadir renderer optimizado para conjuntos de datos grandes
                ErrorDataSetRenderer renderer = new ErrorDataSetRenderer();
                renderer.setDrawMarker(false); // No dibujar marcadores por defecto
                renderer.setErrorType(ErrorStyle.NONE); // Sin barras de error

                // Configurar reducción de datos para mejor rendimiento
                DefaultDataReducer dataReducer = new DefaultDataReducer();
                dataReducer.setMinPointPixelDistance(1); // Cambiado a int

                chart.getRenderers().add(renderer);

                // Añadir plugins interactivos con configuración básica
                // Plugins para interactividad mejorada
                chart.getPlugins().add(new Zoomer()); // Zoom
                chart.getPlugins().add(new Panner()); // Panning/desplazamiento
                chart.getPlugins().add(new EditAxis()); // Edición de ejes
                chart.getPlugins().add(new DataPointTooltip()); // Tooltips en puntos
                chart.getPlugins().add(new ParameterMeasurements()); // Mediciones

                // Añadir gráfico al contenedor
                if (chartContainer != null) {
                    chartContainer.getChildren().clear(); // Limpiar cualquier contenido previo
                    chartContainer.getChildren().add(chart);
                    System.out.println("Gráfico añadido al contenedor correctamente");
                } else {
                    System.err.println("Error: chartContainer es null");
                }
            } catch (Exception e) {
                System.err.println("Error al configurar el gráfico: " + e.getMessage());
                e.printStackTrace();
                // Mostrar un mensaje de error en la UI
                if (chartContainer != null) {
                    Label errorLabel = new Label("Error al cargar el gráfico: " + e.getMessage());
                    errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    chartContainer.getChildren().clear();
                    chartContainer.getChildren().add(errorLabel);
                }
            }
        });
    }

    private void setupUIElements() {
        // Usar Platform.runLater para asegurar que la inicialización se realice en el hilo de JavaFX
        // después de que la escena esté completamente cargada
        Platform.runLater(() -> {
            try {
                System.out.println("Iniciando configuración de elementos UI...");

                // Inicializar campos de texto con valores predeterminados
                if (txtXMin != null) {
                    txtXMin.setText(String.valueOf(xMin));
                    System.out.println("txtXMin inicializado: " + xMin);
                } else {
                    System.err.println("Error: txtXMin es null");
                }

                if (txtXMax != null) {
                    txtXMax.setText(String.valueOf(xMax));
                    System.out.println("txtXMax inicializado: " + xMax);
                } else {
                    System.err.println("Error: txtXMax es null");
                }

                if (txtYMin != null) {
                    txtYMin.setText(String.valueOf(yMin));
                    System.out.println("txtYMin inicializado: " + yMin);
                } else {
                    System.err.println("Error: txtYMin es null");
                }

                if (txtYMax != null) {
                    txtYMax.setText(String.valueOf(yMax));
                    System.out.println("txtYMax inicializado: " + yMax);
                } else {
                    System.err.println("Error: txtYMax es null");
                }

                // Configurar TextField para función
                if (txtFunction != null) {
                    // Asegurar que el campo de texto pueda recibir foco
                    txtFunction.setFocusTraversable(true);
                    txtFunction.setEditable(true);
                    txtFunction.setDisable(false);
                    System.out.println("txtFunction configurado correctamente");
                } else {
                    System.err.println("Error: txtFunction es null");
                }

                // Configurar ListView para funciones
                if (lstFunctions != null) {
                    lstFunctions.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
                        if (newIdx != null && newIdx.intValue() >= 0) {
                            selectFunction(newIdx.intValue());
                        }
                    });
                    System.out.println("lstFunctions configurado correctamente");
                } else {
                    System.err.println("Error: lstFunctions es null");
                }

                // Configurar slider de zoom
                if (zoomSlider != null) {
                    zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            applyZoom(newVal.doubleValue());
                        }
                    });
                    System.out.println("zoomSlider configurado correctamente");
                } else {
                    System.err.println("Error: zoomSlider es null");
                }

                // Configurar checkbox de cuadrícula
                if (chkShowGrid != null) {
                    chkShowGrid.selectedProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            toggleGridLines(newVal);
                        }
                    });
                    System.out.println("chkShowGrid configurado correctamente");
                } else {
                    System.err.println("Error: chkShowGrid es null");
                }

                // Configurar toggle de tema
                if (btnThemeToggle != null) {
                    btnThemeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            toggleTheme(newVal);
                        }
                    });
                    System.out.println("btnThemeToggle configurado correctamente");
                } else {
                    System.err.println("Error: btnThemeToggle es null");
                }

                System.out.println("Configuración de elementos UI completada");
            } catch (Exception e) {
                System.err.println("Error al configurar elementos UI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void setupEventHandlers() {
        // Usar Platform.runLater para asegurar que la inicialización se realice en el hilo de JavaFX
        // después de que la escena esté completamente cargada
        Platform.runLater(() -> {
            try {
                System.out.println("Iniciando configuración de manejadores de eventos...");

                // Configurar eventos para botones con verificación de nulos
                if (btnGraficar != null) {
                    btnGraficar.setOnAction(event -> {
                        try {
                            graficarFuncion();
                        } catch (Exception e) {
                            System.err.println("Error al graficar función: " + e.getMessage());
                            e.printStackTrace();
                            showNotification("Error al graficar: " + e.getMessage());
                        }
                    });
                    System.out.println("btnGraficar configurado correctamente");
                } else {
                    System.err.println("Error: btnGraficar es null");
                }

                if (btnActualizarRango != null) {
                    btnActualizarRango.setOnAction(event -> {
                        try {
                            actualizarRango();
                        } catch (Exception e) {
                            System.err.println("Error al actualizar rango: " + e.getMessage());
                            e.printStackTrace();
                            showNotification("Error al actualizar rango: " + e.getMessage());
                        }
                    });
                    System.out.println("btnActualizarRango configurado correctamente");
                } else {
                    System.err.println("Error: btnActualizarRango es null");
                }

                if (btnAddFunction != null) {
                    btnAddFunction.setOnAction(event -> {
                        try {
                            addNewFunction();
                        } catch (Exception e) {
                            System.err.println("Error al añadir función: " + e.getMessage());
                            e.printStackTrace();
                            showNotification("Error al añadir función: " + e.getMessage());
                        }
                    });
                    System.out.println("btnAddFunction configurado correctamente");
                } else {
                    System.err.println("Error: btnAddFunction es null");
                }

                if (btnDeleteFunction != null) {
                    btnDeleteFunction.setOnAction(event -> {
                        try {
                            deleteSelectedFunction();
                        } catch (Exception e) {
                            System.err.println("Error al eliminar función: " + e.getMessage());
                            e.printStackTrace();
                            showNotification("Error al eliminar función: " + e.getMessage());
                        }
                    });
                    System.out.println("btnDeleteFunction configurado correctamente");
                } else {
                    System.err.println("Error: btnDeleteFunction es null");
                }

                if (btnEnlargeGraph != null) {
                    btnEnlargeGraph.setOnAction(event -> {
                        try {
                            enlargeGraph();
                        } catch (Exception e) {
                            System.err.println("Error al ampliar gráfico: " + e.getMessage());
                            e.printStackTrace();
                            showNotification("Error al ampliar gráfico: " + e.getMessage());
                        }
                    });
                    System.out.println("btnEnlargeGraph configurado correctamente");
                } else {
                    System.err.println("Error: btnEnlargeGraph es null");
                }

                if (btnExportGraph != null) {
                    btnExportGraph.setOnAction(event -> {
                        try {
                            exportGraph();
                        } catch (Exception e) {
                            System.err.println("Error al exportar gráfico: " + e.getMessage());
                            e.printStackTrace();
                            showNotification("Error al exportar gráfico: " + e.getMessage());
                        }
                    });
                    System.out.println("btnExportGraph configurado correctamente");
                } else {
                    System.err.println("Error: btnExportGraph es null");
                }

                if (btnFindIntersection != null) {
                    btnFindIntersection.setOnAction(event -> {
                        try {
                            findIntersection();
                        } catch (Exception e) {
                            System.err.println("Error al buscar intersección: " + e.getMessage());
                            e.printStackTrace();
                            showNotification("Error al buscar intersección: " + e.getMessage());
                        }
                    });
                    System.out.println("btnFindIntersection configurado correctamente");
                } else {
                    System.err.println("Error: btnFindIntersection es null");
                }

                if (btnCalculateDerivative != null) {
                    btnCalculateDerivative.setOnAction(event -> {
                        try {
                            calculateDerivative();
                        } catch (Exception e) {
                            System.err.println("Error al calcular derivada: " + e.getMessage());
                            e.printStackTrace();
                            showNotification("Error al calcular derivada: " + e.getMessage());
                        }
                    });
                    System.out.println("btnCalculateDerivative configurado correctamente");
                } else {
                    System.err.println("Error: btnCalculateDerivative es null");
                }

                if (btnCalculateIntegral != null) {
                    btnCalculateIntegral.setOnAction(event -> {
                        try {
                            calculateIntegral();
                        } catch (Exception e) {
                            System.err.println("Error al calcular integral: " + e.getMessage());
                            e.printStackTrace();
                            showNotification("Error al calcular integral: " + e.getMessage());
                        }
                    });
                    System.out.println("btnCalculateIntegral configurado correctamente");
                } else {
                    System.err.println("Error: btnCalculateIntegral es null");
                }

                if (btnShowTable != null) {
                    btnShowTable.setOnAction(event -> {
                        try {
                            showTable();
                        } catch (Exception e) {
                            System.err.println("Error al mostrar tabla: " + e.getMessage());
                            e.printStackTrace();
                            showNotification("Error al mostrar tabla: " + e.getMessage());
                        }
                    });
                    System.out.println("btnShowTable configurado correctamente");
                } else {
                    System.err.println("Error: btnShowTable es null");
                }

                if (btnAddPoint != null) {
                    btnAddPoint.setOnAction(event -> {
                        try {
                            addPoint();
                        } catch (Exception e) {
                            System.err.println("Error al añadir punto: " + e.getMessage());
                            e.printStackTrace();
                            showNotification("Error al añadir punto: " + e.getMessage());
                        }
                    });
                    System.out.println("btnAddPoint configurado correctamente");
                } else {
                    System.err.println("Error: btnAddPoint es null");
                }

                if (btnSettings != null) {
                    btnSettings.setOnAction(event -> {
                        try {
                            showSettings();
                        } catch (Exception e) {
                            System.err.println("Error al mostrar configuración: " + e.getMessage());
                            e.printStackTrace();
                            showNotification("Error al mostrar configuración: " + e.getMessage());
                        }
                    });
                    System.out.println("btnSettings configurado correctamente");
                } else {
                    System.err.println("Error: btnSettings es null");
                }

                // Configurar evento para el botón de toggle del sidebar
                if (btnToggleSidebar != null) {
                    btnToggleSidebar.setOnAction(event -> {
                        try {
                            toggleSidebar();
                        } catch (Exception e) {
                            System.err.println("Error al alternar sidebar: " + e.getMessage());
                            e.printStackTrace();
                            showNotification("Error al alternar sidebar: " + e.getMessage());
                        }
                    });
                    System.out.println("btnToggleSidebar configurado correctamente");
                } else {
                    System.err.println("Error: btnToggleSidebar es null");
                }

                System.out.println("Configuración de manejadores de eventos completada");
            } catch (Exception e) {
                System.err.println("Error al configurar manejadores de eventos: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Añadir menú contextual a la lista de funciones
        if (lstFunctions != null) {
            javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();

            javafx.scene.control.MenuItem analyzeItem = new javafx.scene.control.MenuItem("Analizar función");
            analyzeItem.setOnAction(event -> analyzeFunction());

            javafx.scene.control.MenuItem changeColorItem = new javafx.scene.control.MenuItem("Cambiar color");
            changeColorItem.setOnAction(event -> changeSelectedFunctionColor());

            javafx.scene.control.MenuItem changeStyleItem = new javafx.scene.control.MenuItem("Cambiar estilo");
            changeStyleItem.setOnAction(event -> changeSelectedFunctionStyle());

            contextMenu.getItems().addAll(analyzeItem, changeColorItem, changeStyleItem);
            lstFunctions.setContextMenu(contextMenu);
        }

        // Configurar eventos para coordenadas del mouse
        if (chart != null && xAxis != null && yAxis != null && lblCoordinates != null) {
            chart.setOnMouseMoved(event -> {
                double x = xAxis.getValueForDisplay(event.getX());
                double y = yAxis.getValueForDisplay(event.getY());
                lblCoordinates.setText(String.format("(%.2f, %.2f)", x, y));
            });
        }

        // Manejar cambios en modo de rango infinito
        if (chkInfiniteRange != null) {
            chkInfiniteRange.selectedProperty().addListener((obs, oldVal, newVal) -> {
                toggleInfiniteRangeMode(newVal);
            });
        }
    }

    /**
     * Alterna entre mostrar y ocultar el panel lateral con animaciones mejoradas
     */
    private void toggleSidebar() {
        if (sidebar == null || btnToggleSidebar == null || mainContent == null) {
            return; // No hacer nada si algún componente es nulo
        }

        sidebarCollapsed = !sidebarCollapsed;

        // Crear animación para el sidebar con curva de aceleración mejorada
        TranslateTransition sidebarTransition = new TranslateTransition(Duration.millis(500), sidebar);
        sidebarTransition.setInterpolator(javafx.animation.Interpolator.SPLINE(0.25, 0.1, 0.25, 1.0));

        // Crear animación de rotación para el botón de toggle
        javafx.animation.RotateTransition rotateBtn = new javafx.animation.RotateTransition(Duration.millis(400), btnToggleSidebar);

        // Crear animación para el contenido principal
        TranslateTransition contentTransition = new TranslateTransition(Duration.millis(500), mainContent);
        contentTransition.setInterpolator(javafx.animation.Interpolator.SPLINE(0.25, 0.1, 0.25, 1.0));

        // Crear animación de escala para el botón
        javafx.animation.ScaleTransition scaleBtn = new javafx.animation.ScaleTransition(Duration.millis(300), btnToggleSidebar);
        scaleBtn.setFromX(1.0);
        scaleBtn.setFromY(1.0);
        scaleBtn.setToX(1.2);
        scaleBtn.setToY(1.2);
        scaleBtn.setCycleCount(2);
        scaleBtn.setAutoReverse(true);

        if (sidebarCollapsed) {
            // Colapsar sidebar
            sidebarTransition.setToX(-sidebar.getWidth() + 50);
            btnToggleSidebar.setText(">");
            sidebar.getStyleClass().add("sidebar-collapsed");
            mainContent.getStyleClass().add("expanded-content");

            // Animar el contenido principal
            contentTransition.setFromX(0);
            contentTransition.setToX(50);

            // Rotar el botón
            rotateBtn.setByAngle(180);

            // Mover el botón de toggle fuera del sidebar para que siga siendo accesible
            btnToggleSidebar.setTranslateX(sidebar.getWidth() - 50);

            // Animar las secciones del sidebar para que desaparezcan secuencialmente
            animateSidebarSections(true);
        } else {
            // Expandir sidebar
            sidebarTransition.setToX(0);
            btnToggleSidebar.setText("<");
            sidebar.getStyleClass().remove("sidebar-collapsed");
            mainContent.getStyleClass().remove("expanded-content");

            // Animar el contenido principal
            contentTransition.setFromX(50);
            contentTransition.setToX(0);

            // Rotar el botón
            rotateBtn.setByAngle(-180);

            // Restaurar la posición del botón de toggle
            btnToggleSidebar.setTranslateX(0);

            // Animar las secciones del sidebar para que aparezcan secuencialmente
            animateSidebarSections(false);
        }

        // Reproducir todas las animaciones
        javafx.animation.ParallelTransition parallelTransition = new javafx.animation.ParallelTransition(
            sidebarTransition, rotateBtn, contentTransition, scaleBtn
        );

        parallelTransition.play();
    }

    /**
     * Anima las secciones del sidebar para que aparezcan o desaparezcan secuencialmente
     * @param collapse true para colapsar, false para expandir
     */
    private void animateSidebarSections(boolean collapse) {
        // Obtener todas las secciones del sidebar
        List<javafx.scene.Node> sections = sidebar.getChildren().stream()
            .filter(node -> node instanceof VBox && node.getStyleClass().contains("sidebar-section"))
            .collect(java.util.stream.Collectors.toList());

        // Si no hay secciones o estamos colapsando, no hacer nada
        if (sections.isEmpty() || collapse) {
            return;
        }

        // Crear una secuencia de animaciones para las secciones
        javafx.animation.SequentialTransition sequentialTransition = new javafx.animation.SequentialTransition();

        // Añadir animaciones para cada sección con un pequeño retraso entre ellas
        for (int i = 0; i < sections.size(); i++) {
            javafx.scene.Node section = sections.get(i);

            // Crear animación de fade y escala
            javafx.animation.FadeTransition fadeTransition = new javafx.animation.FadeTransition(Duration.millis(200), section);
            fadeTransition.setFromValue(0.3);
            fadeTransition.setToValue(1.0);

            javafx.animation.ScaleTransition scaleTransition = new javafx.animation.ScaleTransition(Duration.millis(200), section);
            scaleTransition.setFromX(0.95);
            scaleTransition.setFromY(0.95);
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);

            // Combinar las animaciones
            javafx.animation.ParallelTransition parallelTransition = new javafx.animation.ParallelTransition(
                fadeTransition, scaleTransition
            );

            // Añadir un pequeño retraso entre secciones
            parallelTransition.setDelay(Duration.millis(i * 50));

            // Añadir a la secuencia
            sequentialTransition.getChildren().add(parallelTransition);
        }

        // Reproducir la secuencia
        sequentialTransition.play();
    }

    @FXML
    private void graficarFuncion() {
        String function = txtFunction.getText().trim();

        if (function.isEmpty()) {
            showNotification("Por favor ingresa una función");
            return;
        }

        if (validarFuncion(function)) {
            // Añadir función a la lista si es nueva
            if (!functions.contains(function)) {
                functions.add(function);
                lstFunctions.getItems().add(function);
                lstFunctions.getSelectionModel().select(functions.size() - 1);
            }

            // Actualizar renderizado LaTeX
            updateLatexRender(function);

            // Graficar todas las funciones
            graficarFunciones();
        } else {
            showNotification("Función inválida. Por favor revisa la sintaxis.");
            txtFunction.requestFocus();
        }
    }

    private boolean validarFuncion(String function) {
        try {
            // Reemplazar x con un valor dentro del rango para validación
            String testExpr = function.replaceAll("x", "0");
            evaluator.eval(testExpr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Grafica todas las funciones con animaciones
     */
    private void graficarFunciones() {
        // Limpiar todos los conjuntos de datos anteriores
        chart.getDatasets().clear();
        dataSets.clear();

        // Crear una secuencia de animaciones para añadir las funciones una por una
        javafx.animation.SequentialTransition sequentialTransition = new javafx.animation.SequentialTransition();

        // Graficar cada función con un pequeño retraso entre ellas
        for (int i = 0; i < functions.size(); i++) {
            final int index = i;
            final String function = functions.get(i);

            // Calcular los puntos para la función
            DoubleDataSet dataSet = calcularPuntos(function, i);
            dataSets.put(function, dataSet);

            // Crear una pausa antes de añadir la función (excepto para la primera)
            if (i > 0) {
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(200));
                sequentialTransition.getChildren().add(pause);
            }

            // Crear una animación para añadir la función
            javafx.animation.Transition addFunctionTransition = new javafx.animation.Transition() {
                {
                    setCycleDuration(Duration.millis(500));
                    setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
                }

                @Override
                protected void interpolate(double frac) {
                    // Si es la primera vez que se llama con frac > 0, añadir el conjunto de datos
                    if (frac > 0 && !chart.getDatasets().contains(dataSet)) {
                        chart.getDatasets().add(dataSet);

                        // Aplicar una animación de aparición gradual
                        String color = COLORS[index % COLORS.length];
                        String baseStyle = "strokeColor=" + color + ";";

                        // Animar el grosor de la línea y la opacidad
                        double strokeWidth = 2.0 * frac;
                        double opacity = frac;
                        dataSet.setStyle(baseStyle + "strokeWidth=" + strokeWidth + ";opacity=" + opacity);
                    } else if (frac > 0) {
                        // Actualizar el estilo durante la animación
                        String color = COLORS[index % COLORS.length];
                        String baseStyle = "strokeColor=" + color + ";";

                        // Animar el grosor de la línea y la opacidad
                        double strokeWidth = 2.0 * frac;
                        double opacity = frac;
                        dataSet.setStyle(baseStyle + "strokeWidth=" + strokeWidth + ";opacity=" + opacity);
                    }
                }
            };

            sequentialTransition.getChildren().add(addFunctionTransition);
        }

        // Al finalizar todas las animaciones, resaltar la función seleccionada
        sequentialTransition.setOnFinished(event -> {
            if (selectedFunctionIndex >= 0) {
                highlightSelectedSeries(selectedFunctionIndex);
            }

            // Añadir un efecto de "rebote" suave al finalizar
            javafx.animation.ScaleTransition scaleTransition = new javafx.animation.ScaleTransition(Duration.millis(300), chart);
            scaleTransition.setFromX(0.98);
            scaleTransition.setFromY(0.98);
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.setInterpolator(javafx.animation.Interpolator.SPLINE(0.25, 0.1, 0.25, 1.0));
            scaleTransition.play();
        });

        // Reproducir la secuencia de animaciones
        sequentialTransition.play();
    }

    /**
     * Calcula los puntos para graficar una función
     * @param function La función a graficar
     * @param index El índice de la función en la lista (para determinar el color)
     * @return Un conjunto de datos con los puntos calculados
     */
    private DoubleDataSet calcularPuntos(String function, int index) {
        // Crear conjunto de datos con nombre de la función
        DoubleDataSet dataSet = new DoubleDataSet(function);

        // Determinar el rango y paso para el cálculo
        double rangeMin, rangeMax, step;

        if (chkInfiniteRange.isSelected()) {
            // En modo infinito, usar un rango amplio pero razonable para la visualización
            rangeMin = -100;
            rangeMax = 100;
            // Usar más puntos para funciones en modo infinito para mejor resolución
            step = (rangeMax - rangeMin) / (POINTS * 3); // Aumentar densidad de puntos
        } else {
            // En modo normal, usar el rango definido por el usuario
            rangeMin = xMin;
            rangeMax = xMax;
            step = (rangeMax - rangeMin) / POINTS;
        }

        // Verificar si la función es lineal (como 5*x) para evitar falsos positivos en discontinuidades
        boolean isLinearFunction = isLinearFunction(function);

        // Variables para detectar discontinuidades
        Double lastX = null;
        Double lastY = null;
        double maxJump = (rangeMax - rangeMin) / 10; // Umbral para considerar discontinuidad

        // Variables para detección adaptativa de discontinuidades
        double avgChange = 0;
        int changeCount = 0;
        List<Double> recentChanges = new ArrayList<>();
        final int RECENT_CHANGES_SIZE = 5; // Número de cambios recientes a considerar

        // Asegurarnos de que el punto (0,0) esté incluido en los puntos a evaluar
        boolean originIncluded = false;
        if (rangeMin <= 0 && rangeMax >= 0) {
            // Ajustar el paso para asegurar que 0 sea uno de los puntos evaluados
            double stepsToZero = Math.abs(rangeMin) / step;
            if (stepsToZero != Math.floor(stepsToZero)) {
                // Si no cae exactamente en un paso, ajustar ligeramente el paso
                step = Math.abs(rangeMin) / Math.floor(stepsToZero);
            }
            originIncluded = true;
        }

        // Calcular puntos con detección adaptativa de discontinuidades
        for (double x = rangeMin; x <= rangeMax; x += step) {
            // Corregir pequeñas imprecisiones numéricas para asegurar que evaluamos exactamente en x=0
            if (Math.abs(x) < 1e-10) {
                x = 0.0; // Asegurar que es exactamente 0
            }

            try {
                // Comprobar si x está en el dominio de la función antes de evaluar
                if (!isInDomain(function, x)) {
                    // Si no está en el dominio, añadir un punto NaN para crear discontinuidad visual
                    if (lastX != null) {
                        // Añadir un punto justo antes de la discontinuidad
                        double xBefore = x - step * 0.1;
                        if (xBefore > lastX) {
                            try {
                                double yBefore = evaluarFuncion(function, xBefore);
                                if (!Double.isNaN(yBefore) && !Double.isInfinite(yBefore)) {
                                    dataSet.add(xBefore, yBefore);
                                }
                            } catch (Exception e) {
                                // Ignorar errores en puntos adicionales
                            }
                        }

                        // Añadir el punto NaN para la discontinuidad
                        dataSet.add(x, Double.NaN);
                    }
                    lastX = null;
                    lastY = null;
                    continue;
                }

                double y = evaluarFuncion(function, x);

                // Verificar si el valor es válido (no NaN, no infinito)
                if (Double.isNaN(y) || Double.isInfinite(y)) {
                    // Añadir un punto NaN para crear discontinuidad visual
                    if (lastX != null) {
                        // Añadir un punto justo antes de la discontinuidad
                        double xBefore = x - step * 0.1;
                        if (xBefore > lastX) {
                            try {
                                double yBefore = evaluarFuncion(function, xBefore);
                                if (!Double.isNaN(yBefore) && !Double.isInfinite(yBefore)) {
                                    dataSet.add(xBefore, yBefore);
                                }
                            } catch (Exception e) {
                                // Ignorar errores en puntos adicionales
                            }
                        }

                        // Añadir el punto NaN para la discontinuidad
                        dataSet.add(x, Double.NaN);
                    }
                    lastX = null;
                    lastY = null;
                    continue;
                }

                // Detectar discontinuidades (saltos grandes en el valor de y)
                // Para funciones lineales, omitimos la detección de discontinuidades
                if (lastY != null && !isLinearFunction) {
                    double change = Math.abs(y - lastY);

                    // Actualizar estadísticas de cambios
                    if (recentChanges.size() >= RECENT_CHANGES_SIZE) {
                        recentChanges.remove(0);
                    }
                    recentChanges.add(change);

                    // Calcular el cambio promedio reciente
                    double recentAvgChange = recentChanges.stream().mapToDouble(Double::doubleValue).average().orElse(0);

                    // Actualizar el promedio global de cambios
                    changeCount++;
                    avgChange = avgChange + (change - avgChange) / changeCount;

                    // Umbral adaptativo para discontinuidades
                    // Considera tanto el cambio promedio global como el reciente
                    double adaptiveThreshold = Math.max(maxJump, 10 * Math.max(avgChange, recentAvgChange));

                    // Evitar falsos positivos cerca del origen para funciones que pasan por (0,0)
                    if (Math.abs(x) < step && Math.abs(lastX) < step) {
                        // Cerca del origen, aumentamos el umbral para evitar falsos positivos
                        adaptiveThreshold *= 10;
                    }

                    if (change > adaptiveThreshold) {
                        // Detectamos una discontinuidad

                        // Añadir puntos adicionales cerca de la discontinuidad para mejorar la visualización
                        double xMid = (lastX + x) / 2;

                        // Intentar evaluar puntos a ambos lados de la discontinuidad
                        double xBefore = xMid - step * 0.1;
                        double xAfter = xMid + step * 0.1;

                        if (xBefore > lastX) {
                            try {
                                double yBefore = evaluarFuncion(function, xBefore);
                                if (!Double.isNaN(yBefore) && !Double.isInfinite(yBefore) && 
                                    Math.abs(yBefore - lastY) < adaptiveThreshold) {
                                    dataSet.add(xBefore, yBefore);
                                }
                            } catch (Exception e) {
                                // Ignorar errores en puntos adicionales
                            }
                        }

                        // Añadir el punto NaN para la discontinuidad
                        dataSet.add(xMid, Double.NaN);

                        if (xAfter < x) {
                            try {
                                double yAfter = evaluarFuncion(function, xAfter);
                                if (!Double.isNaN(yAfter) && !Double.isInfinite(yAfter) && 
                                    Math.abs(yAfter - y) < adaptiveThreshold) {
                                    dataSet.add(xAfter, yAfter);
                                }
                            } catch (Exception e) {
                                // Ignorar errores en puntos adicionales
                            }
                        }
                    }
                }

                // En modo infinito, añadir todos los puntos válidos
                // En modo normal, solo añadir si está dentro del rango Y
                if (chkInfiniteRange.isSelected() || (y >= yMin && y <= yMax)) {
                    dataSet.add(x, y);
                    lastX = x;
                    lastY = y;
                }
            } catch (Exception e) {
                // Para puntos donde la evaluación falla, añadir NaN para mostrar discontinuidad
                if (lastX != null) {
                    dataSet.add(x, Double.NaN);
                }
                lastX = null;
                lastY = null;
            }
        }

        // Aplicar estilo según el índice de la función
        String color = COLORS[index % COLORS.length];
        dataSet.setStyle("strokeColor=" + color + ";strokeWidth=2");

        return dataSet;
    }

    /**
     * Verifica si una función es lineal (como 5*x, -2*x, x, etc.)
     * @param function La función a verificar
     * @return true si la función es lineal, false en caso contrario
     */
    private boolean isLinearFunction(String function) {
        // Eliminar espacios en blanco
        String cleanFunction = function.replaceAll("\\s+", "");

        // Patrones comunes de funciones lineales
        if (cleanFunction.matches("[-+]?\\d*\\.?\\d*\\*?x") || // 5*x, -2*x, x
            cleanFunction.matches("x\\*[-+]?\\d*\\.?\\d*") || // x*5, x*-2
            cleanFunction.matches("[-+]?\\d*\\.?\\d*x") || // 5x, -2x
            cleanFunction.equals("x") || // Simplemente x
            cleanFunction.matches("[-+]x")) { // +x, -x
            return true;
        }

        return false;
    }

    /**
     * Verifica si un valor x está en el dominio de la función
     * @param function La función a evaluar
     * @param x El valor a comprobar
     * @return true si x está en el dominio, false en caso contrario
     */
    private boolean isInDomain(String function, double x) {
        // Manejo especial para x=0 para evitar problemas de precisión
        if (Math.abs(x) < 1e-10) {
            x = 0.0; // Asegurar que es exactamente 0
        }

        // Comprobar divisiones por cero de manera más robusta
        if (function.contains("/")) {
            // Buscar expresiones como "1/(x-2)" o "1/x" y verificar si x hace que el denominador sea cero
            String[] parts = function.split("/");
            for (int i = 1; i < parts.length; i++) {
                String denominador = parts[i].trim();

                // Casos comunes de denominadores que pueden ser cero
                if (denominador.equals("x") && x == 0.0) {
                    return false;
                }

                // Manejar expresiones más complejas en el denominador
                try {
                    // Evaluar el denominador para ver si es cero o muy cercano a cero
                    String expr = denominador.replaceAll("x", String.valueOf(x));
                    double result = evaluator.eval(expr).evalDouble();
                    if (Math.abs(result) < 1e-10) {
                        return false;
                    }
                } catch (Exception e) {
                    // Si hay un error al evaluar, asumimos que está fuera del dominio
                    return false;
                }
            }
        }

        // Comprobar raíces cuadradas y raíces de orden n de números negativos
        if (function.contains("sqrt(") || function.contains("√") || function.contains("^(1/") || 
            function.contains("^0.5") || function.contains("**(1/") || function.contains("**0.5")) {

            // Buscar expresiones como "sqrt(x-2)" y verificar si x hace que el argumento sea negativo
            int startIdx = 0;
            while (true) {
                int sqrtIdx = function.indexOf("sqrt(", startIdx);
                if (sqrtIdx == -1) {
                    sqrtIdx = function.indexOf("√", startIdx);
                    if (sqrtIdx == -1) break;
                }

                // Encontrar el argumento de la raíz cuadrada
                int openParenIdx = function.indexOf("(", sqrtIdx);
                if (openParenIdx == -1) break;

                int closeParenIdx = findMatchingCloseParen(function, openParenIdx);
                if (closeParenIdx == -1) break;

                String argument = function.substring(openParenIdx + 1, closeParenIdx);
                try {
                    // Evaluar el argumento para ver si es negativo
                    String expr = argument.replaceAll("x", String.valueOf(x));
                    double result = evaluator.eval(expr).evalDouble();
                    if (result < 0) {
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }

                startIdx = closeParenIdx + 1;
            }

            // Buscar expresiones de raíces con exponentes fraccionarios como (x-2)^(1/3)
            // Solo nos preocupan las raíces pares (1/2, 1/4, etc.) que no pueden tener argumentos negativos
            startIdx = 0;
            while (function.indexOf("^(1/", startIdx) != -1 || function.indexOf("**(1/", startIdx) != -1) {
                int expIdx = function.indexOf("^(1/", startIdx);
                if (expIdx == -1) {
                    expIdx = function.indexOf("**(1/", startIdx);
                    if (expIdx == -1) break;
                }

                // Encontrar la base y el exponente
                int baseEndIdx = expIdx;
                int baseStartIdx = findBaseStart(function, baseEndIdx);
                if (baseStartIdx == -1) break;

                String base = function.substring(baseStartIdx, baseEndIdx);

                // Encontrar el exponente
                int expStartIdx = function.indexOf("(1/", expIdx);
                if (expStartIdx == -1) break;

                int expEndIdx = function.indexOf(")", expStartIdx);
                if (expEndIdx == -1) break;

                String exponent = function.substring(expStartIdx + 1, expEndIdx);

                try {
                    // Evaluar la base
                    String baseExpr = base.replaceAll("x", String.valueOf(x));
                    double baseResult = evaluator.eval(baseExpr).evalDouble();

                    // Evaluar el exponente
                    String expExpr = exponent;
                    double expResult = evaluator.eval(expExpr).evalDouble();

                    // Si el exponente es una fracción con denominador par (1/2, 1/4, etc.)
                    // y la base es negativa, está fuera del dominio
                    if (expResult > 0 && 1.0 / expResult % 2 == 0 && baseResult < 0) {
                        return false;
                    }
                } catch (Exception e) {
                    // Si hay un error al evaluar, asumimos que está fuera del dominio
                    return false;
                }

                startIdx = expEndIdx + 1;
            }
        }

        // Comprobar logaritmos de números negativos o cero
        if (function.contains("log(") || function.contains("ln(") || 
            function.contains("log10(") || function.contains("log2(")) {

            // Buscar expresiones como "log(x-2)" y verificar si x hace que el argumento sea negativo o cero
            int startIdx = 0;
            String[] logFunctions = {"log(", "ln(", "log10(", "log2("};

            for (String logFunc : logFunctions) {
                startIdx = 0;
                while (true) {
                    int logIdx = function.indexOf(logFunc, startIdx);
                    if (logIdx == -1) break;

                    // Encontrar el argumento del logaritmo
                    int openParenIdx = logIdx + logFunc.length() - 1;
                    int closeParenIdx = findMatchingCloseParen(function, openParenIdx);
                    if (closeParenIdx == -1) break;

                    String argument = function.substring(openParenIdx + 1, closeParenIdx);
                    try {
                        // Evaluar el argumento para ver si es negativo o cero
                        String expr = argument.replaceAll("x", String.valueOf(x));
                        double result = evaluator.eval(expr).evalDouble();
                        if (result <= 0) {
                            return false;
                        }
                    } catch (Exception e) {
                        return false;
                    }

                    startIdx = closeParenIdx + 1;
                }
            }
        }

        // Comprobar funciones trigonométricas con restricciones de dominio
        // tan(x), sec(x), csc(x), cot(x) tienen restricciones en ciertos valores
        if (function.contains("tan(") || function.contains("sec(")) {
            // tan(x) y sec(x) no están definidas en x = (n + 1/2)π, donde n es un entero
            int startIdx = 0;
            String[] trigFunctions = {"tan(", "sec("};

            for (String trigFunc : trigFunctions) {
                startIdx = 0;
                while (true) {
                    int funcIdx = function.indexOf(trigFunc, startIdx);
                    if (funcIdx == -1) break;

                    // Encontrar el argumento de la función
                    int openParenIdx = funcIdx + trigFunc.length() - 1;
                    int closeParenIdx = findMatchingCloseParen(function, openParenIdx);
                    if (closeParenIdx == -1) break;

                    String argument = function.substring(openParenIdx + 1, closeParenIdx);
                    try {
                        // Evaluar el argumento
                        String expr = argument.replaceAll("x", String.valueOf(x));
                        double argValue = evaluator.eval(expr).evalDouble();

                        // Verificar si el argumento está cerca de (n + 1/2)π
                        double normalized = Math.abs(argValue) % Math.PI;
                        if (Math.abs(normalized - Math.PI/2) < 1e-10 || 
                            Math.abs(normalized - 3*Math.PI/2) < 1e-10) {
                            return false;
                        }
                    } catch (Exception e) {
                        return false;
                    }

                    startIdx = closeParenIdx + 1;
                }
            }
        }

        if (function.contains("csc(") || function.contains("cot(")) {
            // csc(x) y cot(x) no están definidas en x = nπ, donde n es un entero
            int startIdx = 0;
            String[] trigFunctions = {"csc(", "cot("};

            for (String trigFunc : trigFunctions) {
                startIdx = 0;
                while (true) {
                    int funcIdx = function.indexOf(trigFunc, startIdx);
                    if (funcIdx == -1) break;

                    // Encontrar el argumento de la función
                    int openParenIdx = funcIdx + trigFunc.length() - 1;
                    int closeParenIdx = findMatchingCloseParen(function, openParenIdx);
                    if (closeParenIdx == -1) break;

                    String argument = function.substring(openParenIdx + 1, closeParenIdx);
                    try {
                        // Evaluar el argumento
                        String expr = argument.replaceAll("x", String.valueOf(x));
                        double argValue = evaluator.eval(expr).evalDouble();

                        // Verificar si el argumento está cerca de nπ
                        double normalized = Math.abs(argValue) % Math.PI;
                        if (normalized < 1e-10 || Math.abs(normalized - Math.PI) < 1e-10) {
                            return false;
                        }
                    } catch (Exception e) {
                        return false;
                    }

                    startIdx = closeParenIdx + 1;
                }
            }
        }

        // Si pasó todas las verificaciones, el punto está en el dominio
        return true;
    }

    /**
     * Encuentra el inicio de la base en una expresión de potencia
     * @param str La cadena a analizar
     * @param endIdx El índice donde termina la base (justo antes del operador de potencia)
     * @return El índice donde comienza la base, o -1 si no se puede determinar
     */
    private int findBaseStart(String str, int endIdx) {
        // Si la base está entre paréntesis, buscar el paréntesis de apertura correspondiente
        if (endIdx > 0 && str.charAt(endIdx - 1) == ')') {
            int count = 1;
            for (int i = endIdx - 2; i >= 0; i--) {
                if (str.charAt(i) == ')') {
                    count++;
                } else if (str.charAt(i) == '(') {
                    count--;
                    if (count == 0) {
                        return i;
                    }
                }
            }
            return -1;
        }

        // Si no hay paréntesis, buscar el inicio de la base (número, variable o función)
        int i = endIdx - 1;
        while (i >= 0) {
            char c = str.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '.' || c == '_' || c == 'x') {
                i--;
            } else {
                break;
            }
        }
        return i + 1;
    }

    /**
     * Encuentra el paréntesis de cierre correspondiente a un paréntesis de apertura
     * @param str La cadena a analizar
     * @param openParenIdx El índice del paréntesis de apertura
     * @return El índice del paréntesis de cierre correspondiente, o -1 si no se encuentra
     */
    private int findMatchingCloseParen(String str, int openParenIdx) {
        int count = 1;
        for (int i = openParenIdx + 1; i < str.length(); i++) {
            if (str.charAt(i) == '(') {
                count++;
            } else if (str.charAt(i) == ')') {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Evalúa una función matemática en un punto específico
     * @param function La función a evaluar
     * @param x El valor en el que evaluar la función
     * @return El valor de la función en x, o NaN si no está definido
     */
    private double evaluarFuncion(String function, double x) {
        // Verificar si x está en el dominio de la función
        if (!isInDomain(function, x)) {
            return Double.NaN; // Retornar NaN para puntos fuera del dominio
        }

        // Manejo especial para x=0 para evitar problemas de precisión
        if (Math.abs(x) < 1e-10) {
            x = 0.0; // Asegurar que es exactamente 0
        }

        // Usar caché para valores comunes
        String cacheKey = function + "_" + x;
        if (evaluationCache.containsKey(cacheKey)) {
            return evaluationCache.get(cacheKey);
        }

        // Reemplazar x con el valor específico
        String expr = function.replaceAll("x", String.valueOf(x));

        try {
            // Evaluar expresión con manejo de errores mejorado
            double result = evaluator.eval(expr).evalDouble();

            // Verificar si el resultado es un valor válido
            if (Double.isNaN(result) || Double.isInfinite(result)) {
                // No guardar en caché valores especiales
                return Double.NaN; // Convertir infinitos a NaN para mejor visualización
            }

            // Verificar si el resultado es extremadamente grande o pequeño (puede indicar un problema numérico)
            if (Math.abs(result) > 1e10 || (Math.abs(result) < 1e-10 && Math.abs(result) > 0)) {
                // Para valores extremos, verificar si están cerca de una asíntota
                // Probar con varios puntos cercanos para confirmar comportamiento asintótico
                double[] deltas = {1e-6, -1e-6, 1e-5, -1e-5};
                boolean isAsymptotic = false;

                for (double delta : deltas) {
                    double xNearby = x + delta;
                    if (!isInDomain(function, xNearby)) {
                        continue; // Saltar puntos fuera del dominio
                    }

                    try {
                        String exprNearby = function.replaceAll("x", String.valueOf(xNearby));
                        double resultNearby = evaluator.eval(exprNearby).evalDouble();

                        // Si el resultado es válido, calcular la tasa de cambio
                        if (!Double.isNaN(resultNearby) && !Double.isInfinite(resultNearby)) {
                            double rate = Math.abs((resultNearby - result) / delta);

                            // Si la tasa de cambio es muy alta, probablemente estamos cerca de una asíntota
                            if (rate > 1e8) {
                                isAsymptotic = true;
                                break;
                            }

                            // Si hay un cambio de signo extremo, también puede ser una asíntota
                            if (result * resultNearby < 0 && Math.abs(result) > 1e5 && Math.abs(resultNearby) > 1e5) {
                                isAsymptotic = true;
                                break;
                            }
                        } else {
                            // Si un punto cercano da un valor no válido, es probable que estemos cerca de una asíntota
                            isAsymptotic = true;
                            break;
                        }
                    } catch (Exception ex) {
                        // Si falla la evaluación del punto cercano, asumir discontinuidad
                        isAsymptotic = true;
                        break;
                    }
                }

                if (isAsymptotic) {
                    return Double.NaN; // Tratar como discontinuidad
                }
            }

            // Verificar si el resultado es un valor numérico válido
            if (Double.isFinite(result)) {
                // Guardar en caché para uso futuro
                evaluationCache.put(cacheKey, result);
                return result;
            } else {
                return Double.NaN;
            }
        } catch (Exception e) {
            // En lugar de propagar la excepción, retornar NaN para indicar un punto no válido
            return Double.NaN;
        }
    }

    @FXML
    private void actualizarRango() {
        try {
            // Intentar convertir valores de texto a números
            double newXMin = Double.parseDouble(txtXMin.getText());
            double newXMax = Double.parseDouble(txtXMax.getText());
            double newYMin = Double.parseDouble(txtYMin.getText());
            double newYMax = Double.parseDouble(txtYMax.getText());

            // Validar rangos
            if (newXMin >= newXMax) {
                showNotification("El valor mínimo de X debe ser menor que el máximo");
                mostrarErrorAnimacion(txtXMin);
                return;
            }

            if (newYMin >= newYMax) {
                showNotification("El valor mínimo de Y debe ser menor que el máximo");
                mostrarErrorAnimacion(txtYMin);
                return;
            }

            // Actualizar valores y ejes
            xMin = newXMin;
            xMax = newXMax;
            yMin = newYMin;
            yMax = newYMax;

            xAxis.set(xMin, xMax);
            yAxis.set(yMin, yMax);

            // Redibujar funciones con nuevo rango
            graficarFunciones();

        } catch (NumberFormatException e) {
            showNotification("Por favor ingresa valores numéricos válidos para los rangos");
        }
    }

    private void mostrarErrorAnimacion(TextField textField) {
        // Animar campo de texto para mostrar error
        String originalStyle = textField.getStyle();
        textField.setStyle(originalStyle + "; -fx-border-color: red; -fx-border-width: 2px;");

        // Programar retorno al estilo original
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
        pause.setOnFinished(e -> textField.setStyle(originalStyle));
        pause.play();
    }

    @FXML
    public void addNewFunction() {
        txtFunction.clear();
        txtFunction.requestFocus();
    }

    @FXML
    public void deleteSelectedFunction() {
        int index = lstFunctions.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            // Eliminar de listas
            String function = functions.remove(index);
            lstFunctions.getItems().remove(index);

            // Eliminar del gráfico
            for (DataSet dataSet : new ArrayList<>(chart.getDatasets())) {
                if (dataSet.getName().equals(function)) {
                    chart.getDatasets().remove(dataSet);
                    break;
                }
            }

            // Actualizar función seleccionada
            if (!functions.isEmpty()) {
                int newIndex = Math.min(index, functions.size() - 1);
                lstFunctions.getSelectionModel().select(newIndex);
            } else {
                selectedFunctionIndex = -1;
                lblSelectedFunction.setText("Ninguna función seleccionada");
            }
        }
    }

    private void selectFunction(int index) {
        if (index >= 0 && index < functions.size()) {
            selectedFunctionIndex = index;
            String function = functions.get(index);
            txtFunction.setText(function);
            lblSelectedFunction.setText("Función: " + function);
            updateLatexRender(function);
            highlightSelectedSeries(index);
        }
    }

    private void highlightSelectedSeries(int index) {
        if (index >= 0 && index < functions.size()) {
            String selectedFunction = functions.get(index);

            // Actualizar estilos para todos los conjuntos de datos
            for (DataSet dataSet : chart.getDatasets()) {
                String name = dataSet.getName();
                int functionIndex = functions.indexOf(name);
                // Asegurar que el índice sea válido antes de usar el operador módulo
                int colorIndex = functionIndex >= 0 ? functionIndex % COLORS.length : 0;
                String color = COLORS[colorIndex];

                if (name.equals(selectedFunction)) {
                    // Función seleccionada: más gruesa
                    dataSet.setStyle("strokeColor=" + color + ";strokeWidth=3");
                } else {
                    // Otras funciones: normales
                    dataSet.setStyle("strokeColor=" + color + ";strokeWidth=1.5");
                }
            }
        }
    }

    private void toggleGridLines(boolean show) {
        xAxis.setTickLabelRotation(show ? 0 : 90);
        xAxis.setMinorTickVisible(show);
        yAxis.setMinorTickVisible(show);
        xAxis.setTickLabelsVisible(show);
        yAxis.setTickLabelsVisible(show);
        xAxis.setTickMarkVisible(show);
        yAxis.setTickMarkVisible(show);

        // Actualizar estilo del gráfico
        if (show) {
            chart.getGridRenderer().setDrawOnTop(true);
        } else {
            chart.getGridRenderer().setDrawOnTop(false);
        }
    }

    private void toggleTheme(boolean lightTheme) {
        String backgroundColor = lightTheme ? "white" : "#2b2b2b";
        String textColor = lightTheme ? "black" : "white";

        chart.setStyle("-fx-background-color: " + backgroundColor);
        xAxis.setTickLabelFill(javafx.scene.paint.Color.web(textColor));
        yAxis.setTickLabelFill(javafx.scene.paint.Color.web(textColor));

        // Actualizar líneas de cuadrícula
        chart.getGridRenderer().getHorizontalMinorGrid().setStyle("-fx-stroke: rgba(80,80,80,0.3);");
        chart.getGridRenderer().getVerticalMinorGrid().setStyle("-fx-stroke: rgba(80,80,80,0.3);");
        chart.getGridRenderer().getHorizontalMajorGrid().setStyle("-fx-stroke: rgba(80,80,80,0.8);");
        chart.getGridRenderer().getVerticalMajorGrid().setStyle("-fx-stroke: rgba(80,80,80,0.8);");
    }

    private void toggleInfiniteRangeMode(boolean enable) {
        if (enable) {
            // Guardar valores actuales
            savedXMin = xMin;
            savedXMax = xMax;
            savedYMin = yMin;
            savedYMax = yMax;

            // Configurar ejes para modo infinito
            configureAxesForInfiniteMode();

            // Actualizar UI para reflejar que el rango es infinito
            txtXMin.setDisable(true);
            txtXMax.setDisable(true);
            txtYMin.setDisable(true);
            txtYMax.setDisable(true);
            btnActualizarRango.setDisable(true);
        } else {
            // Restaurar valores guardados o usar predeterminados
            if (savedXMin != null && savedXMax != null) {
                xMin = savedXMin;
                xMax = savedXMax;
                xAxis.set(xMin, xMax);
            }

            if (savedYMin != null && savedYMax != null) {
                yMin = savedYMin;
                yMax = savedYMax;
                yAxis.set(yMin, yMax);
            }

            // Desactivar auto-ranging
            xAxis.setAutoRanging(false);
            yAxis.setAutoRanging(false);

            // Mantener los ejes cruzados en el origen
            xAxis.setForceZeroInRange(true);
            yAxis.setForceZeroInRange(true);

            // Habilitar controles de rango
            txtXMin.setDisable(false);
            txtXMax.setDisable(false);
            txtYMin.setDisable(false);
            txtYMax.setDisable(false);
            btnActualizarRango.setDisable(false);
        }

        // Limpiar caché de evaluación para forzar recálculo con nuevos parámetros
        evaluationCache.clear();

        // Redibujar funciones con configuración actualizada
        graficarFunciones();
    }

    /**
     * Configura los ejes para el modo de rango infinito
     * Optimiza la visualización para mostrar correctamente las funciones
     */
    private void configureAxesForInfiniteMode() {
        // Establecer auto-ranging para ambos ejes
        xAxis.setAutoRanging(true);
        yAxis.setAutoRanging(true);

        // Asegurar que los ejes se crucen en el origen (estilo cruz)
        xAxis.setForceZeroInRange(true);
        yAxis.setForceZeroInRange(true);

        // Configurar opciones adicionales para mejorar la visualización
        xAxis.setAutoRangeRounding(true);
        yAxis.setAutoRangeRounding(true);

        // Configurar márgenes para que haya espacio alrededor de la gráfica
        xAxis.setAutoRangePadding(0.1);
        yAxis.setAutoRangePadding(0.1);
    }

    private void applyZoom(double zoomFactor) {
        // Calcular centro del rango
        double xCenter = (xMin + xMax) / 2;
        double yCenter = (yMin + yMax) / 2;

        // Calcular tamaño de rango actual
        double xRange = xMax - xMin;
        double yRange = yMax - yMin;

        // Calcular nuevo tamaño de rango basado en factor de zoom
        double newXRange = xRange * (2 - zoomFactor / 100);
        double newYRange = yRange * (2 - zoomFactor / 100);

        // Calcular nuevos límites
        double newXMin = xCenter - newXRange / 2;
        double newXMax = xCenter + newXRange / 2;
        double newYMin = yCenter - newYRange / 2;
        double newYMax = yCenter + newYRange / 2;

        // Actualizar campos de texto
        txtXMin.setText(String.valueOf(newXMin));
        txtXMax.setText(String.valueOf(newXMax));
        txtYMin.setText(String.valueOf(newYMin));
        txtYMax.setText(String.valueOf(newYMax));

        // Aplicar nuevo rango
        actualizarRango();
    }

    @FXML
    public void enlargeGraph() {
        // Implementar vista ampliada del gráfico en ventana nueva
        javafx.stage.Stage newStage = new javafx.stage.Stage();
        newStage.setTitle("Gráfico Ampliado");

        // Crear copia del gráfico para la nueva ventana
        DefaultNumericAxis xAxisNew = new DefaultNumericAxis(xMin, xMax, 1);
        DefaultNumericAxis yAxisNew = new DefaultNumericAxis(yMin, yMax, 1);
        xAxisNew.setName("x");
        yAxisNew.setName("y");
        XYChart enlargedChart = new XYChart(xAxisNew, yAxisNew);

        // Copiar configuración y datos
        enlargedChart.getPlugins().addAll(chart.getPlugins());
        for (DataSet dataSet : chart.getDatasets()) {
            enlargedChart.getDatasets().add(dataSet);
        }

        // Configurar escena
        javafx.scene.Scene scene = new javafx.scene.Scene(enlargedChart, 800, 600);
        newStage.setScene(scene);
        newStage.show();
    }

    @FXML
    private void exportGraph() {
        // Captura un snapshot de la gráfica
        WritableImage image = chartContainer.snapshot(new SnapshotParameters(), null);

        // Configuración del selector de archivos
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                showNotification("Gráfica exportada exitosamente");
            } catch (IOException e) {
                showNotification("Error al exportar gráfica");
                e.printStackTrace();
            }
        }
    }

    private void setupLatexRender() {
        // Usar Platform.runLater para asegurar que la inicialización se realice en el hilo de JavaFX
        // después de que la escena esté completamente cargada
        Platform.runLater(() -> {
            try {
                System.out.println("Iniciando configuración del renderizador LaTeX...");

                if (latexRender == null) {
                    System.err.println("Error: latexRender es null");
                    return;
                }

                // Crear un panel para mostrar fórmulas LaTeX
                JPanel panel = new JPanel();
                panel.setBackground(Color.WHITE);

                // Configurar el SwingNode con el panel
                latexRender.setContent(panel);

                // Mostrar una fórmula inicial
                updateLatexRender("f(x) = x");

                System.out.println("Configuración del renderizador LaTeX completada");
            } catch (Exception e) {
                System.err.println("Error al configurar el renderizador LaTeX: " + e.getMessage());
                e.printStackTrace();

                // Intentar crear un panel de respaldo en caso de error
                try {
                    JPanel fallbackPanel = new JPanel();
                    fallbackPanel.setBackground(Color.WHITE);
                    JLabel errorLabel = new JLabel("Error al inicializar LaTeX: " + e.getMessage());
                    errorLabel.setForeground(Color.RED);
                    fallbackPanel.add(errorLabel);
                    latexRender.setContent(fallbackPanel);
                } catch (Exception ex) {
                    System.err.println("Error al crear panel de respaldo: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }

    private void updateLatexRender(String function) {
        // Usar SwingUtilities.invokeLater para asegurar que la actualización se realice en el hilo de Swing
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Actualizando renderizado LaTeX para: " + function);

                if (latexRender == null) {
                    System.err.println("Error: latexRender es null");
                    return;
                }

                if (function == null || function.trim().isEmpty()) {
                    // Mostrar un mensaje de espera en lugar de retornar silenciosamente
                    JPanel panel = new JPanel();
                    panel.setBackground(Color.WHITE);
                    JLabel waitingLabel = new JLabel("Esperando entrada de función...");
                    waitingLabel.setForeground(Color.GRAY);
                    panel.add(waitingLabel);
                    latexRender.setContent(panel);
                    return;
                }

                // Convertir la función a formato LaTeX
                String latexString = ConvertidorLatex.toLatex(function);
                System.out.println("Función convertida a LaTeX: " + latexString);

                // Crear la fórmula LaTeX
                TeXFormula formula = new TeXFormula(latexString);

                // Crear un icono TeXIcon a partir de la fórmula
                TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20);

                // Configurar dimensiones
                icon.setInsets(new Insets(5, 5, 5, 5));

                // Crear un JLabel para mostrar el icono
                JLabel label = new JLabel(icon);
                label.setForeground(Color.BLACK);

                // Crear un panel para contener el label
                JPanel panel = new JPanel();
                panel.setBackground(Color.WHITE);
                panel.add(label);

                // Actualizar el contenido del SwingNode
                latexRender.setContent(panel);
                System.out.println("Renderizado LaTeX actualizado correctamente");
            } catch (Exception e) {
                System.err.println("Error al renderizar LaTeX: " + e.getMessage());
                e.printStackTrace();

                // En caso de error, mostrar un mensaje más detallado
                try {
                    JPanel panel = new JPanel();
                    panel.setBackground(Color.WHITE);
                    JLabel errorLabel = new JLabel("Error al renderizar LaTeX: " + e.getMessage());
                    errorLabel.setForeground(Color.RED);
                    panel.add(errorLabel);
                    latexRender.setContent(panel);
                } catch (Exception ex) {
                    System.err.println("Error al crear panel de error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }

    private void setupSymbolButtons() {
        // Usar Platform.runLater para asegurar que la inicialización se realice en el hilo de JavaFX
        // después de que la escena esté completamente cargada
        Platform.runLater(() -> {
            try {
                System.out.println("Iniciando configuración de botones de símbolos...");

                if (symbolButtons == null) {
                    System.err.println("Error: symbolButtons es null");
                    return;
                }

                // Limpiar botones existentes
                symbolButtons.getChildren().clear();

                // Definir símbolos matemáticos comunes
                String[] symbols = {
                    "+", "-", "*", "/", "^", "√", "π", 
                    "sin", "cos", "tan", "log", "ln", 
                    "(", ")", "=", "x", "e"
                };

                System.out.println("Creando " + symbols.length + " botones de símbolos");

                // Crear botones para cada símbolo
                for (String symbol : symbols) {
                    try {
                        Button btn = new Button(symbol);
                        btn.getStyleClass().add("symbol-button");

                        // Asegurar que el botón sea clickeable
                        btn.setDisable(false);
                        btn.setFocusTraversable(false); // No robar el foco

                        // Configurar acción al hacer clic
                        btn.setOnAction(event -> {
                            try {
                                if (txtFunction != null) {
                                    // Obtener posición actual del cursor
                                    int caretPosition = txtFunction.getCaretPosition();

                                    // Obtener texto actual
                                    String currentText = txtFunction.getText();

                                    // Texto a insertar (con ajustes para funciones especiales)
                                    String insertText = symbol;
                                    if (symbol.equals("√")) {
                                        insertText = "sqrt(";
                                    } else if (symbol.equals("π")) {
                                        insertText = "pi";
                                    } else if (symbol.equals("sin") || symbol.equals("cos") || 
                                               symbol.equals("tan") || symbol.equals("log") || 
                                               symbol.equals("ln")) {
                                        insertText = symbol + "(";
                                    }

                                    // Insertar símbolo en la posición del cursor
                                    String newText = currentText.substring(0, caretPosition) + 
                                                    insertText + 
                                                    currentText.substring(caretPosition);

                                    // Actualizar texto
                                    txtFunction.setText(newText);

                                    // Mover cursor después del símbolo insertado
                                    txtFunction.positionCaret(caretPosition + insertText.length());

                                    // Dar foco al campo de texto
                                    txtFunction.requestFocus();

                                    // Actualizar renderizado LaTeX si está disponible
                                    updateLatexRender(newText);
                                } else {
                                    System.err.println("Error: txtFunction es null al intentar insertar símbolo " + symbol);
                                }
                            } catch (Exception e) {
                                System.err.println("Error al insertar símbolo " + symbol + ": " + e.getMessage());
                                e.printStackTrace();
                            }
                        });

                        // Añadir tooltip para ayudar al usuario
                        Tooltip tooltip = new Tooltip("Insertar " + getSymbolDescription(symbol));
                        Tooltip.install(btn, tooltip);

                        // Añadir botón al contenedor
                        symbolButtons.getChildren().add(btn);
                    } catch (Exception e) {
                        System.err.println("Error al crear botón para símbolo " + symbol + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                System.out.println("Configuración de botones de símbolos completada");
            } catch (Exception e) {
                System.err.println("Error al configurar botones de símbolos: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Obtiene una descripción para un símbolo matemático
     */
    private String getSymbolDescription(String symbol) {
        switch (symbol) {
            case "+": return "suma";
            case "-": return "resta";
            case "*": return "multiplicación";
            case "/": return "división";
            case "^": return "potencia";
            case "√": return "raíz cuadrada";
            case "π": return "pi (3.14159...)";
            case "e": return "número de Euler (2.71828...)";
            case "sin": return "seno";
            case "cos": return "coseno";
            case "tan": return "tangente";
            case "log": return "logaritmo base 10";
            case "ln": return "logaritmo natural";
            case "(": return "paréntesis izquierdo";
            case ")": return "paréntesis derecho";
            case "=": return "igual";
            case "x": return "variable x";
            default: return symbol;
        }
    }

    /**
     * Muestra una notificación animada con el mensaje especificado
     * @param message El mensaje a mostrar
     */
    private void showNotification(String message) {
        // Crear un panel para la notificación
        javafx.scene.layout.StackPane notificationPane = new javafx.scene.layout.StackPane();
        notificationPane.setStyle("-fx-background-color: rgba(74, 58, 155, 0.9); " +
                                 "-fx-background-radius: 10px; " +
                                 "-fx-padding: 15px; " +
                                 "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 10, 0.5, 0, 0);");
        notificationPane.setMaxWidth(300);
        notificationPane.setMaxHeight(100);

        // Crear el texto de la notificación
        javafx.scene.text.Text text = new javafx.scene.text.Text(message);
        text.setStyle("-fx-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        text.setWrappingWidth(270);

        // Añadir el texto al panel
        notificationPane.getChildren().add(text);

        // Posicionar la notificación en la parte superior del contenedor principal
        javafx.scene.layout.StackPane.setAlignment(notificationPane, javafx.geometry.Pos.TOP_CENTER);
        javafx.scene.layout.StackPane.setMargin(notificationPane, new javafx.geometry.Insets(20, 0, 0, 0));

        // Añadir la notificación al contenedor principal
        javafx.scene.layout.Pane rootPane = (javafx.scene.layout.Pane) chartContainer.getScene().getRoot();
        rootPane.getChildren().add(notificationPane);

        // Configurar animaciones

        // Animación de entrada: fade in + slide down
        javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(Duration.millis(300), notificationPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        javafx.animation.TranslateTransition slideIn = new javafx.animation.TranslateTransition(Duration.millis(300), notificationPane);
        slideIn.setFromY(-50);
        slideIn.setToY(0);

        // Animación de salida: fade out + slide up
        javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(Duration.millis(300), notificationPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        javafx.animation.TranslateTransition slideOut = new javafx.animation.TranslateTransition(Duration.millis(300), notificationPane);
        slideOut.setFromY(0);
        slideOut.setToY(-50);

        // Configurar secuencia de animaciones
        javafx.animation.SequentialTransition sequentialTransition = new javafx.animation.SequentialTransition();

        // Entrada
        javafx.animation.ParallelTransition parallelIn = new javafx.animation.ParallelTransition(fadeIn, slideIn);

        // Pausa
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(3));

        // Salida
        javafx.animation.ParallelTransition parallelOut = new javafx.animation.ParallelTransition(fadeOut, slideOut);

        // Añadir a la secuencia
        sequentialTransition.getChildren().addAll(parallelIn, pause, parallelOut);

        // Al finalizar, eliminar la notificación del contenedor
        sequentialTransition.setOnFinished(event -> rootPane.getChildren().remove(notificationPane));

        // Reproducir la secuencia
        sequentialTransition.play();
    }

    // Métodos de funcionalidad adicional
    private void findIntersection() {
        // Verificar que hay al menos dos funciones seleccionadas
        if (functions.size() < 2) {
            showNotification("Se necesitan al menos dos funciones para encontrar intersecciones");
            return;
        }

        // Si no hay función seleccionada, usar las dos primeras
        int selectedIndex = lstFunctions.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            selectedIndex = 0;
        }

        // Seleccionar la segunda función (diferente de la primera)
        int secondIndex = (selectedIndex + 1) % functions.size();

        String function1 = functions.get(selectedIndex);
        String function2 = functions.get(secondIndex);

        // Mostrar notificación de procesamiento
        showNotification("Buscando intersecciones entre f₁(x) = " + function1 + " y f₂(x) = " + function2);

        // Crear una función que represente la diferencia entre las dos funciones
        // La intersección ocurre cuando f1(x) - f2(x) = 0
        String diffFunction = "(" + function1 + ") - (" + function2 + ")";

        // Buscar raíces en el rango visible
        double step = (xMax - xMin) / 100.0;
        List<Double> intersections = new ArrayList<>();

        // Buscar cambios de signo (método simple para encontrar raíces)
        double prevY = evaluarFuncion(diffFunction, xMin);
        for (double x = xMin + step; x <= xMax; x += step) {
            double y = evaluarFuncion(diffFunction, x);

            // Si hay un cambio de signo, hay una raíz entre x-step y x
            if (prevY * y <= 0 && !Double.isNaN(prevY) && !Double.isNaN(y)) {
                // Refinar la raíz con bisección
                double root = findRootBisection(diffFunction, x - step, x, 1e-6, 20);
                if (!Double.isNaN(root)) {
                    intersections.add(root);
                }
            }

            prevY = y;
        }

        // Mostrar resultados
        if (intersections.isEmpty()) {
            showNotification("No se encontraron intersecciones en el rango visible");
        } else {
            // Añadir puntos de intersección al gráfico
            for (Double x : intersections) {
                double y = evaluarFuncion(function1, x);

                // Crear un dataset para el punto de intersección
                DoubleDataSet intersectionPoint = new DoubleDataSet("Intersección");
                intersectionPoint.add(x, y);

                // Configurar el renderer para mostrar el punto
                ErrorDataSetRenderer pointRenderer = new ErrorDataSetRenderer();
                pointRenderer.setDrawMarker(true);
                pointRenderer.setMarkerSize(10);
                pointRenderer.getDatasets().add(intersectionPoint);

                // Añadir el renderer al gráfico
                chart.getRenderers().add(pointRenderer);

                // Mostrar coordenadas
                showNotification(String.format("Intersección en (%.4f, %.4f)", x, y));
            }
        }
    }

    // Método auxiliar para encontrar raíces usando bisección
    private double findRootBisection(String function, double a, double b, double tolerance, int maxIterations) {
        double fa = evaluarFuncion(function, a);
        double fb = evaluarFuncion(function, b);

        // Verificar que hay un cambio de signo
        if (fa * fb > 0) {
            return Double.NaN;
        }

        double c = a;
        double fc;
        int iteration = 0;

        while ((b - a) > tolerance && iteration < maxIterations) {
            // Calcular punto medio
            c = (a + b) / 2;
            fc = evaluarFuncion(function, c);

            if (Math.abs(fc) < tolerance) {
                break;
            }

            // Ajustar intervalo
            if (fa * fc < 0) {
                b = c;
                fb = fc;
            } else {
                a = c;
                fa = fc;
            }

            iteration++;
        }

        return c;
    }

    private void calculateDerivative() {
        // Verificar que hay una función seleccionada
        int selectedIndex = lstFunctions.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= functions.size()) {
            showNotification("Seleccione una función para calcular su derivada");
            return;
        }

        String function = functions.get(selectedIndex);

        // Crear diálogo para opciones de derivada
        javafx.scene.control.Dialog<javafx.util.Pair<Integer, Boolean>> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Opciones de Derivada");
        dialog.setHeaderText("Configurar opciones para la derivada de f(x) = " + function);

        // Botones
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);

        // Crear layout
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        // Spinner para orden de derivada
        javafx.scene.control.Spinner<Integer> orderSpinner = new javafx.scene.control.Spinner<>(1, 5, 1);
        orderSpinner.setEditable(true);
        orderSpinner.getValueFactory().setWrapAround(false);

        // Checkbox para mostrar puntos críticos
        javafx.scene.control.CheckBox showCriticalPoints = new javafx.scene.control.CheckBox("Mostrar puntos críticos");
        showCriticalPoints.setSelected(true);

        // Añadir controles al grid
        grid.add(new javafx.scene.control.Label("Orden de la derivada:"), 0, 0);
        grid.add(orderSpinner, 1, 0);
        grid.add(showCriticalPoints, 0, 1, 2, 1);

        dialog.getDialogPane().setContent(grid);

        // Convertir resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == javafx.scene.control.ButtonType.OK) {
                return new javafx.util.Pair<>(orderSpinner.getValue(), showCriticalPoints.isSelected());
            }
            return null;
        });

        // Mostrar diálogo y procesar resultado
        dialog.showAndWait().ifPresent(result -> {
            int order = result.getKey();
            boolean findCriticalPoints = result.getValue();

            // Crear calculadora de derivadas
            DerivativeCalculator calculator = new DerivativeCalculator();

            try {
                // Variable para almacenar la función derivada actual
                String currentFunction = function;
                String derivative = "";

                // Calcular la derivada del orden especificado
                for (int i = 1; i <= order; i++) {
                    derivative = calculator.differentiate(currentFunction);
                    currentFunction = derivative;
                }

                // Crear nombre para la derivada según el orden
                String derivativeName;
                if (order == 1) {
                    derivativeName = "f'(x)";
                } else if (order == 2) {
                    derivativeName = "f''(x)";
                } else {
                    derivativeName = "f^(" + order + ")(x)";
                }

                // Mostrar la derivada
                showNotification("Derivada de orden " + order + " de f(x) = " + function + " es " + derivativeName + " = " + derivative);

                // Añadir la derivada a la lista de funciones
                functions.add(derivative);

                // Actualizar la lista visual
                updateFunctionsList();

                // Graficar todas las funciones incluyendo la nueva derivada
                graficarFunciones();

                // Seleccionar la nueva función (derivada)
                lstFunctions.getSelectionModel().select(functions.size() - 1);
                selectFunction(functions.size() - 1);

                // Si se solicitó, encontrar y mostrar puntos críticos
                if (findCriticalPoints) {
                    findCriticalPoints(derivative);
                }
            } catch (Exception e) {
                showNotification("Error al calcular la derivada: " + e.getMessage());
            }
        });
    }

    /**
     * Encuentra y muestra los puntos críticos de una función
     * @param function La función a analizar
     */
    private void findCriticalPoints(String function) {
        try {
            // Buscar puntos donde la derivada es cero (puntos críticos)
            List<Double> criticalPoints = new ArrayList<>();

            // Buscar en un rango razonable
            double searchMin = xMin - 10;
            double searchMax = xMax + 10;
            double step = (searchMax - searchMin) / 100;

            // Buscar cambios de signo en la función
            Double lastY = null;
            for (double x = searchMin; x <= searchMax; x += step) {
                try {
                    double y = evaluarFuncion(function, x);

                    if (lastY != null && ((lastY < 0 && y >= 0) || (lastY > 0 && y <= 0))) {
                        // Encontramos un cambio de signo, refinar con bisección
                        double criticalX = findRootBisection(function, x - step, x, 1e-6, 50);
                        if (!Double.isNaN(criticalX)) {
                            criticalPoints.add(criticalX);
                        }
                    }

                    lastY = y;
                } catch (Exception e) {
                    // Ignorar errores en la evaluación
                    lastY = null;
                }
            }

            // Mostrar puntos críticos encontrados
            if (!criticalPoints.isEmpty()) {
                StringBuilder message = new StringBuilder("Puntos críticos encontrados:\n");

                for (Double x : criticalPoints) {
                    // Evaluar la función original en el punto crítico
                    double y = evaluarFuncion(functions.get(lstFunctions.getSelectionModel().getSelectedIndex() - 1), x);
                    message.append(String.format("x = %.4f, f(x) = %.4f\n", x, y));

                    // Añadir un punto visual en el gráfico
                    DoubleDataSet pointDataSet = new DoubleDataSet("Punto crítico");
                    pointDataSet.add(x, y);
                    pointDataSet.setStyle("strokeColor=red;strokeWidth=0;pointSize=8");
                    chart.getDatasets().add(pointDataSet);
                }

                showNotification(message.toString());
            } else {
                showNotification("No se encontraron puntos críticos en el rango visible.");
            }
        } catch (Exception e) {
            showNotification("Error al buscar puntos críticos: " + e.getMessage());
        }
    }

    private void calculateIntegral() {
        // Verificar que hay una función seleccionada
        int selectedIndex = lstFunctions.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= functions.size()) {
            showNotification("Seleccione una función para calcular su integral");
            return;
        }

        String function = functions.get(selectedIndex);

        // Crear diálogo para opciones de integral
        javafx.scene.control.Dialog<javafx.util.Pair<Boolean, javafx.util.Pair<Double, Double>>> dialog = 
            new javafx.scene.control.Dialog<>();
        dialog.setTitle("Opciones de Integral");
        dialog.setHeaderText("Configurar opciones para la integral de f(x) = " + function);

        // Botones
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);

        // Crear layout
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        // Radio buttons para tipo de integral
        javafx.scene.control.ToggleGroup integralTypeGroup = new javafx.scene.control.ToggleGroup();
        javafx.scene.control.RadioButton indefiniteRadio = new javafx.scene.control.RadioButton("Integral Indefinida");
        indefiniteRadio.setToggleGroup(integralTypeGroup);
        indefiniteRadio.setSelected(true);

        javafx.scene.control.RadioButton definiteRadio = new javafx.scene.control.RadioButton("Integral Definida");
        definiteRadio.setToggleGroup(integralTypeGroup);

        // Campos para límites de integración
        javafx.scene.control.TextField lowerBoundField = new javafx.scene.control.TextField(String.valueOf(xMin));
        lowerBoundField.setPromptText("Límite inferior");
        lowerBoundField.setDisable(true);

        javafx.scene.control.TextField upperBoundField = new javafx.scene.control.TextField(String.valueOf(xMax));
        upperBoundField.setPromptText("Límite superior");
        upperBoundField.setDisable(true);

        // Checkbox para visualizar área
        javafx.scene.control.CheckBox visualizeAreaCheck = new javafx.scene.control.CheckBox("Visualizar área bajo la curva");
        visualizeAreaCheck.setSelected(true);
        visualizeAreaCheck.setDisable(true);

        // Habilitar/deshabilitar campos según el tipo de integral
        definiteRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            lowerBoundField.setDisable(!newVal);
            upperBoundField.setDisable(!newVal);
            visualizeAreaCheck.setDisable(!newVal);
        });

        // Añadir controles al grid
        grid.add(new javafx.scene.control.Label("Tipo de integral:"), 0, 0);
        grid.add(indefiniteRadio, 1, 0);
        grid.add(definiteRadio, 1, 1);

        grid.add(new javafx.scene.control.Label("Límite inferior:"), 0, 2);
        grid.add(lowerBoundField, 1, 2);

        grid.add(new javafx.scene.control.Label("Límite superior:"), 0, 3);
        grid.add(upperBoundField, 1, 3);

        grid.add(visualizeAreaCheck, 0, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);

        // Convertir resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == javafx.scene.control.ButtonType.OK) {
                boolean isDefinite = definiteRadio.isSelected();

                double lowerBound = xMin;
                double upperBound = xMax;

                if (isDefinite) {
                    try {
                        lowerBound = Double.parseDouble(lowerBoundField.getText());
                        upperBound = Double.parseDouble(upperBoundField.getText());
                    } catch (NumberFormatException e) {
                        // Usar valores predeterminados si hay error
                        lowerBound = xMin;
                        upperBound = xMax;
                    }
                }

                return new javafx.util.Pair<>(isDefinite, new javafx.util.Pair<>(lowerBound, upperBound));
            }
            return null;
        });

        // Mostrar diálogo y procesar resultado
        dialog.showAndWait().ifPresent(result -> {
            boolean isDefinite = result.getKey();
            double lowerBound = result.getValue().getKey();
            double upperBound = result.getValue().getValue();

            // Crear calculadora de integrales
            IntegralCalculator calculator = new IntegralCalculator();

            try {
                if (isDefinite) {
                    // Calcular la integral definida
                    double integralValue = calculator.evaluateDefiniteIntegral(function, lowerBound, upperBound);

                    // Mostrar el resultado
                    showNotification(String.format(
                        "Integral definida de f(x) = %s desde x = %.2f hasta x = %.2f es: %.6f",
                        function, lowerBound, upperBound, integralValue));

                    // Visualizar el área si se solicitó
                    if (visualizeAreaCheck.isSelected()) {
                        visualizeAreaUnderCurve(function, lowerBound, upperBound);
                    }
                } else {
                    // Calcular la integral indefinida
                    String integral = calculator.integrate(function);

                    // Mostrar la integral
                    showNotification("Integral indefinida de f(x) = " + function + " es ∫f(x)dx = " + integral + " + C");

                    // Añadir la integral a la lista de funciones
                    functions.add(integral);

                    // Actualizar la lista visual
                    updateFunctionsList();

                    // Graficar todas las funciones incluyendo la nueva integral
                    graficarFunciones();

                    // Seleccionar la nueva función (integral)
                    lstFunctions.getSelectionModel().select(functions.size() - 1);
                    selectFunction(functions.size() - 1);
                }
            } catch (Exception e) {
                showNotification("Error al calcular la integral: " + e.getMessage());
            }
        });
    }

    /**
     * Visualiza el área bajo la curva entre dos puntos
     * @param function La función a integrar
     * @param lowerBound Límite inferior
     * @param upperBound Límite superior
     */
    private void visualizeAreaUnderCurve(String function, double lowerBound, double upperBound) {
        try {
            // Crear un conjunto de datos para el área
            DoubleDataSet areaDataSet = new DoubleDataSet("Área bajo la curva");

            // Número de puntos para la visualización
            int numPoints = 200;
            double step = (upperBound - lowerBound) / numPoints;

            // Añadir puntos para el polígono del área
            // Primero añadir el punto inferior izquierdo
            areaDataSet.add(lowerBound, 0);

            // Añadir puntos a lo largo de la curva
            for (double x = lowerBound; x <= upperBound; x += step) {
                double y = evaluarFuncion(function, x);
                if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                    areaDataSet.add(x, y);
                }
            }

            // Añadir el punto inferior derecho para cerrar el polígono
            areaDataSet.add(upperBound, 0);

            // Configurar estilo para el área
            String color = COLORS[functions.indexOf(function) % COLORS.length];
            // Convertir color a formato RGBA con transparencia
            String fillColor = color.replace("#", "rgba(") + ", 0.3)";
            areaDataSet.setStyle("strokeColor=" + color + ";fillColor=" + fillColor + ";strokeWidth=1.5;areaStyle=filled");

            // Añadir el conjunto de datos al gráfico
            chart.getDatasets().add(areaDataSet);

            // Calcular el valor de la integral para mostrar en la etiqueta
            IntegralCalculator calculator = new IntegralCalculator();
            double integralValue = calculator.evaluateDefiniteIntegral(function, lowerBound, upperBound);

            // Añadir una etiqueta con el valor del área
            double labelX = lowerBound + (upperBound - lowerBound) / 2;
            double labelY = evaluarFuncion(function, labelX) / 2;

            // Crear un conjunto de datos para la etiqueta
            DoubleDataSet labelDataSet = new DoubleDataSet("Valor del área");
            labelDataSet.add(labelX, labelY);
            labelDataSet.setStyle("strokeColor=transparent;pointSize=0");

            // Añadir metadatos para mostrar el valor en el tooltip
            labelDataSet.getDataLabelMap().put(0, String.format("Área = %.4f", integralValue));

            chart.getDatasets().add(labelDataSet);

        } catch (Exception e) {
            showNotification("Error al visualizar el área: " + e.getMessage());
        }
    }

    private void showTable() {
        // Verificar que hay una función seleccionada
        int selectedIndex = lstFunctions.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= functions.size()) {
            showNotification("Seleccione una función para mostrar su tabla de valores");
            return;
        }

        String function = functions.get(selectedIndex);

        // Crear una ventana para mostrar la tabla
        Stage tableStage = new Stage();
        tableStage.setTitle("Tabla de valores para f(x) = " + function);

        // Crear tabla
        javafx.scene.control.TableView<javafx.util.Pair<Double, Double>> table = 
            new javafx.scene.control.TableView<>();

        // Columna para x
        javafx.scene.control.TableColumn<javafx.util.Pair<Double, Double>, Double> xColumn = 
            new javafx.scene.control.TableColumn<>("x");
        xColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleDoubleProperty(data.getValue().getKey()).asObject());

        // Columna para f(x)
        javafx.scene.control.TableColumn<javafx.util.Pair<Double, Double>, Double> fxColumn = 
            new javafx.scene.control.TableColumn<>("f(x)");
        fxColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleDoubleProperty(data.getValue().getValue()).asObject());

        // Añadir columnas a la tabla
        table.getColumns().add(xColumn);
        table.getColumns().add(fxColumn);

        // Calcular valores para la tabla
        javafx.collections.ObservableList<javafx.util.Pair<Double, Double>> data = 
            javafx.collections.FXCollections.observableArrayList();

        double step = (xMax - xMin) / 20.0;
        for (double x = xMin; x <= xMax; x += step) {
            double y = evaluarFuncion(function, x);
            data.add(new javafx.util.Pair<>(x, y));
        }

        // Añadir datos a la tabla
        table.setItems(data);

        // Crear layout
        VBox layout = new VBox(10);
        layout.setPadding(new javafx.geometry.Insets(10));
        layout.getChildren().add(table);

        // Configurar escena
        Scene scene = new Scene(layout, 300, 400);
        tableStage.setScene(scene);

        // Mostrar ventana
        tableStage.show();
    }

    private void addPoint() {
        // Crear un diálogo para ingresar las coordenadas del punto
        javafx.scene.control.Dialog<javafx.util.Pair<Double, Double>> dialog = 
            new javafx.scene.control.Dialog<>();
        dialog.setTitle("Añadir Punto");
        dialog.setHeaderText("Ingrese las coordenadas del punto");

        // Configurar botones
        dialog.getDialogPane().getButtonTypes().addAll(
            javafx.scene.control.ButtonType.OK, 
            javafx.scene.control.ButtonType.CANCEL
        );

        // Crear campos para las coordenadas
        javafx.scene.control.TextField xField = new javafx.scene.control.TextField();
        xField.setPromptText("Coordenada X");
        javafx.scene.control.TextField yField = new javafx.scene.control.TextField();
        yField.setPromptText("Coordenada Y");

        // Crear layout
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        grid.add(new javafx.scene.control.Label("X:"), 0, 0);
        grid.add(xField, 1, 0);
        grid.add(new javafx.scene.control.Label("Y:"), 0, 1);
        grid.add(yField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Convertir resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == javafx.scene.control.ButtonType.OK) {
                try {
                    double x = Double.parseDouble(xField.getText());
                    double y = Double.parseDouble(yField.getText());
                    return new javafx.util.Pair<>(x, y);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        // Mostrar diálogo y procesar resultado
        dialog.showAndWait().ifPresent(point -> {
            // Crear un dataset para el punto
            DoubleDataSet pointDataset = new DoubleDataSet("Punto (" + point.getKey() + ", " + point.getValue() + ")");
            pointDataset.add(point.getKey(), point.getValue());

            // Configurar el renderer para mostrar el punto
            ErrorDataSetRenderer pointRenderer = new ErrorDataSetRenderer();
            pointRenderer.setDrawMarker(true);
            pointRenderer.setMarkerSize(10);
            pointRenderer.getDatasets().add(pointDataset);

            // Añadir el renderer al gráfico
            chart.getRenderers().add(pointRenderer);

            showNotification("Punto añadido en (" + point.getKey() + ", " + point.getValue() + ")");
        });
    }

    private void showSettings() {
        // Crear un diálogo para configuración avanzada
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Configuración Avanzada");
        dialog.setHeaderText("Ajustes de la graficadora");

        // Configurar botones
        dialog.getDialogPane().getButtonTypes().addAll(
            javafx.scene.control.ButtonType.OK, 
            javafx.scene.control.ButtonType.CANCEL
        );

        // Crear controles para ajustes
        javafx.scene.control.CheckBox gridCheckBox = new javafx.scene.control.CheckBox("Mostrar cuadrícula");
        gridCheckBox.setSelected(chkShowGrid.isSelected());

        javafx.scene.control.CheckBox infiniteRangeCheckBox = new javafx.scene.control.CheckBox("Rango infinito");
        infiniteRangeCheckBox.setSelected(chkInfiniteRange.isSelected());

        javafx.scene.control.Slider pointsSlider = new javafx.scene.control.Slider(100, 1000, 500);
        pointsSlider.setShowTickLabels(true);
        pointsSlider.setShowTickMarks(true);
        pointsSlider.setMajorTickUnit(100);

        javafx.scene.control.ColorPicker backgroundColorPicker = new javafx.scene.control.ColorPicker();
        backgroundColorPicker.setValue(javafx.scene.paint.Color.web("#0A0629"));

        // Crear layout
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.setPadding(new javafx.geometry.Insets(20));
        content.getChildren().addAll(
            new javafx.scene.control.Label("Opciones de visualización:"),
            gridCheckBox,
            infiniteRangeCheckBox,
            new javafx.scene.control.Label("Puntos por función:"),
            pointsSlider,
            new javafx.scene.control.Label("Color de fondo:"),
            backgroundColorPicker
        );

        dialog.getDialogPane().setContent(content);

        // Manejar resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == javafx.scene.control.ButtonType.OK) {
                // Aplicar configuración
                chkShowGrid.setSelected(gridCheckBox.isSelected());
                toggleGridLines(gridCheckBox.isSelected());

                chkInfiniteRange.setSelected(infiniteRangeCheckBox.isSelected());
                toggleInfiniteRangeMode(infiniteRangeCheckBox.isSelected());

                // Actualizar número de puntos por función
                numPoints = (int) pointsSlider.getValue();

                // Actualizar color de fondo
                String colorHex = String.format("#%02X%02X%02X",
                    (int) (backgroundColorPicker.getValue().getRed() * 255),
                    (int) (backgroundColorPicker.getValue().getGreen() * 255),
                    (int) (backgroundColorPicker.getValue().getBlue() * 255));
                chartContainer.setStyle("-fx-background-color: " + colorHex + ";");

                // Volver a graficar con la nueva configuración
                graficarFunciones();

                showNotification("Configuración aplicada");
            }
            return null;
        });

        // Mostrar diálogo
        dialog.showAndWait();
    }

    // Método auxiliar para actualizar la lista de funciones en la UI
    private void updateFunctionsList() {
        javafx.collections.ObservableList<String> items = javafx.collections.FXCollections.observableArrayList();
        for (int i = 0; i < functions.size(); i++) {
            items.add("f" + (i+1) + "(x) = " + functions.get(i));
        }
        lstFunctions.setItems(items);
    }

    /**
     * Analiza la función seleccionada para encontrar características importantes
     */
    private void analyzeFunction() {
        // Verificar que hay una función seleccionada
        int selectedIndex = lstFunctions.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= functions.size()) {
            showNotification("Seleccione una función para analizar");
            return;
        }

        String function = functions.get(selectedIndex);

        // Crear ventana de análisis
        Stage analysisStage = new Stage();
        analysisStage.setTitle("Análisis de función: f(x) = " + function);
        analysisStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        analysisStage.initOwner(chart.getScene().getWindow());

        // Crear layout
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(10);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: #0A0629;");

        // Título
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("Análisis de f(x) = " + function);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Crear TabPane para organizar el análisis
        javafx.scene.control.TabPane tabPane = new javafx.scene.control.TabPane();
        tabPane.setTabClosingPolicy(javafx.scene.control.TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab para información general
        javafx.scene.control.Tab generalTab = new javafx.scene.control.Tab("General");
        javafx.scene.layout.VBox generalContent = new javafx.scene.layout.VBox(10);
        generalContent.setPadding(new javafx.geometry.Insets(10));
        generalContent.setStyle("-fx-background-color: #1A1640;");

        // Buscar dominio, rango, etc.
        try {
            // Verificar si es una función racional (tiene divisiones)
            boolean isRational = function.contains("/");

            // Texto para el dominio
            String domainText = "Dominio: ";
            if (isRational) {
                domainText += "ℝ excepto donde el denominador es cero";
            } else {
                domainText += "ℝ (todos los números reales)";
            }

            // Verificar si es una función par, impar o ninguna
            String symmetryText = "Simetría: ";
            try {
                // Evaluar f(-x) y comparar con f(x) o -f(x)
                String minusXExpr = function.replaceAll("(?<![a-zA-Z0-9_])x(?![a-zA-Z0-9_])", "(-x)");

                // Evaluar en algunos puntos de prueba
                boolean mightBeEven = true;
                boolean mightBeOdd = true;

                for (double testX : new double[]{1.0, 2.0, 3.14, 5.0}) {
                    if (testX != 0) { // Evitar x=0 donde par e impar son indistinguibles
                        double fx = evaluarFuncion(function, testX);
                        double fMinusX = evaluarFuncion(minusXExpr, testX); // Realmente evaluando f(-x)

                        // Comprobar si f(-x) = f(x) (función par)
                        if (Math.abs(fMinusX - fx) > 1e-10) {
                            mightBeEven = false;
                        }

                        // Comprobar si f(-x) = -f(x) (función impar)
                        if (Math.abs(fMinusX + fx) > 1e-10) {
                            mightBeOdd = false;
                        }

                        // Si ya sabemos que no es ni par ni impar, salir del bucle
                        if (!mightBeEven && !mightBeOdd) {
                            break;
                        }
                    }
                }

                if (mightBeEven) {
                    symmetryText += "Función par (simétrica respecto al eje Y)";
                } else if (mightBeOdd) {
                    symmetryText += "Función impar (simétrica respecto al origen)";
                } else {
                    symmetryText += "No tiene simetría par ni impar";
                }
            } catch (Exception e) {
                symmetryText += "No se pudo determinar";
            }

            // Buscar intersecciones con los ejes
            List<Double> xIntercepts = findRoots(function, xMin, xMax);
            String xInterceptsText = "Intersecciones con eje X: ";
            if (xIntercepts.isEmpty()) {
                xInterceptsText += "No se encontraron en el rango visible";
            } else {
                xInterceptsText += xIntercepts.stream()
                    .map(x -> String.format("(%.4f, 0)", x))
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            }

            // Intersección con eje Y (evaluar en x=0)
            String yInterceptText = "Intersección con eje Y: ";
            try {
                double y0 = evaluarFuncion(function, 0);
                yInterceptText += String.format("(0, %.4f)", y0);
            } catch (Exception e) {
                yInterceptText += "No definida en x=0";
            }

            // Añadir información al panel general
            generalContent.getChildren().addAll(
                createInfoLabel(domainText),
                createInfoLabel(symmetryText),
                createInfoLabel(xInterceptsText),
                createInfoLabel(yInterceptText)
            );

        } catch (Exception e) {
            generalContent.getChildren().add(
                createInfoLabel("Error al analizar la función: " + e.getMessage())
            );
        }

        generalTab.setContent(generalContent);

        // Tab para derivadas
        javafx.scene.control.Tab derivativesTab = new javafx.scene.control.Tab("Derivadas");
        javafx.scene.layout.VBox derivativesContent = new javafx.scene.layout.VBox(10);
        derivativesContent.setPadding(new javafx.geometry.Insets(10));
        derivativesContent.setStyle("-fx-background-color: #1A1640;");

        try {
            DerivativeCalculator calculator = new DerivativeCalculator();

            // Primera derivada
            String firstDerivative = calculator.differentiate(function);
            derivativesContent.getChildren().add(
                createInfoLabel("Primera derivada: f'(x) = " + firstDerivative)
            );

            // Segunda derivada
            String secondDerivative = calculator.differentiate(firstDerivative);
            derivativesContent.getChildren().add(
                createInfoLabel("Segunda derivada: f''(x) = " + secondDerivative)
            );

            // Puntos críticos (donde f'(x) = 0)
            List<Double> criticalPoints = findRoots(firstDerivative, xMin, xMax);

            if (!criticalPoints.isEmpty()) {
                javafx.scene.layout.VBox criticalPointsBox = new javafx.scene.layout.VBox(5);
                criticalPointsBox.getChildren().add(
                    createInfoLabel("Puntos críticos (f'(x) = 0):")
                );

                for (Double x : criticalPoints) {
                    double fx = evaluarFuncion(function, x);
                    double f2x = evaluarFuncion(secondDerivative, x);

                    String pointType;
                    if (f2x < 0) {
                        pointType = "Máximo local";
                    } else if (f2x > 0) {
                        pointType = "Mínimo local";
                    } else {
                        pointType = "Punto de inflexión o indeterminado";
                    }

                    criticalPointsBox.getChildren().add(
                        createInfoLabel(String.format("   x = %.4f, f(x) = %.4f - %s", x, fx, pointType))
                    );
                }

                derivativesContent.getChildren().add(criticalPointsBox);
            } else {
                derivativesContent.getChildren().add(
                    createInfoLabel("No se encontraron puntos críticos en el rango visible")
                );
            }

            // Puntos de inflexión (donde f''(x) = 0)
            List<Double> inflectionPoints = findRoots(secondDerivative, xMin, xMax);

            if (!inflectionPoints.isEmpty()) {
                javafx.scene.layout.VBox inflectionPointsBox = new javafx.scene.layout.VBox(5);
                inflectionPointsBox.getChildren().add(
                    createInfoLabel("Puntos de inflexión (f''(x) = 0):")
                );

                for (Double x : inflectionPoints) {
                    double fx = evaluarFuncion(function, x);

                    inflectionPointsBox.getChildren().add(
                        createInfoLabel(String.format("   x = %.4f, f(x) = %.4f", x, fx))
                    );
                }

                derivativesContent.getChildren().add(inflectionPointsBox);
            } else {
                derivativesContent.getChildren().add(
                    createInfoLabel("No se encontraron puntos de inflexión en el rango visible")
                );
            }

        } catch (Exception e) {
            derivativesContent.getChildren().add(
                createInfoLabel("Error al calcular derivadas: " + e.getMessage())
            );
        }

        derivativesTab.setContent(derivativesContent);

        // Tab para integrales
        javafx.scene.control.Tab integralsTab = new javafx.scene.control.Tab("Integrales");
        javafx.scene.layout.VBox integralsContent = new javafx.scene.layout.VBox(10);
        integralsContent.setPadding(new javafx.geometry.Insets(10));
        integralsContent.setStyle("-fx-background-color: #1A1640;");

        try {
            IntegralCalculator calculator = new IntegralCalculator();

            // Integral indefinida
            String indefiniteIntegral = calculator.integrate(function);
            integralsContent.getChildren().add(
                createInfoLabel("Integral indefinida: ∫f(x)dx = " + indefiniteIntegral + " + C")
            );

            // Integral definida en el rango visible
            double definiteIntegral = calculator.evaluateDefiniteIntegral(function, xMin, xMax);
            integralsContent.getChildren().add(
                createInfoLabel(String.format(
                    "Integral definida en [%.2f, %.2f]: %.6f", 
                    xMin, xMax, definiteIntegral))
            );

            // Añadir controles para calcular integrales definidas personalizadas
            javafx.scene.layout.HBox customIntegralBox = new javafx.scene.layout.HBox(10);

            javafx.scene.control.TextField lowerBoundField = new javafx.scene.control.TextField(String.valueOf(xMin));
            lowerBoundField.setPromptText("Límite inferior");
            lowerBoundField.setPrefWidth(100);

            javafx.scene.control.TextField upperBoundField = new javafx.scene.control.TextField(String.valueOf(xMax));
            upperBoundField.setPromptText("Límite superior");
            upperBoundField.setPrefWidth(100);

            javafx.scene.control.Button calculateButton = new javafx.scene.control.Button("Calcular");

            javafx.scene.control.Label resultLabel = new javafx.scene.control.Label("Resultado: ");
            resultLabel.setStyle("-fx-text-fill: white;");

            calculateButton.setOnAction(e -> {
                try {
                    double lower = Double.parseDouble(lowerBoundField.getText());
                    double upper = Double.parseDouble(upperBoundField.getText());

                    double result = calculator.evaluateDefiniteIntegral(function, lower, upper);
                    resultLabel.setText(String.format("Resultado: %.6f", result));
                } catch (Exception ex) {
                    resultLabel.setText("Error: " + ex.getMessage());
                }
            });

            customIntegralBox.getChildren().addAll(
                new javafx.scene.control.Label("Desde:"), lowerBoundField,
                new javafx.scene.control.Label("Hasta:"), upperBoundField,
                calculateButton
            );

            integralsContent.getChildren().addAll(
                new javafx.scene.control.Label("Calcular integral definida personalizada:"),
                customIntegralBox,
                resultLabel
            );

        } catch (Exception e) {
            integralsContent.getChildren().add(
                createInfoLabel("Error al calcular integrales: " + e.getMessage())
            );
        }

        integralsTab.setContent(integralsContent);

        // Añadir tabs al TabPane
        tabPane.getTabs().addAll(generalTab, derivativesTab, integralsTab);

        // Botón para cerrar
        javafx.scene.control.Button closeButton = new javafx.scene.control.Button("Cerrar");
        closeButton.setOnAction(e -> analysisStage.close());

        // Añadir componentes al layout principal
        root.getChildren().addAll(titleLabel, tabPane, closeButton);

        // Configurar y mostrar la ventana
        Scene scene = new Scene(root, 600, 500);
        analysisStage.setScene(scene);
        analysisStage.show();
    }

    /**
     * Crea una etiqueta de información con estilo predefinido
     */
    private javafx.scene.control.Label createInfoLabel(String text) {
        javafx.scene.control.Label label = new javafx.scene.control.Label(text);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        label.setWrapText(true);
        return label;
    }

    /**
     * Encuentra las raíces de una función en un intervalo
     */
    private List<Double> findRoots(String function, double min, double max) {
        List<Double> roots = new ArrayList<>();

        // Número de subdivisiones para la búsqueda inicial
        int divisions = 200;
        double step = (max - min) / divisions;

        // Buscar cambios de signo
        Double lastY = null;
        for (double x = min; x <= max; x += step) {
            try {
                double y = evaluarFuncion(function, x);

                if (lastY != null) {
                    // Si hay un cambio de signo o un cruce por cero
                    if ((lastY < 0 && y >= 0) || (lastY > 0 && y <= 0) || 
                        (Math.abs(y) < 1e-10)) {

                        // Si y es muy cercano a cero, considerarlo una raíz directamente
                        if (Math.abs(y) < 1e-10) {
                            roots.add(x);
                        } else {
                            // Refinar con bisección
                            double root = findRootBisection(function, x - step, x, 1e-6, 50);
                            if (!Double.isNaN(root)) {
                                roots.add(root);
                            }
                        }
                    }
                }

                lastY = y;
            } catch (Exception e) {
                // Ignorar errores en la evaluación
                lastY = null;
            }
        }

        return roots;
    }

    /**
     * Cambia el color de la función seleccionada
     */
    private void changeSelectedFunctionColor() {
        // Verificar que hay una función seleccionada
        int selectedIndex = lstFunctions.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= functions.size()) {
            showNotification("Seleccione una función para cambiar su color");
            return;
        }

        String function = functions.get(selectedIndex);

        // Crear selector de color
        javafx.scene.control.ColorPicker colorPicker = new javafx.scene.control.ColorPicker();

        // Obtener el color actual si es posible
        try {
            DoubleDataSet dataSet = dataSets.get(function);
            if (dataSet != null) {
                String style = dataSet.getStyle();
                if (style != null && style.contains("strokeColor=")) {
                    String colorStr = style.replaceAll(".*strokeColor=([^;]*).*", "$1");
                    try {
                        colorPicker.setValue(javafx.scene.paint.Color.web(colorStr));
                    } catch (Exception e) {
                        // Si no se puede parsear el color, usar uno predeterminado
                        colorPicker.setValue(javafx.scene.paint.Color.web(COLORS[selectedIndex % COLORS.length]));
                    }
                }
            }
        } catch (Exception e) {
            // Usar color predeterminado si hay error
            colorPicker.setValue(javafx.scene.paint.Color.web(COLORS[selectedIndex % COLORS.length]));
        }

        // Crear diálogo
        javafx.scene.control.Dialog<javafx.scene.paint.Color> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Cambiar color de función");
        dialog.setHeaderText("Seleccione un nuevo color para f(x) = " + function);

        // Botones
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);

        // Contenido
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.setPadding(new javafx.geometry.Insets(20));
        content.getChildren().add(colorPicker);

        dialog.getDialogPane().setContent(content);

        // Convertir resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == javafx.scene.control.ButtonType.OK) {
                return colorPicker.getValue();
            }
            return null;
        });

        // Mostrar diálogo y procesar resultado
        dialog.showAndWait().ifPresent(color -> {
            // Convertir color a formato hexadecimal
            String colorHex = String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));

            // Actualizar el color de la función
            DoubleDataSet dataSet = dataSets.get(function);
            if (dataSet != null) {
                String currentStyle = dataSet.getStyle();
                String newStyle;

                if (currentStyle != null && !currentStyle.isEmpty()) {
                    // Reemplazar el color en el estilo existente
                    newStyle = currentStyle.replaceAll("strokeColor=[^;]*", "strokeColor=" + colorHex);
                } else {
                    // Crear un nuevo estilo
                    newStyle = "strokeColor=" + colorHex + ";strokeWidth=2";
                }

                dataSet.setStyle(newStyle);

                // Actualizar la selección para mostrar el nuevo color
                if (selectedFunctionIndex == selectedIndex) {
                    highlightSelectedSeries(selectedIndex);
                }
            }
        });
    }

    /**
     * Cambia el estilo de la función seleccionada
     */
    private void changeSelectedFunctionStyle() {
        // Verificar que hay una función seleccionada
        int selectedIndex = lstFunctions.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= functions.size()) {
            showNotification("Seleccione una función para cambiar su estilo");
            return;
        }

        String function = functions.get(selectedIndex);

        // Crear diálogo
        javafx.scene.control.Dialog<javafx.util.Pair<Double, String>> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Cambiar estilo de función");
        dialog.setHeaderText("Configure el estilo para f(x) = " + function);

        // Botones
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);

        // Crear layout
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        // Spinner para grosor de línea
        javafx.scene.control.Spinner<Double> lineWidthSpinner = new javafx.scene.control.Spinner<>(0.5, 5.0, 2.0, 0.5);
        lineWidthSpinner.setEditable(true);

        // ComboBox para estilo de línea
        javafx.scene.control.ComboBox<String> lineStyleCombo = new javafx.scene.control.ComboBox<>();
        lineStyleCombo.getItems().addAll("Continua", "Discontinua", "Punteada", "Guiones largos");
        lineStyleCombo.setValue("Continua");

        // Obtener el estilo actual si es posible
        try {
            DoubleDataSet dataSet = dataSets.get(function);
            if (dataSet != null) {
                String style = dataSet.getStyle();
                if (style != null) {
                    // Extraer grosor de línea
                    if (style.contains("strokeWidth=")) {
                        String widthStr = style.replaceAll(".*strokeWidth=([^;]*).*", "$1");
                        try {
                            double width = Double.parseDouble(widthStr);
                            lineWidthSpinner.getValueFactory().setValue(width);
                        } catch (Exception e) {
                            // Ignorar errores
                        }
                    }

                    // Extraer estilo de línea
                    if (style.contains("strokeDashArray=")) {
                        String dashStr = style.replaceAll(".*strokeDashArray=([^;]*).*", "$1");
                        if (dashStr.equals("5,5")) {
                            lineStyleCombo.setValue("Discontinua");
                        } else if (dashStr.equals("2,2")) {
                            lineStyleCombo.setValue("Punteada");
                        } else if (dashStr.equals("10,5")) {
                            lineStyleCombo.setValue("Guiones largos");
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignorar errores
        }

        // Añadir controles al grid
        grid.add(new javafx.scene.control.Label("Grosor de línea:"), 0, 0);
        grid.add(lineWidthSpinner, 1, 0);
        grid.add(new javafx.scene.control.Label("Estilo de línea:"), 0, 1);
        grid.add(lineStyleCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Convertir resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == javafx.scene.control.ButtonType.OK) {
                return new javafx.util.Pair<>(lineWidthSpinner.getValue(), lineStyleCombo.getValue());
            }
            return null;
        });

        // Mostrar diálogo y procesar resultado
        dialog.showAndWait().ifPresent(result -> {
            double lineWidth = result.getKey();
            String lineStyle = result.getValue();

            // Determinar el patrón de guiones según el estilo seleccionado
            String dashPattern = "";
            if (lineStyle.equals("Discontinua")) {
                dashPattern = "strokeDashArray=5,5;";
            } else if (lineStyle.equals("Punteada")) {
                dashPattern = "strokeDashArray=2,2;";
            } else if (lineStyle.equals("Guiones largos")) {
                dashPattern = "strokeDashArray=10,5;";
            }

            // Actualizar el estilo de la función
            DoubleDataSet dataSet = dataSets.get(function);
            if (dataSet != null) {
                String currentStyle = dataSet.getStyle();
                String newStyle;

                if (currentStyle != null && !currentStyle.isEmpty()) {
                    // Actualizar grosor de línea
                    newStyle = currentStyle.replaceAll("strokeWidth=[^;]*", "strokeWidth=" + lineWidth);

                    // Actualizar o añadir patrón de guiones
                    if (dashPattern.isEmpty()) {
                        // Eliminar patrón de guiones si existe
                        newStyle = newStyle.replaceAll("strokeDashArray=[^;]*;", "");
                    } else if (newStyle.contains("strokeDashArray=")) {
                        // Reemplazar patrón existente
                        newStyle = newStyle.replaceAll("strokeDashArray=[^;]*", dashPattern.replace(";", ""));
                    } else {
                        // Añadir nuevo patrón
                        newStyle += dashPattern;
                    }
                } else {
                    // Crear un nuevo estilo
                    String color = COLORS[selectedIndex % COLORS.length];
                    newStyle = "strokeColor=" + color + ";strokeWidth=" + lineWidth + ";" + dashPattern;
                }

                dataSet.setStyle(newStyle);

                // Actualizar la selección para mostrar el nuevo estilo
                if (selectedFunctionIndex == selectedIndex) {
                    highlightSelectedSeries(selectedIndex);
                }
            }
        });
    }

    // Métodos para navegación (mantener los existentes)
    @FXML
    private void btnVMain() {
        // Código existente para volver al menú principal
    }

    @FXML
    private void btnExit() {
        // Código existente para salir
    }
}
