package controllers;

import com.example.prueba.ConvertidorLatex;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import application.App;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import javafx.embed.swing.SwingNode;
import javafx.scene.control.Button;
import utils.Paths;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class CtrlMenu extends TopBarController {
    @FXML private VBox menuContent;
    @FXML private TextField txtFunction;
    @FXML private SwingNode latexRender;
    @FXML private HBox symbolButtons;

    // Nuevos botones añadidos al menú mejorado
    @FXML private Button btnHelp;
    @FXML private Button btnMetodosNumericos;
    @FXML private Button btnBiseccion;
    @FXML private Button btnPuntoFijo;
    @FXML private Button btnGraficadora;

    @FXML
    void initialize() {
        initializeTopBar();

        // Añadir clase para animación de aparición
        if (menuContent != null) {
            menuContent.getStyleClass().add("menu-content");
        }

        setupLatexRender();

        // Agregar listener para actualizar el renderizado al escribir
        txtFunction.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Texto cambiado a: " + newValue); // Depuración
            updateLatexRender(newValue); // Actualizar renderizado en tiempo real
            Platform.runLater(() -> {
                txtFunction.requestFocus(); // Mantener foco
                txtFunction.positionCaret(newValue.length()); // Mover cursor al final
            });
        });

        // Forzar foco periódicamente mientras el TextField esté visible
        Timeline focusTimeline = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            if (txtFunction.getScene() != null && txtFunction.getScene().getWindow() != null) {
                if (!txtFunction.isFocused()) {
                    txtFunction.requestFocus();
                    txtFunction.positionCaret(txtFunction.getText().length());
                }
            }
        }));
        focusTimeline.setCycleCount(Timeline.INDEFINITE);
        focusTimeline.play();

        // Añadir botones de símbolos
        setupSymbolButtons();

        // Configurar los nuevos botones
        setupNewButtons();
    }

    /**
     * Configura los nuevos botones añadidos al menú mejorado
     */
    private void setupNewButtons() {
        // Botón de ayuda
        if (btnHelp != null) {
            btnHelp.setOnAction(event -> showHelpDialog());
        }

        // Botón de métodos numéricos
        if (btnMetodosNumericos != null) {
            btnMetodosNumericos.setOnAction(event -> showNumericalMethodsMenu());
        }

        // Botón de método de bisección
        if (btnBiseccion != null) {
            btnBiseccion.setOnAction(event -> {
                try {
                    App.app.setScene(Paths.METODO_BIS);
                } catch (Exception e) {
                    showErrorDialog("Error al cargar el método de bisección", e.getMessage());
                }
            });
        }

        // Botón de punto fijo
        if (btnPuntoFijo != null) {
            btnPuntoFijo.setOnAction(event -> {
                try {
                    App.app.setScene(Paths.METODO_PTO_FIJO);
                } catch (Exception e) {
                    showErrorDialog("Error al cargar el método de punto fijo", e.getMessage());
                }
            });
        }

        // Botón de graficadora
        if (btnGraficadora != null) {
            btnGraficadora.setOnAction(event -> {
                try {
                    App.app.setScene(Paths.GRAFICA);
                } catch (Exception e) {
                    showErrorDialog("Error al cargar la graficadora", e.getMessage());
                }
            });
        }
    }

    /**
     * Muestra un diálogo de ayuda con información sobre la aplicación
     */
    private void showHelpDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ayuda - Métodos Numéricos");
        alert.setHeaderText("Guía de uso de la aplicación");

        VBox content = new VBox(10);
        content.setStyle("-fx-background-color: #0A0629; -fx-padding: 10px;");

        Label introLabel = new Label("Esta aplicación te permite trabajar con funciones matemáticas y aplicar diversos métodos numéricos.");
        introLabel.setWrapText(true);
        introLabel.setTextAlignment(TextAlignment.JUSTIFY);
        introLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Label functionsLabel = new Label("Funciones disponibles:");
        functionsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label graphingLabel = new Label("• Graficación de funciones: Visualiza funciones matemáticas y analiza sus propiedades.");
        graphingLabel.setWrapText(true);
        graphingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Label bisectionLabel = new Label("• Método de Bisección: Encuentra raíces de funciones dividiendo intervalos sucesivamente.");
        bisectionLabel.setWrapText(true);
        bisectionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Label fixedPointLabel = new Label("• Método de Punto Fijo: Resuelve ecuaciones mediante iteraciones sucesivas.");
        fixedPointLabel.setWrapText(true);
        fixedPointLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Label tipsLabel = new Label("Consejos:");
        tipsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label tip1Label = new Label("• Usa los botones de símbolos matemáticos para insertar funciones complejas.");
        tip1Label.setWrapText(true);
        tip1Label.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Label tip2Label = new Label("• La visualización LaTeX te permite verificar que la función se interpreta correctamente.");
        tip2Label.setWrapText(true);
        tip2Label.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        content.getChildren().addAll(
            introLabel, 
            new Separator(), 
            functionsLabel, 
            graphingLabel, 
            bisectionLabel, 
            fixedPointLabel, 
            new Separator(), 
            tipsLabel, 
            tip1Label, 
            tip2Label
        );

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setStyle("-fx-background-color: #0A0629;");
        alert.getDialogPane().getStyleClass().add("custom-alert");

        alert.showAndWait();
    }

    /**
     * Muestra un menú con los métodos numéricos disponibles
     */
    private void showNumericalMethodsMenu() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Métodos Numéricos Disponibles");
        alert.setHeaderText("Seleccione un método numérico");

        VBox content = new VBox(15);
        content.setStyle("-fx-background-color: #0A0629; -fx-padding: 15px;");

        Button btnBiseccionDialog = new Button("Método de Bisección");
        btnBiseccionDialog.getStyleClass().add("method-button");
        btnBiseccionDialog.setPrefWidth(200);
        btnBiseccionDialog.setOnAction(e -> {
            alert.close();
            try {
                App.app.setScene(Paths.METODO_BIS);
            } catch (Exception ex) {
                showErrorDialog("Error", ex.getMessage());
            }
        });

        Button btnPuntoFijoDialog = new Button("Método de Punto Fijo");
        btnPuntoFijoDialog.getStyleClass().add("method-button");
        btnPuntoFijoDialog.setPrefWidth(200);
        btnPuntoFijoDialog.setOnAction(e -> {
            alert.close();
            try {
                App.app.setScene(Paths.METODO_PTO_FIJO);
            } catch (Exception ex) {
                showErrorDialog("Error", ex.getMessage());
            }
        });

        Button btnGraficadoraDialog = new Button("Graficadora Avanzada");
        btnGraficadoraDialog.getStyleClass().add("method-button");
        btnGraficadoraDialog.setPrefWidth(200);
        btnGraficadoraDialog.setOnAction(e -> {
            alert.close();
            try {
                App.app.setScene(Paths.GRAFICA);
            } catch (Exception ex) {
                showErrorDialog("Error", ex.getMessage());
            }
        });

        Button btnInterpolacionDialog = new Button("Interpolación de Lagrange");
        btnInterpolacionDialog.getStyleClass().add("method-button");
        btnInterpolacionDialog.setPrefWidth(200);
        btnInterpolacionDialog.setOnAction(e -> {
            alert.close();
            try {
                App.app.setScene(Paths.METODO_INTERPOLACION);
            } catch (Exception ex) {
                showErrorDialog("Error", ex.getMessage());
            }
        });

        content.getChildren().addAll(
            btnBiseccionDialog,
            btnPuntoFijoDialog,
            btnGraficadoraDialog,
            btnInterpolacionDialog
        );

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setStyle("-fx-background-color: #0A0629;");
        alert.getDialogPane().getStyleClass().add("custom-alert");

        // Reemplazar los botones estándar
        alert.getButtonTypes().clear();
        alert.getButtonTypes().add(ButtonType.CLOSE);

        alert.showAndWait();
    }

    /**
     * Muestra un diálogo de error
     */
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Ha ocurrido un error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupLatexRender() {
        SwingUtilities.invokeLater(() -> {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(Color.WHITE);
            panel.setPreferredSize(new Dimension(600, 80));
            panel.setFocusable(false); // Deshabilitar foco en el JPanel
            latexRender.setContent(panel);
            latexRender.setDisable(false); // Deshabilitar foco en el SwingNode
            updateLatexRender(""); // Renderizar texto inicial

            // Añadir animación de entrada al panel de LaTeX
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
                    System.out.println("Intentando renderizar: " + latex); // Depuración
                    String latexFunction = ConvertidorLatex.toLatex(latex); // Convertir a LaTeX
                    System.out.println("LaTeX generado: " + latexFunction); // Depuración
                    if (latexFunction.startsWith("Esperando...")) {
                        panel.add(new JLabel(latexFunction), BorderLayout.CENTER);
                    } else {
                        TeXFormula formula = new TeXFormula(latexFunction);
                        TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20); // Tamaño de fuente
                        icon.setForeground(Color.BLACK);
                        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g2 = image.createGraphics();
                        icon.paintIcon(null, g2, 0, 0);
                        g2.dispose();
                        JLabel label = new JLabel(new ImageIcon(image));
                        panel.add(label, BorderLayout.CENTER);
                    }
                } catch (Exception e) {
                    System.out.println("Error al renderizar: " + e.getMessage()); // Depuración
                    panel.add(new JLabel("Esperando..."), BorderLayout.CENTER);
                }
            } else {
                panel.add(new JLabel("Esperando..."), BorderLayout.CENTER);
            }
            panel.revalidate();
            panel.repaint();
            Platform.runLater(() -> {
                latexRender.requestFocus();
            });
        });
    }

    private void setupSymbolButtons() {
        // Símbolos básicos
        String[] basicSymbols = {"(", ")", "+", "-", "*", "/", "^", "="};

        // Funciones trigonométricas
        String[] trigSymbols = {"sin", "cos", "tan", "cot", "sec", "csc"};

        // Funciones inversas
        String[] invTrigSymbols = {"arcsin", "arccos", "arctan"};

        // Constantes y símbolos especiales
        String[] specialSymbols = {"π", "e", "i", "∞"};

        // Operadores y funciones matemáticas
        String[] mathOperators = {"√", "∛", "log", "ln", "exp", "abs"};

        // Cálculo
        String[] calculusSymbols = {"∫", "∂", "∑", "∏", "lim"};

        // Combinar todos los símbolos en grupos
        String[][] symbolGroups = {basicSymbols, trigSymbols, specialSymbols, mathOperators, calculusSymbols};

        // Limpiar el contenedor de botones
        symbolButtons.getChildren().clear();

        // Crear un VBox para organizar los botones en grupos
        VBox symbolGroupsContainer = new VBox(5);
        symbolGroupsContainer.setStyle("-fx-padding: 5px;");

        // Añadir cada grupo de símbolos
        for (int groupIndex = 0; groupIndex < symbolGroups.length; groupIndex++) {
            HBox groupBox = new HBox(5);
            groupBox.setAlignment(javafx.geometry.Pos.CENTER);

            String[] symbols = symbolGroups[groupIndex];

            for (int i = 0; i < symbols.length; i++) {
                String symbol = symbols[i];
                Button button = new Button(symbol);

                // Ajustar tamaño según la longitud del símbolo
                if (symbol.length() <= 1) {
                    button.setPrefSize(40, 40);
                } else if (symbol.length() <= 3) {
                    button.setPrefSize(60, 40);
                } else {
                    button.setPrefSize(80, 40);
                }

                button.getStyleClass().add("symbol-button");

                // Añadir clase específica según el grupo
                switch (groupIndex) {
                    case 0: button.getStyleClass().add("basic-symbol"); break;
                    case 1: button.getStyleClass().add("trig-symbol"); break;
                    case 2: button.getStyleClass().add("special-symbol"); break;
                    case 3: button.getStyleClass().add("math-operator"); break;
                    case 4: button.getStyleClass().add("calculus-symbol"); break;
                }

                button.setFocusTraversable(false); // Deshabilitar que el botón reciba foco

                // Añadir tooltip con descripción
                String description = getSymbolDescription(symbol);
                if (description != null && !description.isEmpty()) {
                    javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(description);
                    javafx.scene.control.Tooltip.install(button, tooltip);
                }

                // Añadir efecto de animación con retraso para entrada escalonada
                final int index = i;
                final int finalGroupIndex = groupIndex;
                Platform.runLater(() -> {
                    // Inicialmente fuera de vista
                    button.setTranslateY(30);
                    button.setOpacity(0);

                    // Animación de entrada
                    TranslateTransition translateIn = new TranslateTransition(Duration.millis(400), button);
                    translateIn.setFromY(30);
                    translateIn.setToY(0);

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(500), button);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);

                    ParallelTransition parallelTransition = new ParallelTransition(translateIn, fadeIn);
                    parallelTransition.setDelay(Duration.millis(100 + (finalGroupIndex * 100) + (index * 50))); // Retraso escalonado
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
                        String insertion = getSymbolInsertion(symbol);
                        int caretPosition = txtFunction.getCaretPosition();

                        // Insertar el símbolo en la posición del cursor
                        String newText = currentText.substring(0, caretPosition) + insertion + currentText.substring(caretPosition);
                        txtFunction.setText(newText);
                        updateLatexRender(newText); // Actualizar renderizado
                        scaleUp.play();

                        Platform.runLater(() -> {
                            txtFunction.requestFocus(); // Forzar foco
                            // Posicionar el cursor después del símbolo insertado
                            txtFunction.positionCaret(caretPosition + insertion.length());
                        });
                    });

                    scaleDown.play();
                });

                groupBox.getChildren().add(button);
            }

            symbolGroupsContainer.getChildren().add(groupBox);

            // Añadir separador entre grupos (excepto después del último)
            if (groupIndex < symbolGroups.length - 1) {
                Separator separator = new Separator();
                separator.setOpacity(0.3);
                symbolGroupsContainer.getChildren().add(separator);
            }
        }

        // Añadir el contenedor de grupos al HBox principal
        symbolButtons.getChildren().add(symbolGroupsContainer);
    }

    /**
     * Obtiene una descripción para un símbolo matemático
     */
    private String getSymbolDescription(String symbol) {
        switch (symbol) {
            case "π": return "Pi (3.14159...)";
            case "e": return "Número de Euler (2.71828...)";
            case "i": return "Unidad imaginaria";
            case "∞": return "Infinito";
            case "√": return "Raíz cuadrada";
            case "∛": return "Raíz cúbica";
            case "log": return "Logaritmo base 10";
            case "ln": return "Logaritmo natural (base e)";
            case "exp": return "Función exponencial (e^x)";
            case "sin": return "Seno";
            case "cos": return "Coseno";
            case "tan": return "Tangente";
            case "cot": return "Cotangente";
            case "sec": return "Secante";
            case "csc": return "Cosecante";
            case "arcsin": return "Arcoseno (sin⁻¹)";
            case "arccos": return "Arcocoseno (cos⁻¹)";
            case "arctan": return "Arcotangente (tan⁻¹)";
            case "∫": return "Integral";
            case "∂": return "Derivada parcial";
            case "∑": return "Sumatoria";
            case "∏": return "Productoria";
            case "lim": return "Límite";
            case "abs": return "Valor absoluto";
            default: return "";
        }
    }

    /**
     * Obtiene el texto a insertar para un símbolo matemático
     */
    private String getSymbolInsertion(String symbol) {
        switch (symbol) {
            case "sin": 
            case "cos": 
            case "tan": 
            case "cot": 
            case "sec": 
            case "csc": 
            case "arcsin": 
            case "arccos": 
            case "arctan": 
            case "log": 
            case "ln": 
            case "exp": 
            case "abs": 
            case "lim": return symbol + "(";
            case "√": return "sqrt(";
            case "∛": return "cbrt(";
            case "∑": return "sum(";
            case "∏": return "prod(";
            case "∫": return "integrate(";
            case "∂": return "diff(";
            default: return symbol;
        }
    }

    @FXML
    void confirmFunction() {
        String function = txtFunction.getText().trim();
        if (!function.isEmpty()) {
            // Animación para confirmación
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), latexRender.getParent());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.7);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), latexRender.getParent());
            fadeIn.setFromValue(0.7);
            fadeIn.setToValue(1.0);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), latexRender.getParent());
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);

            ScaleTransition scaleNormal = new ScaleTransition(Duration.millis(200), latexRender.getParent());
            scaleNormal.setToX(1.0);
            scaleNormal.setToY(1.0);

            ParallelTransition parallelOut = new ParallelTransition(fadeOut, scaleUp);
            ParallelTransition parallelIn = new ParallelTransition(fadeIn, scaleNormal);

            parallelOut.setOnFinished(e -> {
                String latexFunction = ConvertidorLatex.toLatex(function);
                System.out.println("Función en LaTeX: " + latexFunction);
                App.app.setFunction(function);
                System.out.println("Función guardada en App: " + App.app.getFunction());
                parallelIn.play();
            });

            parallelOut.play();
        } else {
            System.out.println("Por favor, ingresa una función.");

            // Animación de aviso de error
            Timeline shakingAnimation = new Timeline(
                new KeyFrame(Duration.millis(0), evt -> txtFunction.setTranslateX(0)),
                new KeyFrame(Duration.millis(50), evt -> txtFunction.setTranslateX(5)),
                new KeyFrame(Duration.millis(100), evt -> txtFunction.setTranslateX(-5)),
                new KeyFrame(Duration.millis(150), evt -> txtFunction.setTranslateX(5)),
                new KeyFrame(Duration.millis(200), evt -> txtFunction.setTranslateX(-5)),
                new KeyFrame(Duration.millis(250), evt -> txtFunction.setTranslateX(0))
            );
            shakingAnimation.play();
        }
    }
}
