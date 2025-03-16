package controllers;

import com.example.prueba.ConvertidorLatex; // Añadido
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.InfoMetodoBiseccion;
import model.MetodoBiseccion;

public class CtrlMetodoBiseccion extends TopBarController {

    @FXML private TableColumn<InfoMetodoBiseccion, Float> colA;
    @FXML private TableColumn<InfoMetodoBiseccion, Float> colB;
    @FXML private TableColumn<InfoMetodoBiseccion, Float> colC;
    @FXML private TableColumn<InfoMetodoBiseccion, Float> colError;
    @FXML private TableColumn<InfoMetodoBiseccion, Float> colFa;
    @FXML private TableColumn<InfoMetodoBiseccion, Float> colFb;
    @FXML private TableColumn<InfoMetodoBiseccion, Float> colFc;
    @FXML private TableColumn<InfoMetodoBiseccion, Integer> colN;
    @FXML private TableView<InfoMetodoBiseccion> tblMBiseccion;
    @FXML private MenuItem menuMBiseccion;
    @FXML private TextField txtA;
    @FXML private TextField txtB;
    @FXML private TextField txtErMax;
    @FXML private TextField txtIMax;

    private String userFunction; // Variable para almacenar la función ingresada

    @FXML
    void initialize() {
        initializeTopBar();

        // Configuración de las columnas
        colN.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getiMax()).asObject());
        colA.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty(cellData.getValue().getA()).asObject());
        colB.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty(cellData.getValue().getB()).asObject());
        colC.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty(cellData.getValue().getC()).asObject());
        colFa.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty((float) evaluateFunction(cellData.getValue().getA())).asObject());
        colFb.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty((float) evaluateFunction(cellData.getValue().getB())).asObject());
        colFc.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty((float) evaluateFunction(cellData.getValue().getC())).asObject());
        colError.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty(cellData.getValue().getErAct()).asObject());
    }

    @FXML
    void ejecutar(ActionEvent event) {
        tblMBiseccion.getItems().clear();

        if (userFunction == null || userFunction.trim().isEmpty()) {
            System.out.println("No se ha ingresado una función. Usa la vista principal para ingresarla.");
            return;
        }

        InfoMetodoBiseccion inf = new InfoMetodoBiseccion();
        inf.setA(Float.parseFloat(txtA.getText()));
        inf.setB(Float.parseFloat(txtB.getText()));

        float a=Float.parseFloat(txtA.getText());
        float b=Float.parseFloat(txtB.getText());
        inf.setC((a + b) / 2);
        inf.setiMax(Integer.parseInt(txtIMax.getText()));
        inf.setErMax(Float.parseFloat(txtErMax.getText()));

        MetodoBiseccion metodo = new MetodoBiseccion();
        metodo.metodoBisec(inf, tblMBiseccion, userFunction); // Pasar la función al método
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

























/*
package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.InfoMetodoBiseccion;
import model.MetodoBiseccion;

import static model.MetodoBiseccion.f;

public class CtrlMetodoBiseccion extends TopBarController {

    @FXML private TableColumn<InfoMetodoBiseccion, Float> colA;
    @FXML private TableColumn<InfoMetodoBiseccion, Float> colB;
    @FXML private TableColumn<InfoMetodoBiseccion, Float> colC;
    @FXML private TableColumn<InfoMetodoBiseccion, Float> colError;
    @FXML private TableColumn<InfoMetodoBiseccion, Float> colFa;
    @FXML private TableColumn<InfoMetodoBiseccion, Float> colFb;
    @FXML private TableColumn<InfoMetodoBiseccion, Float> colFc;
    @FXML private TableColumn<InfoMetodoBiseccion, Integer> colN;
    @FXML private TableView<InfoMetodoBiseccion> tblMBiseccion;
    @FXML private MenuItem menuMBiseccion;
    @FXML private TextField txtA;
    @FXML private TextField txtB;
    @FXML private TextField txtErMax;
    @FXML private TextField txtIMax;

    @FXML
    @Override
    void initialize() {
        // Inicializar la barra superior heredada
        initializeTopBar();

        // Configuración específica de CtrlMetodoBiseccion
        colN.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getiMax()).asObject());
        colA.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty(cellData.getValue().getA()).asObject());
        colB.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty(cellData.getValue().getB()).asObject());
        colC.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty(cellData.getValue().getC()).asObject());
        colFa.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty((float) f(cellData.getValue().getA())).asObject());
        colFb.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty((float) f(cellData.getValue().getB())).asObject());
        colFc.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty((float) f(cellData.getValue().getC())).asObject());
        colError.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty(cellData.getValue().getErAct()).asObject());
    }

    @FXML
    void ejecutar(ActionEvent event) {
        tblMBiseccion.getItems().clear();

        InfoMetodoBiseccion inf = new InfoMetodoBiseccion();
        inf.setA(Float.parseFloat(txtA.getText()));
        inf.setB(Float.parseFloat(txtB.getText()));
        inf.setiMax(Integer.parseInt(txtIMax.getText()));
        inf.setErMax(Float.parseFloat(txtErMax.getText()));

        MetodoBiseccion info = new MetodoBiseccion();
        info.metodoBisec(inf, tblMBiseccion);
    }
}

*/
//Hay que hacer una ventana donde se pueda ingresar la funcion y los valores
// UN CONTROLADOR ES UNA CLASS Y CADA VIEW DEBE TENER SU PROPIO CONTROLADOR
// POR BUENA PRACTICA A CADA CONTROLLER LE CORRESPONDERA UNA VISTA, NO MAS DE UNA.