package controllers;

import com.example.prueba.ConvertidorLatex;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.InfoMetodoMuller;
import model.MetodoMuller;

public class CtrlMetodoMuller extends TopBarController {

    @FXML private TableColumn<InfoMetodoMuller, Integer> colIteracion;
    @FXML private TableColumn<InfoMetodoMuller, Double> colX0;
    @FXML private TableColumn<InfoMetodoMuller, Double> colX1;
    @FXML private TableColumn<InfoMetodoMuller, Double> colX2;
    @FXML private TableColumn<InfoMetodoMuller, Double> colX3;
    @FXML private TableColumn<InfoMetodoMuller, Double> colError;
    @FXML private TableView<InfoMetodoMuller> tblMuller;
    @FXML private TextField txtX0;
    @FXML private TextField txtX1;
    @FXML private TextField txtX2;
    @FXML private TextField txtError;
    @FXML private TextField txtMaxIteraciones;

    private String userFunction; // Variable para almacenar la función ingresada

    @FXML
    void initialize() {
        initializeTopBar();

        // Configuración de las columnas
        colIteracion.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getIteracion()).asObject());
        colX0.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getX0()).asObject());
        colX1.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getX1()).asObject());
        colX2.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getX2()).asObject());
        colX3.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getX3()).asObject());
        colError.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getError()).asObject());
    }

    @FXML
    void ejecutar(ActionEvent event) {
        tblMuller.getItems().clear();

        if (userFunction == null || userFunction.trim().isEmpty()) {
            System.out.println("No se ha ingresado una función. Usa la vista principal para ingresarla.");
            return;
        }

        try {
            // Obtener valores de los TextFields
            double x0 = Double.parseDouble(txtX0.getText());
            double x1 = Double.parseDouble(txtX1.getText());
            double x2 = Double.parseDouble(txtX2.getText());
            double error = Double.parseDouble(txtError.getText());
            int maxIteraciones = Integer.parseInt(txtMaxIteraciones.getText());

            // Ejecutar el método de Muller
            MetodoMuller metodo = new MetodoMuller();
            metodo.metodoMuller(x0, x1, x2, error, maxIteraciones, tblMuller, userFunction);
        } catch (NumberFormatException e) {
            System.out.println("Error al convertir los valores. Asegúrate de ingresar números válidos.");
        } catch (Exception e) {
            System.out.println("Error al ejecutar el método: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para evaluar la función ingresada por el usuario
    private double evaluateFunction(double x) {
        return ConvertidorLatex.evaluate(userFunction, x);
    }

    // Método para establecer la función desde CtrlMenu
    public void setUserFunction(String function) {
        this.userFunction = function;
        String latexFunction = ConvertidorLatex.toLatex(function);
        System.out.println("Función traducida a LaTeX: " + latexFunction);
    }
}