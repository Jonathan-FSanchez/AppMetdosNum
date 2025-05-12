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
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import application.App;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import javafx.embed.swing.SwingNode;
import javafx.scene.control.Button;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

public class CtrlMenu extends TopBarController {
    @FXML private VBox menuContent;
    @FXML private TextField txtFunction;
    @FXML private SwingNode latexRender;
    @FXML private HBox symbolButtons;

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
        String[] symbols = {"√", "π", "sin", "cos", "tan", "e", "∫", "∂", "(", ")", "^"};
        for (int i = 0; i < symbols.length; i++) {
            String symbol = symbols[i];
            Button button = new Button(symbol);
            button.setPrefSize(40, 40);
            button.getStyleClass().add("symbol-button");
            button.setFocusTraversable(false); // Deshabilitar que el botón reciba foco
            
            // Añadir efecto de animación con retraso para entrada escalonada
            final int index = i;
            Platform.runLater(() -> {
                // Inicialmente fuera de vista
                button.setTranslateY(50);
                button.setOpacity(0);
                
                // Animación de entrada
                TranslateTransition translateIn = new TranslateTransition(Duration.millis(500), button);
                translateIn.setFromY(50);
                translateIn.setToY(0);
                
                FadeTransition fadeIn = new FadeTransition(Duration.millis(600), button);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                
                ParallelTransition parallelTransition = new ParallelTransition(translateIn, fadeIn);
                parallelTransition.setDelay(Duration.millis(100 + (index * 60))); // Retraso escalonado
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
                    updateLatexRender(currentText + symbol); // Actualizar renderizado
                    scaleUp.play();
                    
                    Platform.runLater(() -> {
                        txtFunction.requestFocus(); // Forzar foco
                        txtFunction.positionCaret(txtFunction.getText().length()); // Mover cursor al final
                    });
                });
                
                scaleDown.play();
            });
            
            symbolButtons.getChildren().add(button);
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