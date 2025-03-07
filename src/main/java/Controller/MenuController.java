package Controller;

import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import application.ConvertidorLatex;

public class MenuController {

    @FXML
    private HBox navBar;

    @FXML
    private Button methodsButton, solutionsButton, graphButton, minimizeButton, maximizeButton, exitButton;

    @FXML
    private VBox methodsSubMenu, solutionsSubMenu;

    @FXML
    private TextField equationInput;

    @FXML
    private WebView webView;

    private FadeTransition currentFadeOut;
    private boolean isMaximized = false;
    private WebEngine webEngine;

    @FXML
    public void initialize() {
        if (navBar == null || methodsButton == null || solutionsButton == null ||
                methodsSubMenu == null || solutionsSubMenu == null ||
                minimizeButton == null || maximizeButton == null || exitButton == null ||
                equationInput == null || webView == null) {
            System.err.println("Error: Algún elemento no está inicializado.");
            return;
        }

        // Configuración de la barra de navegación
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(600), navBar);
        slideIn.setFromY(-60);
        slideIn.setToY(0);
        slideIn.play();

        setupButtonAnimation(methodsButton, methodsSubMenu);
        setupButtonAnimation(solutionsButton, solutionsSubMenu);
        setupButtonAnimation(graphButton, null);

        navBar.setOnMouseEntered(event -> {
            TranslateTransition shift = new TranslateTransition(Duration.millis(400), navBar);
            shift.setByX(10);
            shift.setCycleCount(2);
            shift.setAutoReverse(true);
            shift.play();

            FadeTransition glow = new FadeTransition(Duration.millis(400), navBar);
            glow.setFromValue(1.0);
            glow.setToValue(0.85);
            glow.setCycleCount(2);
            glow.setAutoReverse(true);
            glow.play();
        });

        navBar.setOnMouseExited(event -> {
            TranslateTransition reset = new TranslateTransition(Duration.millis(300), navBar);
            reset.setToX(0);
            reset.play();

            FadeTransition fadeBack = new FadeTransition(Duration.millis(300), navBar);
            fadeBack.setFromValue(0.85);
            fadeBack.setToValue(1.0);
            fadeBack.play();
        });

        minimizeButton.setOnMouseClicked(event -> {
            Stage stage = (Stage) navBar.getScene().getWindow();
            RotateTransition rotate = new RotateTransition(Duration.millis(200), minimizeButton);
            rotate.setByAngle(90);
            rotate.setCycleCount(2);
            rotate.setAutoReverse(true);
            rotate.setOnFinished(e -> stage.setIconified(true));
            rotate.play();
        });

        maximizeButton.setOnMouseClicked(event -> {
            Stage stage = (Stage) navBar.getScene().getWindow();
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), maximizeButton);
            scale.setToX(1.2);
            scale.setToY(1.2);
            scale.setCycleCount(2);
            scale.setAutoReverse(true);
            scale.setOnFinished(e -> {
                if (isMaximized) {
                    stage.setMaximized(false);
                    stage.setWidth(800);
                    stage.setHeight(600);
                    isMaximized = false;
                } else {
                    stage.setMaximized(true);
                    isMaximized = true;
                }
            });
            scale.play();
        });

        exitButton.setOnMouseClicked(event -> {
            RotateTransition rotate = new RotateTransition(Duration.millis(200), exitButton);
            rotate.setByAngle(45);
            rotate.setCycleCount(2);
            rotate.setAutoReverse(true);
            rotate.setOnFinished(e -> System.exit(0));
            rotate.play();
        });

        // Configuración del WebView con MathJax (desde MathController)
        webEngine = webView.getEngine();
        String mathJaxHtml = "<html>\n" +
                "<head>\n" +
                "    <script type=\"text/javascript\" async\n" +
                "      src=\"https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.7/MathJax.js?config=TeX-AMS_HTML\">\n" +
                "    </script>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"math\"></div>\n" +
                "    <script>\n" +
                "        function updateEquation(latex) {\n" +
                "            document.getElementById(\"math\").innerHTML = \"$$\" + latex + \"$$\";\n" +
                "            MathJax.Hub.Queue([\"Typeset\", MathJax.Hub]);\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
        webEngine.loadContent(mathJaxHtml);

        // Actualizar ecuación al presionar Enter
        equationInput.setOnAction(e -> updateEquation());
    }

    private void setupButtonAnimation(Button button, VBox subMenu) {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), button);
        scaleIn.setToX(1.1);
        scaleIn.setToY(1.1);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), button);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        button.setOnMouseEntered(event -> {
            System.out.println("Entrando al botón: " + button.getText());
            scaleIn.playFromStart();
            if (subMenu != null) {
                if (currentFadeOut != null) {
                    currentFadeOut.stop();
                }
                positionSubMenuBelowButton(button, subMenu);
                showSubMenu(subMenu);
            }
        });

        button.setOnMouseExited(event -> {
            System.out.println("Saliendo del botón: " + button.getText());
            scaleOut.playFromStart();
            if (subMenu != null && !subMenu.isHover()) {
                hideSubMenu(subMenu);
            }
        });

        if (subMenu != null) {
            subMenu.setOnMouseEntered(event -> {
                System.out.println("Entrando al submenú de: " + button.getText());
                if (currentFadeOut != null) {
                    currentFadeOut.stop();
                    subMenu.setVisible(true);
                    subMenu.setOpacity(1.0);
                }
            });

            subMenu.setOnMouseExited(event -> {
                System.out.println("Saliendo del submenú de: " + button.getText());
                if (!button.isHover()) {
                    hideSubMenu(subMenu);
                }
            });
        }
    }

    private void positionSubMenuBelowButton(Button button, VBox subMenu) {
        double buttonX = button.localToScene(0, 0).getX();
        double buttonY = button.localToScene(0, 0).getY() + button.getHeight();
        subMenu.setLayoutX(buttonX);
        subMenu.setLayoutY(buttonY);
    }

    private void showSubMenu(VBox subMenu) {
        subMenu.setVisible(true);
        subMenu.setManaged(true);
        subMenu.setOpacity(1.0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), subMenu);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.playFromStart();
    }

    private void hideSubMenu(VBox subMenu) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), subMenu);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            System.out.println("FadeOut finalizado. Submenu hover: " + subMenu.isHover() + ", Any button hover: " + isAnyButtonHovered());
            if (!subMenu.isHover() && !isAnyButtonHovered()) {
                subMenu.setVisible(false);
                subMenu.setManaged(false);
            }
        });
        currentFadeOut = fadeOut;
        System.out.println("Iniciando ocultación del submenú");
        fadeOut.playFromStart();
    }

    private boolean isAnyButtonHovered() {
        return methodsButton.isHover() || solutionsButton.isHover() || graphButton.isHover();
    }

    public HBox getNavBar() {
        return navBar;
    }

    // Métodos para insertar símbolos (ajustados para LaTeX y MathEclipse)
    @FXML
    private void insertExponent() {
        equationInput.insertText(equationInput.getCaretPosition(), "^2");
    }

    @FXML
    private void insertSqrt() {
        equationInput.insertText(equationInput.getCaretPosition(), "sqrt(");
    }

    @FXML
    private void insertPi() {
        equationInput.insertText(equationInput.getCaretPosition(), "π");
    }

    @FXML
    private void insertSin() {
        equationInput.insertText(equationInput.getCaretPosition(), "sin(");
    }

    @FXML
    private void insertCos() {
        equationInput.insertText(equationInput.getCaretPosition(), "cos(");
    }

    @FXML
    private void insertEqual() {
        equationInput.insertText(equationInput.getCaretPosition(), " = ");
    }

    @FXML
    private void insertAlpha() {
        equationInput.insertText(equationInput.getCaretPosition(), "\\alpha");
    }

    @FXML
    private void insertBeta() {
        equationInput.insertText(equationInput.getCaretPosition(), "\\beta");
    }

    @FXML
    private void insertGamma() {
        equationInput.insertText(equationInput.getCaretPosition(), "\\gamma");
    }

    @FXML
    private void insertTheta() {
        equationInput.insertText(equationInput.getCaretPosition(), "\\theta");
    }

    @FXML
    private void insertIntegral() {
        equationInput.insertText(equationInput.getCaretPosition(), "\\int_{}^{}");
    }

    @FXML
    private void insertSum() {
        equationInput.insertText(equationInput.getCaretPosition(), "\\sum_{}^{}");
    }

    @FXML
    private void insertInfinity() {
        equationInput.insertText(equationInput.getCaretPosition(), "\\infty");
    }

    @FXML
    private void renderEquation() {
        updateEquation();
    }

    private void updateEquation() {
        String userInput = equationInput.getText();
        String latex = ConvertidorLatex.toLatex(userInput); // Convierte a LaTeX con MathEclipse

        Platform.runLater(() ->
                webEngine.executeScript("updateEquation('" + latex.replace("\\", "\\\\") + "');")
        );
    }
}