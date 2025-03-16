package controllers;

import com.example.prueba.ConvertidorLatex;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
        for (String symbol : symbols) {
            javafx.scene.control.Button button = new javafx.scene.control.Button(symbol);
            button.setPrefSize(40, 40);
            button.setStyle("-fx-background-color: #6a0dad; -fx-text-fill: white; -fx-font-size: 14px;");
            button.setFocusTraversable(false); // Deshabilitar que el botón reciba foco
            button.setOnAction(event -> {
                String currentText = txtFunction.getText();
                txtFunction.setText(currentText + symbol);
                updateLatexRender(currentText + symbol); // Actualizar renderizado
                Platform.runLater(() -> {
                    txtFunction.requestFocus(); // Forzar foco
                    txtFunction.positionCaret(txtFunction.getText().length()); // Mover cursor al final
                });
            });
            symbolButtons.getChildren().add(button);
        }
    }

    @FXML
    void confirmFunction() {
        String function = txtFunction.getText().trim();
        if (!function.isEmpty()) {
            String latexFunction = ConvertidorLatex.toLatex(function);
            System.out.println("Función en LaTeX: " + latexFunction);
            App.app.setFunction(function);
            System.out.println("Función guardada en App: " + App.app.getFunction());
        } else {
            System.out.println("Por favor, ingresa una función.");
        }
    }
}