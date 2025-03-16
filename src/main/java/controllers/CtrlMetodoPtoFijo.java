package controllers;

import com.example.prueba.ConvertidorLatex;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.InfoMetodoPtoFijo;
import model.MetodoPtoFijo;

import java.awt.*;

public class CtrlMetodoPtoFijo extends TopBarController {

    @FXML private TableColumn<InfoMetodoPtoFijo, Double> colError;
    @FXML private TableColumn<InfoMetodoPtoFijo, Integer> colN;
    @FXML private TableColumn<InfoMetodoPtoFijo, Double> colX;
    @FXML private TableColumn<InfoMetodoPtoFijo, Double> colNextX;
    @FXML private TableView<InfoMetodoPtoFijo> tblPtoFijo;
    @FXML private TextField txtA;
    @FXML private TextField txtX;
    @FXML private TextField txtB;
    @FXML private TextField txtErMax;
    @FXML private TextField txtIMax;

    private String userFunction; // Variable para almacenar la función ingresada

    @FXML
    void initialize() {
        initializeTopBar();

        // Configuración de las columnas
        colN.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getN()).asObject());
        colX.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getX()).asObject());
        colNextX.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getNextX()).asObject());
        colError.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getErAct()).asObject());
    }

    @FXML
    void ejecutar(ActionEvent event) {
        tblPtoFijo.getItems().clear();

        if (userFunction == null || userFunction.trim().isEmpty()) {
            System.out.println("No se ha ingresado una función. Usa la vista principal para ingresarla.");
            return;
        }

        // Obtener valores de los TextFields
        double a = Double.parseDouble(txtA.getText());
        double b = Double.parseDouble(txtB.getText());
        double x = Double.parseDouble(txtX.getText());
        System.out.println("x_ctrl: " + x);
        double erMax = Double.parseDouble(txtErMax.getText());
        int iMax = Integer.parseInt(txtIMax.getText());

        // Crear instancia de InfoMetodoPtoFijo con valores iniciales
        InfoMetodoPtoFijo inf = new InfoMetodoPtoFijo(a, b, x, 0, erMax, iMax);
        MetodoPtoFijo metodo = new MetodoPtoFijo();
        metodo.metodoPtoFijo(inf, tblPtoFijo, userFunction); // Ejecutar el método
    }

    // Método para evaluar la función ingresada por el usuario
    private double evaluateFunction(double x) {
        return ConvertidorLatex.evaluate(userFunction, x);
    }

    // Método para establecer la función desde CtrlMenu (ajusta según tu lógica)
    public void setUserFunction(String function) {
        this.userFunction = function;
        String latexFunction = ConvertidorLatex.toLatex(function);
        System.out.println("Función traducida a LaTeX: " + latexFunction);
    }
}