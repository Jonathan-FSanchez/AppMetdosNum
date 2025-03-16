package controllers;

import application.App;
import javafx.animation.PauseTransition;
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
        popup = new Popup();
        subPopup = new Popup();

        popupContent = new VBox(5);
        popupContent.getStyleClass().add("context-menu");
        popup.getContent().setAll(popupContent);

        subPopupContent = new VBox(5);
        subPopupContent.getStyleClass().add("context-menu");
        subPopup.getContent().setAll(subPopupContent);

        hideDelay = new PauseTransition(Duration.millis(200));
        subHideDelay = new PauseTransition(Duration.millis(200));

        setupHoverEvent(btnMetodos, "Método");
        setupHoverEvent(btnSoluciones, "Solución");
        setupHoverEvent(btnGraficas, "Gráfica");

        btnVMain.setOnAction(this::btnVMain);
        btnExit.setOnAction(this::btnExit);
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
                        App.app.setScene(Paths.METODO_BIS);
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
                        App.app.setScene(Paths.METODO_PTO_FIJO);
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
            popup.show(button, x, y);
        });

        button.setOnMouseExited(event -> {
            hideDelay.setOnFinished(e -> {
                if (!popupContent.isHover() && !button.isHover() && !subPopupContent.isHover()) {
                    popup.hide();
                    subPopup.hide();
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
                    popup.hide();
                    subPopup.hide();
                }
            });
            hideDelay.playFromStart();
        });
    }

    protected void setupSubMenu(Label label) {
        label.setOnMouseClicked(event -> {
            subPopupContent.getChildren().clear();

            Label subItem1 = new Label("Método de Deflación");
            Label subItem2 = new Label("Método de Raíces de Pol_2");
            Label subItem3 = new Label("Método de Raíces de Pol_3");

            subItem1.getStyleClass().add("menu-item");
            subItem2.getStyleClass().add("menu-item");
            subItem3.getStyleClass().add("menu-item");

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
            subPopup.show(label, x, y);
        });

        label.setOnMouseExited(event -> {
            subHideDelay.setOnFinished(e -> {
                if (!subPopupContent.isHover()) {
                    subPopup.hide();
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
                    subPopup.hide();
                }
            });
            subHideDelay.playFromStart();
        });
    }

}





/*
package controllers;

import application.App;
import javafx.animation.PauseTransition;
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
    protected VBox popupContent;
    protected PauseTransition hideDelay;

    @FXML
    protected void initializeTopBar() {
        popup = new Popup();
        popupContent = new VBox(5);
        popupContent.getStyleClass().add("context-menu");
        popup.getContent().setAll(popupContent);

        hideDelay = new PauseTransition(Duration.millis(200));

        setupHoverEvent(btnMetodos, "Método");
        setupHoverEvent(btnSoluciones, "Solución");
        setupHoverEvent(btnGraficas, "Gráfica");

        btnVMain.setOnAction(this::btnVMain);
        btnExit.setOnAction(this::btnExit);
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

            Label item1 = new Label(title + " de Bisección");
            Label item2 = new Label(title + " de Secante");
            Label item3 = new Label(title + " de Punto Fijo");

            item1.getStyleClass().add("menu-item");
            item2.getStyleClass().add("menu-item");
            item3.getStyleClass().add("menu-item");

            if (button == btnSoluciones) { // Solo para btnSoluciones cambiamos la vista
                item1.setOnMouseClicked(e -> {
                    System.out.println("Seleccionaste: " + item1.getText());
                    String function = App.app.getFunction();
                    if (function == null || function.trim().isEmpty()) {
                        System.out.println("No se ha ingresado una función. Usa la vista principal para ingresarla.");
                    } else {
                        App.app.setScene(Paths.METODO_BIS); // Cambiar a la vista de bisección
                    }
                    popup.hide();
                });
                item2.setOnMouseClicked(e -> {
                    System.out.println("Seleccionaste: " + item2.getText());
                    // Aquí puedes agregar la lógica para otra vista (por ejemplo, Paths.METODO_SEC)
                    popup.hide();
                });
                item3.setOnMouseClicked(e -> {
                    System.out.println("Seleccionaste: " + item3.getText());
                    // Aquí puedes agregar la lógica para otra vista (por ejemplo, Paths.METODO_PF)
                    popup.hide();
                });
            } else if (button == btnMetodos || button == btnGraficas) {
                // Para btnMetodos y btnGraficas, solo imprimimos por ahora (puedes añadir lógica)
                item1.setOnMouseClicked(e -> System.out.println("Seleccionaste: " + item1.getText()));
                item2.setOnMouseClicked(e -> System.out.println("Seleccionaste: " + item2.getText()));
                item3.setOnMouseClicked(e -> System.out.println("Seleccionaste: " + item3.getText()));
            }

            popupContent.getChildren().addAll(item1, item2, item3);

            Bounds bounds = button.localToScreen(button.getBoundsInLocal());
            double x = bounds.getMinX();
            double y = 130;
            popup.show(button, x, y);
        });

        button.setOnMouseExited(event -> {
            hideDelay.setOnFinished(e -> {
                if (!popupContent.isHover() && !button.isHover()) {
                    popup.hide();
                }
            });
            hideDelay.playFromStart();
        });

        popupContent.setOnMouseEntered(event -> {
            hideDelay.stop();
        });

        popupContent.setOnMouseExited(event -> {
            hideDelay.setOnFinished(e -> {
                if (!button.isHover()) {
                    popup.hide();
                }
            });
            hideDelay.playFromStart();
        });
    }

    @FXML
    abstract void initialize();
}

}
*/