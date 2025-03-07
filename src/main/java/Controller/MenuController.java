package Controller; // Ajusta el paquete según tu estructura

import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MenuController {

    @FXML
    private HBox navBar;

    @FXML
    private Button methodsButton, solutionsButton, graphButton, minimizeButton, maximizeButton, exitButton;

    @FXML
    private VBox methodsSubMenu, solutionsSubMenu;

    private FadeTransition currentFadeOut;
    private boolean isMaximized = false;

    @FXML
    public void initialize() {
        if (navBar == null || methodsButton == null || solutionsButton == null ||
                methodsSubMenu == null || solutionsSubMenu == null ||
                minimizeButton == null || maximizeButton == null || exitButton == null) {
            System.err.println("Error: Algún elemento no está inicializado.");
            return;
        }

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

        // Configurar botón Minimizar
        minimizeButton.setOnMouseClicked(event -> {
            Stage stage = (Stage) navBar.getScene().getWindow();
            RotateTransition rotate = new RotateTransition(Duration.millis(200), minimizeButton);
            rotate.setByAngle(90);
            rotate.setCycleCount(2);
            rotate.setAutoReverse(true);
            rotate.setOnFinished(e -> stage.setIconified(true));
            rotate.play();
        });

        // Configurar botón Maximizar/Restaurar
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
                    stage.setWidth(800); // Tamaño restaurado más grande
                    stage.setHeight(600);
                    isMaximized = false;
                } else {
                    stage.setMaximized(true);
                    isMaximized = true;
                }
            });
            scale.play();
        });

        // Configurar botón Salir (nuevo estilo)
        exitButton.setOnMouseClicked(event -> {
            RotateTransition rotate = new RotateTransition(Duration.millis(200), exitButton);
            rotate.setByAngle(45); // Giro diferente para distinguirlo
            rotate.setCycleCount(2);
            rotate.setAutoReverse(true);
            rotate.setOnFinished(e -> System.exit(0));
            rotate.play();
        });
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
        return methodsButton.isHover() || solutionsButton.isHover() ||
                graphButton.isHover();
    }

    public HBox getNavBar() {
        return navBar;
    }
}