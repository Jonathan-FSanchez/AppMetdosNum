package application;

import controllers.CtrlMetodoBiseccion;
import controllers.CtrlMetodoPtoFijo; // Agregar import para el controlador de punto fijo
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import utils.Paths;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class App extends Application {
    private static final double WINDOW_WIDTH = 1121.0;
    private static final double WINDOW_HEIGHT = 803.0;
    public static App app;
    private Stage stageW;
    private String userFunction; // Para almacenar la función ingresada
    private Map<String, Object> controllers = new HashMap<>(); // Mapa para almacenar controladores

    @Override
    public void start(Stage stage) throws Exception {
        app = this;
        stageW = stage;
        setScene(Paths.MENU_PRINCIPAL);
    }

    public static void main(String[] args) {
        launch();
    }

    public void setScene(String path) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        try {
            BorderPane pane = loader.load();

            Scene scene = new Scene(pane, WINDOW_WIDTH, WINDOW_HEIGHT);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

            stageW.setScene(scene);
            stageW.setTitle("Menú Principal");
            stageW.setResizable(false);
            stageW.setWidth(WINDOW_WIDTH);
            stageW.setHeight(WINDOW_HEIGHT);
            stageW.show();

            // Almacenar el controlador
            Object controller = loader.getController();
            controllers.put(path, controller);

            // Pasar la función al controlador correspondiente
            if (userFunction != null) {
                if (path.equals(Paths.METODO_BIS) && controller instanceof CtrlMetodoBiseccion) {
                    ((CtrlMetodoBiseccion) controller).setUserFunction(userFunction);
                } else if (path.equals(Paths.METODO_PTO_FIJO) && controller instanceof CtrlMetodoPtoFijo) {
                    ((CtrlMetodoPtoFijo) controller).setUserFunction(userFunction);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setFunction(String function) {
        this.userFunction = function;
    }

    public String getFunction() {
        return userFunction;
    }

    public Object getController(String path) {
        return controllers.get(path);
    }
}














/*
package application;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import utils.Paths;

import java.io.IOException;

public class App extends Application {
    private static final double WINDOW_WIDTH = 1121.0;
    private static final double WINDOW_HEIGHT = 803.0;
    public static App app;
    private Stage stageW;

    @Override
    public void start(Stage stage) throws Exception {
        app = this;
        stageW = stage;
        setScene(Paths.MENU_PRINCIPAL);

    }


    public static void main(String[] args){
        launch();
    }

    public void setScene(String path){
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        try{
            BorderPane pane = loader.load();

            Scene scene = new Scene(pane, WINDOW_WIDTH, WINDOW_HEIGHT);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

            stageW.setScene(scene);
            stageW.setTitle("Menú Principal");
            stageW.setResizable(false);
            stageW.setWidth(WINDOW_WIDTH);  // Establecer ancho fijo
            stageW.setHeight(WINDOW_HEIGHT);
            stageW.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
*/