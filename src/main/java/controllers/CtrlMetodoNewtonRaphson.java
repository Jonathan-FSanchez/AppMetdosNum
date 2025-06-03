package controllers;

import com.example.prueba.ConvertidorLatex;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.InfoMetodoNewtonRaphson;
import model.MetodoNewtonRaphson;

public class CtrlMetodoNewtonRaphson extends TopBarController {

    @FXML private TableColumn<InfoMetodoNewtonRaphson, Integer> colIteracion;
    @FXML private TableColumn<InfoMetodoNewtonRaphson, Double> colX;
    @FXML private TableColumn<InfoMetodoNewtonRaphson, Double> colFx;
    @FXML private TableColumn<InfoMetodoNewtonRaphson, Double> colDfx;
    @FXML private TableColumn<InfoMetodoNewtonRaphson, Double> colNextX;
    @FXML private TableColumn<InfoMetodoNewtonRaphson, Double> colError;
    @FXML private TableView<InfoMetodoNewtonRaphson> tblNewtonRaphson;
    @FXML private TextField txtX0;
    @FXML private TextField txtError;
    @FXML private TextField txtMaxIteraciones;

    private String userFunction; // Variable para almacenar la función ingresada

    @FXML
    void initialize() {
        initializeTopBar();

        // Configuración de las columnas
        colIteracion.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getIteracion()).asObject());
        colX.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getX()).asObject());
        colFx.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getFx()).asObject());
        colDfx.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getDfx()).asObject());
        colNextX.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getNextX()).asObject());
        colError.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getError()).asObject());
    }

    @FXML
    void ejecutar(ActionEvent event) {
        tblNewtonRaphson.getItems().clear();

        if (userFunction == null || userFunction.trim().isEmpty()) {
            System.out.println("No se ha ingresado una función. Usa la vista principal para ingresarla.");
            return;
        }

        try {
            // Obtener valores de los TextFields
            double x0 = Double.parseDouble(txtX0.getText());
            double error = Double.parseDouble(txtError.getText());
            int maxIteraciones = Integer.parseInt(txtMaxIteraciones.getText());

            // Ejecutar el método de Newton-Raphson
            MetodoNewtonRaphson metodo = new MetodoNewtonRaphson();
            metodo.metodoNewtonRaphson(x0, error, maxIteraciones, tblNewtonRaphson, userFunction);
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