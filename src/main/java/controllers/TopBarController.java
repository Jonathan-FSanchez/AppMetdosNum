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
                setupSubMenu(item6); // Configura el submenú para item6
            }

            if (button == btnSoluciones) {
                item1.setOnMouseClicked(e -> {
                    System.out.println("Seleccionaste: " + item1.getText());
                    String function = App.app.getFunction();
                    if (function == null || function.trim().isEmpty()) {
                        System.out.println("No se ha ingresado una función. Usa la vista principal para ingresarla.");
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
            } else if (button == btnMetodos || button == btnGraficas) {
                item1.setOnMouseClicked(e -> System.out.println("Seleccionaste: " + item1.getText()));
                item2.setOnMouseClicked(e -> System.out.println("Seleccionaste: " + item2.getText()));
                item3.setOnMouseClicked(e -> System.out.println("Seleccionaste: " + item3.getText()));
            }

            popupContent.getChildren().addAll(item1, item2, item3, item4, item5, item6);

            Bounds bounds = button.localToScreen(button.getBoundsInLocal());
            double x = bounds.getMinX();
            double y = 135;
            
            // Animación de entrada para el popup
            popupContent.setOpacity(0);
            popupContent.setScaleY(0.8);
            
            popup.show(button, x, y);
            
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
                    // Animación de salida
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(200), popupContent);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);
                    
                    ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), popupContent);
                    scaleOut.setFromY(1.0);
                    scaleOut.setToY(0.8);
                    
                    ParallelTransition parallelOut = new ParallelTransition(fadeOut, scaleOut);
                    parallelOut.setOnFinished(ex -> {
                        popup.hide();
                        popupContent.getStyleClass().remove("showing");
                    });
                    parallelOut.play();
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
                    // Animación de salida
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(200), popupContent);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);
                    
                    ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), popupContent);
                    scaleOut.setFromY(1.0);
                    scaleOut.setToY(0.8);
                    
                    ParallelTransition parallelOut = new ParallelTransition(fadeOut, scaleOut);
                    parallelOut.setOnFinished(ex -> {
                        popup.hide();
                        popupContent.getStyleClass().remove("showing");
                    });
                    parallelOut.play();
                }
            });
            hideDelay.playFromStart();
        });
    }
    
    private void setupMenuItemAnimation(Label item, int index) {
        item.setTranslateX(-100);
        item.setOpacity(0);
        
        TranslateTransition moveIn = new TranslateTransition(Duration.millis(200), item);
        moveIn.setToX(0);
        moveIn.setDelay(Duration.millis(index * 50)); // Desfase para la animación secuencial
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), item);
        fadeIn.setToValue(1);
        fadeIn.setDelay(Duration.millis(index * 50));
        
        ParallelTransition parallelTransition = new ParallelTransition(moveIn, fadeIn);
        parallelTransition.play();
    }

    protected void setupSubMenu(Label label) {
        label.setOnMouseEntered(event -> {
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

            // Posicionar el submenú a la derecha del Label
            Bounds bounds = label.localToScreen(label.getBoundsInLocal());
            double x = bounds.getMaxX() + 5; // A la derecha del Label
            double y = bounds.getMinY();
            
            // Animación de entrada para el submenú
            subPopupContent.setOpacity(0);
            subPopupContent.setScaleX(0.8);
            
            subPopup.show(label, x, y);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), subPopupContent);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), subPopupContent);
            scaleIn.setFromX(0.8);
            scaleIn.setToX(1.0);
            
            ParallelTransition parallelIn = new ParallelTransition(fadeIn, scaleIn);
            parallelIn.play();
        });

        label.setOnMouseExited(event -> {
            subHideDelay.setOnFinished(e -> {
                if (!subPopupContent.isHover()) {
                    // Animación de salida para el submenú
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(200), subPopupContent);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);
                    
                    ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), subPopupContent);
                    scaleOut.setFromX(1.0);
                    scaleOut.setToX(0.8);
                    
                    ParallelTransition parallelOut = new ParallelTransition(fadeOut, scaleOut);
                    parallelOut.setOnFinished(ex -> {
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

        subPopupContent.setOnMouseExited(event -> {
            subHideDelay.setOnFinished(e -> {
                if (!label.isHover()) {
                    // Animación de salida para el submenú
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(200), subPopupContent);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);
                    
                    ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), subPopupContent);
                    scaleOut.setFromX(1.0);
                    scaleOut.setToX(0.8);
                    
                    ParallelTransition parallelOut = new ParallelTransition(fadeOut, scaleOut);
                    parallelOut.setOnFinished(ex -> {
                        subPopup.hide();
                        subPopupContent.getStyleClass().remove("showing");
                    });
                    parallelOut.play();
                }
            });
            subHideDelay.playFromStart();
        });
    }

}