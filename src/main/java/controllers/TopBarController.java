package controllers;

import application.App;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;
import utils.Paths;

public abstract class TopBarController {
    @FXML protected Button btnMetodos;
    @FXML protected Button btnSoluciones;
    @FXML protected Button btnGraficas;
    @FXML protected Button btnVMain;
    @FXML protected Button btnExit;

    protected Popup popup;
    protected Popup subPopup; // Segundo Popup para el submenú
    protected VBox popupContent;
    protected VBox subPopupContent; // Contenido del submenú
    protected PauseTransition hideDelay;
    protected PauseTransition subHideDelay; // Retraso para el submenú

    // En la clase TopBarController, modifica estas constantes para distancias verticales más cortas:
    // Posiciones fijas para el menú y submenú (relativas a la ventana)
    private static final double MENU_OFFSET_Y = 10.0; // Mantiene la distancia original para menús principales
    private static final double SUBMENU_OFFSET_Y = 250.0; // Distancia reducida solo para el submenú de Soluciones

    @FXML
    protected void initializeTopBar() {
        // Configurar animaciones para los botones de la barra superior
        setupButtonAnimations(btnMetodos);
        setupButtonAnimations(btnSoluciones);
        setupButtonAnimations(btnGraficas);
        setupButtonAnimations(btnVMain);
        setupButtonAnimations(btnExit);

        popup = new Popup();
        subPopup = new Popup();

        popupContent = new VBox(5);
        popupContent.getStyleClass().addAll("context-menu", "popup");
        popup.getContent().setAll(popupContent);

        subPopupContent = new VBox(5);
        subPopupContent.getStyleClass().addAll("context-menu", "popup");
        subPopup.getContent().setAll(subPopupContent);

        hideDelay = new PauseTransition(Duration.millis(200));
        subHideDelay = new PauseTransition(Duration.millis(200));

        setupHoverEvent(btnMetodos, "Método");
        setupHoverEvent(btnSoluciones, "Solución");
        setupHoverEvent(btnGraficas, "Gráfica");

        btnVMain.setOnAction(this::btnVMain);
        btnExit.setOnAction(this::btnExit);
    }

    private void setupButtonAnimations(Button button) {
        button.setOnMouseEntered(event -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(150), button);
            scaleTransition.setToX(1.1);
            scaleTransition.setToY(1.1);
            scaleTransition.play();
        });

        button.setOnMouseExited(event -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(150), button);
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();
        });

        button.setOnMousePressed(event -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), button);
            scaleTransition.setToX(0.95);
            scaleTransition.setToY(0.95);
            scaleTransition.play();
        });

        button.setOnMouseReleased(event -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), button);
            scaleTransition.setToX(1.1);
            scaleTransition.setToY(1.1);
            scaleTransition.play();
        });
    }

    @FXML
    protected void btnExit(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    protected void btnVMain(ActionEvent event) {
        App.app.setScene(Paths.MENU_PRINCIPAL);
    }

    protected void setupHoverEvent(Button button, String title) {
        button.setOnMouseEntered(event -> {
            hideDelay.stop();
            popupContent.getChildren().clear();
            popupContent.getStyleClass().add("showing");

            Label item1 = new Label(title + " de Bisección");
            Label item2 = new Label(title + " de Punto Fijo");
            Label item3 = new Label(title + " de la Secante");
            Label item4 = new Label(title + " de Newton Rapshon");
            Label item5 = new Label(title + " de Müller");
            Label item6 = new Label(title + " de Añadir metodo");

            item1.getStyleClass().add("menu-item");
            item2.getStyleClass().add("menu-item");
            item3.getStyleClass().add("menu-item");
            item4.getStyleClass().add("menu-item");
            item5.getStyleClass().add("menu-item");
            item6.getStyleClass().add("menu-item");

            // Animación para los elementos del menú
            setupMenuItemAnimation(item1, 0);
            setupMenuItemAnimation(item2, 1);
            setupMenuItemAnimation(item3, 2);
            setupMenuItemAnimation(item4, 3);
            setupMenuItemAnimation(item5, 4);
            setupMenuItemAnimation(item6, 5);

            // Configurar el submenú para "Solución de Raíces de un Polinomio"
            if (button == btnSoluciones) {
                setupSubMenu(item6, button); // Pasar el botón padre
            }

            if (button == btnSoluciones) {
                item1.setOnMouseClicked(e -> {
                    System.out.println("Seleccionaste: " + item1.getText());
                    String function = App.app.getFunction();
                    if (function == null || function.trim().isEmpty()) {
                        System.out.println("No se ha ingresado una función. Usa la vista principal para ingresarla.");

                        // Mostrar animación de error
                        Timeline shakeAnimation = new Timeline(
                            new KeyFrame(Duration.millis(0), evt -> item1.setTranslateX(0)),
                            new KeyFrame(Duration.millis(50), evt -> item1.setTranslateX(5)),
                            new KeyFrame(Duration.millis(100), evt -> item1.setTranslateX(-5)),
                            new KeyFrame(Duration.millis(150), evt -> item1.setTranslateX(5)),
                            new KeyFrame(Duration.millis(200), evt -> item1.setTranslateX(0))
                        );
                        shakeAnimation.play();
                    } else {
                        // Animación de transición entre escenas
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), button.getScene().getRoot());
                        fadeOut.setFromValue(1.0);
                        fadeOut.setToValue(0.0);
                        fadeOut.setOnFinished(ev -> {
                            App.app.setScene(Paths.METODO_BIS);
                        });
                        fadeOut.play();
                    }
                    popup.hide();
                    subPopup.hide();
                });
                item2.setOnMouseClicked(e -> {
                    System.out.println("Seleccionaste: " + item2.getText());
                    String function = App.app.getFunction();
                    if (function == null || function.trim().isEmpty()) {
                        System.out.println("No se ha ingresado una función. Usa la vista principal para ingresarla.");

                        // Mostrar animación de error
                        Timeline shakeAnimation = new Timeline(
                            new KeyFrame(Duration.millis(0), evt -> item2.setTranslateX(0)),
                            new KeyFrame(Duration.millis(50), evt -> item2.setTranslateX(5)),
                            new KeyFrame(Duration.millis(100), evt -> item2.setTranslateX(-5)),
                            new KeyFrame(Duration.millis(150), evt -> item2.setTranslateX(5)),
                            new KeyFrame(Duration.millis(200), evt -> item2.setTranslateX(0))
                        );
                        shakeAnimation.play();
                    } else {
                        // Animación de transición entre escenas
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), button.getScene().getRoot());
                        fadeOut.setFromValue(1.0);
                        fadeOut.setToValue(0.0);
                        fadeOut.setOnFinished(ev -> {
                            App.app.setScene(Paths.METODO_PTO_FIJO);
                        });
                        fadeOut.play();
                    }
                    popup.hide();
                    subPopup.hide();
                });
                item3.setOnMouseClicked(e -> {
                    System.out.println("Seleccionaste: " + item3.getText());
                    popup.hide();
                    subPopup.hide();
                });
                item4.setOnMouseClicked(e -> {
                    System.out.println("Seleccionaste: " + item4.getText());
                    popup.hide();
                    subPopup.hide();
                });
                item5.setOnMouseClicked(e -> {
                    System.out.println("Seleccionaste: " + item5.getText());
                    popup.hide();
                    subPopup.hide();
                });
                // No se necesita acción para item6 aquí, ya que tiene submenú
            } else if (button == btnGraficas) {
                item1.setOnMouseClicked(e -> {
                    System.out.println("Seleccionaste: " + item1.getText());
                    // Animación de transición entre escenas
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), button.getScene().getRoot());
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(ev -> {
                        App.app.setScene(Paths.GRAFICA);
                    });
                    fadeOut.play();
                    popup.hide();
                    subPopup.hide();
                });
                item2.setOnMouseClicked(e -> System.out.println("Seleccionaste: " + item2.getText()));
                item3.setOnMouseClicked(e -> System.out.println("Seleccionaste: " + item3.getText()));
            } else if (button == btnMetodos) {
                item1.setOnMouseClicked(e -> System.out.println("Seleccionaste: " + item1.getText()));
                item2.setOnMouseClicked(e -> System.out.println("Seleccionaste: " + item2.getText()));
                item3.setOnMouseClicked(e -> System.out.println("Seleccionaste: " + item3.getText()));
            }

            popupContent.getChildren().addAll(item1, item2, item3, item4, item5, item6);

            // Calcular posición relativa a la ventana
            Window window = button.getScene().getWindow();
            Bounds buttonBounds = button.localToScreen(button.getBoundsInLocal());

            // Posicionar el menú debajo del botón con una distancia fija
            double menuX = buttonBounds.getMinX();
            double menuY = buttonBounds.getMaxY() + MENU_OFFSET_Y;

            // Animación de entrada para el popup
            popupContent.setOpacity(0);
            popupContent.setScaleY(0.8);

            popup.show(window, menuX, menuY);

            // Aplicar animación
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), popupContent);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), popupContent);
            scaleIn.setFromY(0.8);
            scaleIn.setToY(1.0);

            ParallelTransition parallelIn = new ParallelTransition(fadeIn, scaleIn);
            parallelIn.play();
        });

        button.setOnMouseExited(event -> {
            hideDelay.setOnFinished(e -> {
                if (!popupContent.isHover() && !button.isHover() && !subPopupContent.isHover()) {
                    // Animación de salida para el menú principal
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(200), popupContent);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);

                    ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), popupContent);
                    scaleOut.setFromY(1.0);
                    scaleOut.setToY(0.8);

                    ParallelTransition parallelOut = new ParallelTransition(fadeOut, scaleOut);
                    parallelOut.setOnFinished(ev -> {
                        popup.hide();
                        popupContent.getStyleClass().remove("showing");
                    });
                    parallelOut.play();

                    // También ocultar el submenú con animación
                    if (subPopup.isShowing()) {
                        FadeTransition subFadeOut = new FadeTransition(Duration.millis(200), subPopupContent);
                        subFadeOut.setFromValue(1);
                        subFadeOut.setToValue(0);

                        ScaleTransition subScaleOut = new ScaleTransition(Duration.millis(200), subPopupContent);
                        subScaleOut.setFromY(1.0);
                        subScaleOut.setToY(0.8);

                        ParallelTransition subParallelOut = new ParallelTransition(subFadeOut, subScaleOut);
                        subParallelOut.setOnFinished(ev -> {
                            subPopup.hide();
                            subPopupContent.getStyleClass().remove("showing");
                        });
                        subParallelOut.play();
                    }
                }
            });
            hideDelay.playFromStart();
        });

        popupContent.setOnMouseEntered(event -> {
            hideDelay.stop();
        });

        popupContent.setOnMouseExited(event -> {
            hideDelay.setOnFinished(e -> {
                if (!button.isHover() && !subPopupContent.isHover()) {
                    // Animación de salida para el menú principal
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(200), popupContent);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);

                    ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), popupContent);
                    scaleOut.setFromY(1.0);
                    scaleOut.setToY(0.8);

                    ParallelTransition parallelOut = new ParallelTransition(fadeOut, scaleOut);
                    parallelOut.setOnFinished(ev -> {
                        popup.hide();
                        popupContent.getStyleClass().remove("showing");
                    });
                    parallelOut.play();

                    // Si el submenú está visible y no se está interactuando con él, ocultarlo también
                    if (subPopup.isShowing() && !subPopupContent.isHover()) {
                        FadeTransition subFadeOut = new FadeTransition(Duration.millis(200), subPopupContent);
                        subFadeOut.setFromValue(1);
                        subFadeOut.setToValue(0);

                        ScaleTransition subScaleOut = new ScaleTransition(Duration.millis(200), subPopupContent);
                        subScaleOut.setFromY(1.0);
                        subScaleOut.setToY(0.8);

                        ParallelTransition subParallelOut = new ParallelTransition(subFadeOut, subScaleOut);
                        subParallelOut.setOnFinished(ev -> {
                            subPopup.hide();
                            subPopupContent.getStyleClass().remove("showing");
                        });
                        subParallelOut.play();
                    }
                }
            });
            hideDelay.playFromStart();
        });
    }

    // Configura la animación para elementos del menú
    private void setupMenuItemAnimation(Label item, int index) {
        // Inicialmente invisible y desplazado
        item.setOpacity(0);
        item.setTranslateX(-20);

        // Animación con retraso basado en el índice
        Timeline timeline = new Timeline(new KeyFrame(
            Duration.millis(100 + (index * 50)),
            e -> {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), item);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                TranslateTransition translateIn = new TranslateTransition(Duration.millis(200), item);
                translateIn.setFromX(-20);
                translateIn.setToX(0);

                ParallelTransition parallel = new ParallelTransition(fadeIn, translateIn);
                parallel.play();
            }
        ));
        timeline.play();
    }

    protected void setupSubMenu(Label label, Button parentButton) {
        label.setOnMouseEntered(event -> {
            subHideDelay.stop();
            subPopupContent.getChildren().clear();
            subPopupContent.getStyleClass().add("showing");

            Label subItem1 = new Label("Método de Deflación");
            Label subItem2 = new Label("Método de Raíces de Pol_2");
            Label subItem3 = new Label("Método de Raíces de Pol_3");

            subItem1.getStyleClass().add("menu-item");
            subItem2.getStyleClass().add("menu-item");
            subItem3.getStyleClass().add("menu-item");

            // Animación para los elementos del submenú
            setupMenuItemAnimation(subItem1, 0);
            setupMenuItemAnimation(subItem2, 1);
            setupMenuItemAnimation(subItem3, 2);

            subItem1.setOnMouseClicked(e -> {
                System.out.println("Seleccionaste: " + subItem1.getText());
                popup.hide();
                subPopup.hide();
            });
            subItem2.setOnMouseClicked(e -> {
                System.out.println("Seleccionaste: " + subItem2.getText());
                popup.hide();
                subPopup.hide();
            });
            subItem3.setOnMouseClicked(e -> {
                System.out.println("Seleccionaste: " + subItem3.getText());
                popup.hide();
                subPopup.hide();
            });

            subPopupContent.getChildren().addAll(subItem1, subItem2, subItem3);

            // Calcular la posición fija para el submenú
            Window window = label.getScene().getWindow();
            Bounds buttonBounds = parentButton.localToScreen(parentButton.getBoundsInLocal());

            // Posicionar el submenú más cerca del botón principal
            double subMenuX = buttonBounds.getMinX() + 150; // Desplazamiento horizontal reducido

            // Usar una distancia vertical menor SOLO para el botón de Soluciones
            double subMenuY;
            if (parentButton == btnSoluciones) {
                subMenuY = buttonBounds.getMaxY() + SUBMENU_OFFSET_Y; // Distancia reducida para Soluciones
            } else {
                subMenuY = buttonBounds.getMaxY() + MENU_OFFSET_Y; // Mantiene la distancia original para otros botones
            }

            // Animación de entrada para el submenú
            subPopupContent.setOpacity(0);
            subPopupContent.setScaleY(0.8);

            subPopup.show(window, subMenuX, subMenuY);

            // Aplicar animación
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), subPopupContent);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), subPopupContent);
            scaleIn.setFromY(0.8);
            scaleIn.setToY(1.0);

            ParallelTransition parallelIn = new ParallelTransition(fadeIn, scaleIn);
            parallelIn.play();
        });

        // Manejar la salida del submenú
        subPopupContent.setOnMouseExited(event -> {
            subHideDelay.setOnFinished(e -> {
                if (!label.isHover()) {
                    // Animación de salida para el submenú
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(200), subPopupContent);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);

                    ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), subPopupContent);
                    scaleOut.setFromY(1.0);
                    scaleOut.setToY(0.8);

                    ParallelTransition parallelOut = new ParallelTransition(fadeOut, scaleOut);
                    parallelOut.setOnFinished(ev -> {
                        subPopup.hide();
                        subPopupContent.getStyleClass().remove("showing");
                    });
                    parallelOut.play();
                }
            });
            subHideDelay.playFromStart();
        });

        subPopupContent.setOnMouseEntered(event -> {
            subHideDelay.stop();
        });
    }

    @FXML
    abstract void initialize();
}
