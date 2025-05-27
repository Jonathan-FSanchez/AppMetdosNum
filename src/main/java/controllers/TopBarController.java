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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;
import utils.Paths;

public abstract class TopBarController {
    // Original buttons (will be hidden in the new implementation)
    @FXML protected Button btnMetodos;
    @FXML protected Button btnSoluciones;
    @FXML protected Button btnGraficas;
    @FXML protected Button btnVMain;
    @FXML protected Button btnExit;

    // New graficar button
    @FXML protected Button btnGraficar;

    // New main menu button
    @FXML protected Button btnMainMenu;

    protected Popup popup;
    protected Popup subPopup; // Segundo Popup para el submenú
    protected VBox popupContent;
    protected VBox subPopupContent; // Contenido del submenú
    protected PauseTransition hideDelay;
    protected PauseTransition subHideDelay; // Retraso para el submenú

    // Grid for the large submenu
    protected GridPane menuGrid;

    // En la clase TopBarController, modifica estas constantes para distancias verticales más cortas:
    // Posiciones fijas para el menú y submenú (relativas a la ventana)
    private static final double MENU_OFFSET_Y = 10.0; // Mantiene la distancia original para menús principales
    private static final double SUBMENU_OFFSET_Y = 250.0; // Distancia reducida solo para el submenú de Soluciones

    @FXML
    protected void initializeTopBar() {
        // Hide original buttons
        if (btnMetodos != null) btnMetodos.setVisible(false);
        if (btnSoluciones != null) btnSoluciones.setVisible(false);
        if (btnGraficas != null) btnGraficas.setVisible(false);

        // Keep these buttons visible
        setupButtonAnimations(btnVMain);
        setupButtonAnimations(btnExit);

        // Setup the main menu button if it exists
        if (btnMainMenu != null) {
            setupButtonAnimations(btnMainMenu);
            setupMainMenuButton();
        } else {
            // Fallback to original implementation if btnMainMenu is not defined
            setupButtonAnimations(btnMetodos);
            setupButtonAnimations(btnSoluciones);
            setupButtonAnimations(btnGraficas);

            btnMetodos.setVisible(true);
            btnSoluciones.setVisible(true);
            btnGraficas.setVisible(true);

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
            setupHoverEvent(btnSoluciones, "Raíces");
            setupHoverEvent(btnGraficas, "Gráfica");
        }

        btnVMain.setOnAction(this::btnVMain);
        btnExit.setOnAction(this::btnExit);

        // Setup graficar button if it exists
        if (btnGraficar != null) {
            setupButtonAnimations(btnGraficar);
            btnGraficar.setOnAction(event -> {
                // Animación de transición entre escenas
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), btnGraficar.getScene().getRoot());
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(ev -> {
                    App.app.setScene(Paths.GRAFICA);
                });
                fadeOut.play();
            });
        }

        setupDynamicButton(btnGraficar);
        setupDynamicButton(btnVMain);
        setupDynamicButton(btnExit);
    }

    /**
     * Sets up the main menu button with a large submenu containing all options
     */
    protected void setupMainMenuButton() {
        popup = new Popup();
        hideDelay = new PauseTransition(Duration.millis(200));

        // Create a grid pane for the large submenu
        menuGrid = new GridPane();
        menuGrid.setHgap(20);
        menuGrid.setVgap(10);
        menuGrid.getStyleClass().addAll("context-menu", "popup", "large-menu");

        // Create sections for each category
        VBox metodosSection = createMenuSection("Métodos", new String[]{
            "Método de Bisección",
            "Método de Punto Fijo",
            "Método de la Secante",
            "Método de Newton Rapshon",
            "Método de Müller",
            "Método de Añadir metodo"
        });

        VBox raicesSection = createMenuSection("Raíces", new String[]{
            "Solución de Bisección",
            "Solución de Punto Fijo",
            "Solución de la Secante",
            "Solución de Newton Rapshon",
            "Solución de Müller",
            "Solución de Añadir metodo"
        });

        // Create new sections
        VBox derivacionSection = createMenuSection("Derivación", new String[]{
            "Interpolacion",
            "Polinomio Interpolante de Lagrange",
            "Derivacion Númerica",
            "Interpolacion de Richardson",
            "Derivación para puntos Desigualmente espaciados"
        });

        VBox integracionSection = createMenuSection("Integración", new String[]{
            "Integración Numérica",
            "Integración compuesta",
            "Integración múltiple",
            "Interpolación de Romberg",
            "Método Cuadratura adaptiva"
        });

        VBox ecuacionesDifSection = createMenuSection("Ecuaciones Diferenciales", new String[]{
            "Método 1",
            "Método 2"
        });

        // Add sections to the grid - reorganized to eliminate gaps
        menuGrid.add(metodosSection, 0, 0);
        menuGrid.add(raicesSection, 1, 0);
        menuGrid.add(derivacionSection, 2, 0);
        menuGrid.add(integracionSection, 0, 1);
        menuGrid.add(ecuacionesDifSection, 1, 1);

        popup.getContent().setAll(menuGrid);

        // Set up hover event for the main menu button
        btnMainMenu.setOnMouseEntered(event -> showLargeMenu(event));
        btnMainMenu.setOnMouseExited(event -> {
            hideDelay.setOnFinished(e -> {
                if (!menuGrid.isHover() && !btnMainMenu.isHover()) {
                    // Animación de salida para el menú
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(200), menuGrid);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);

                    ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), menuGrid);
                    scaleOut.setFromY(1.0);
                    scaleOut.setToY(0.8);

                    ParallelTransition parallelOut = new ParallelTransition(fadeOut, scaleOut);
                    parallelOut.setOnFinished(ev -> {
                        popup.hide();
                        menuGrid.getStyleClass().remove("showing");
                    });
                    parallelOut.play();
                }
            });
            hideDelay.playFromStart();
        });

        menuGrid.setOnMouseEntered(event -> {
            hideDelay.stop();
        });

        menuGrid.setOnMouseExited(event -> {
            hideDelay.setOnFinished(e -> {
                if (!btnMainMenu.isHover()) {
                    // Animación de salida para el menú
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(200), menuGrid);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);

                    ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), menuGrid);
                    scaleOut.setFromY(1.0);
                    scaleOut.setToY(0.8);

                    ParallelTransition parallelOut = new ParallelTransition(fadeOut, scaleOut);
                    parallelOut.setOnFinished(ev -> {
                        popup.hide();
                        menuGrid.getStyleClass().remove("showing");
                    });
                    parallelOut.play();
                }
            });
            hideDelay.playFromStart();
        });
    }

    /**
     * Creates a section for the large menu with a title and menu items
     */
    private VBox createMenuSection(String title, String[] items) {
        VBox section = new VBox(8);
        section.getStyleClass().add("menu-section");

        // Add title
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("menu-section-title");
        section.getChildren().add(titleLabel);

        // Add menu items
        for (int i = 0; i < items.length; i++) {
            Label item = new Label(items[i]);
            item.getStyleClass().add("menu-item");

            // Setup animations
            setupMenuItemAnimation(item, i);

            // Setup click handlers based on the item text
            setupMenuItemClickHandler(item);

            section.getChildren().add(item);
        }

        return section;
    }

    /**
     * Sets up click handlers for menu items
     */
    private void setupMenuItemClickHandler(Label item) {
        item.setOnMouseClicked(e -> {
            System.out.println("Seleccionaste: " + item.getText());

            // Set up submenus for specific items
            if (item.getText().equals("Integración compuesta")) {
                setupSubMenu(item, btnMainMenu, new String[]{
                    "Trapecio Compuesto",
                    "Simpson 1/3",
                    "Simpson 3/8"
                });
                return;
            } else if (item.getText().equals("Integración múltiple")) {
                setupSubMenu(item, btnMainMenu, new String[]{
                    "Método del trapecio",
                    "Método de simpson 1/3"
                });
                return;
            } else if (item.getText().equals("Método Cuadratura adaptiva")) {
                setupSubMenu(item, btnMainMenu, new String[]{
                    "Simpson Compuesto",
                    "Trapecio simple"
                });
                return;
            }

            // Handle specific menu items
            if (item.getText().contains("Bisección")) {
                String function = App.app.getFunction();
                if (function == null || function.trim().isEmpty()) {
                    System.out.println("No se ha ingresado una función. Usa la vista principal para ingresarla.");

                    // Mostrar animación de error
                    Timeline shakeAnimation = new Timeline(
                        new KeyFrame(Duration.millis(0), evt -> item.setTranslateX(0)),
                        new KeyFrame(Duration.millis(50), evt -> item.setTranslateX(5)),
                        new KeyFrame(Duration.millis(100), evt -> item.setTranslateX(-5)),
                        new KeyFrame(Duration.millis(150), evt -> item.setTranslateX(5)),
                        new KeyFrame(Duration.millis(200), evt -> item.setTranslateX(0))
                    );
                    shakeAnimation.play();
                } else if (item.getText().contains("Solución")) {
                    // Animación de transición entre escenas
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), btnMainMenu.getScene().getRoot());
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(ev -> {
                        App.app.setScene(Paths.METODO_BIS);
                    });
                    fadeOut.play();
                } else if (item.getText().contains("Gráfica")) {
                    // Animación de transición entre escenas
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), btnMainMenu.getScene().getRoot());
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(ev -> {
                        App.app.setScene(Paths.GRAFICA);
                    });
                    fadeOut.play();
                }
            } else if (item.getText().contains("Punto Fijo")) {
                String function = App.app.getFunction();
                if (function == null || function.trim().isEmpty()) {
                    System.out.println("No se ha ingresado una función. Usa la vista principal para ingresarla.");

                    // Mostrar animación de error
                    Timeline shakeAnimation = new Timeline(
                        new KeyFrame(Duration.millis(0), evt -> item.setTranslateX(0)),
                        new KeyFrame(Duration.millis(50), evt -> item.setTranslateX(5)),
                        new KeyFrame(Duration.millis(100), evt -> item.setTranslateX(-5)),
                        new KeyFrame(Duration.millis(150), evt -> item.setTranslateX(5)),
                        new KeyFrame(Duration.millis(200), evt -> item.setTranslateX(0))
                    );
                    shakeAnimation.play();
                } else if (item.getText().contains("Solución")) {
                    // Animación de transición entre escenas
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), btnMainMenu.getScene().getRoot());
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(ev -> {
                        App.app.setScene(Paths.METODO_PTO_FIJO);
                    });
                    fadeOut.play();
                }
            }

            popup.hide();
        });
    }

    /**
     * Shows the large menu when hovering over the main menu button
     */
    private void showLargeMenu(MouseEvent event) {
        hideDelay.stop();
        menuGrid.getStyleClass().add("showing");

        // Calculate position relative to the window
        Window window = btnMainMenu.getScene().getWindow();
        Bounds buttonBounds = btnMainMenu.localToScreen(btnMainMenu.getBoundsInLocal());

        // Position the menu below the button
        double menuX = buttonBounds.getMinX();
        double menuY = buttonBounds.getMaxY() + MENU_OFFSET_Y;

        // Animation for the menu
        menuGrid.setOpacity(0);
        menuGrid.setScaleY(0.8);

        popup.show(window, menuX, menuY);

        // Apply animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), menuGrid);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), menuGrid);
        scaleIn.setFromY(0.8);
        scaleIn.setToY(1.0);

        ParallelTransition parallelIn = new ParallelTransition(fadeIn, scaleIn);
        parallelIn.play();
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
                setupSubMenu(item6, button, new String[]{
                    "Método de Deflación",
                    "Método de Raíces de Pol_2",
                    "Método de Raíces de Pol_3"
                }); // Pasar el botón padre y los items del submenú
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

    protected void setupSubMenu(Label label, Button parentButton, String[] subItems) {
        // Define a method to show the submenu
        Runnable showSubmenu = () -> {
            subHideDelay.stop();
            subPopupContent.getChildren().clear();
            subPopupContent.getStyleClass().add("showing");

            // Create submenu items
            Label[] subLabels = new Label[subItems.length];
            for (int i = 0; i < subItems.length; i++) {
                subLabels[i] = new Label(subItems[i]);
                subLabels[i].getStyleClass().add("menu-item");

                // Animación para los elementos del submenú
                setupMenuItemAnimation(subLabels[i], i);

                // Setup click handler
                final int index = i;
                subLabels[i].setOnMouseClicked(e -> {
                    System.out.println("Seleccionaste: " + subLabels[index].getText());
                    popup.hide();
                    subPopup.hide();
                });
            }

            subPopupContent.getChildren().addAll(subLabels);

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
        };

        // Set up mouse enter event to show submenu
        label.setOnMouseEntered(event -> {
            showSubmenu.run();
        });

        // Also set up click event to show submenu
        label.setOnMouseClicked(event -> {
            showSubmenu.run();
            event.consume(); // Prevent event from bubbling up
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

    private void setupDynamicButton(Button btn) {
        // Check if button is null to prevent NullPointerException
        if (btn == null) {
            return;
        }

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), btn);

        // Animación al pasar mouse
        btn.setOnMouseEntered(event -> {
            scaleTransition.setToX(1.1);
            scaleTransition.setToY(1.1);
            scaleTransition.play();
        });
        // Animación al salir del mouse
        btn.setOnMouseExited(event -> {
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();
        });
        // Animación al presionar
        btn.setOnMousePressed(event -> {
            scaleTransition.setToX(0.95);
            scaleTransition.setToY(0.95);
            scaleTransition.play();
        });
        // Restaurar tamaño al soltar
        btn.setOnMouseReleased(event -> {
            scaleTransition.setToX(1.1);
            scaleTransition.setToY(1.1);
            scaleTransition.play();
        });
    }

    @FXML
    abstract void initialize();
}
