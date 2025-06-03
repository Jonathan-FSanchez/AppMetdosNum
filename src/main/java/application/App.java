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
        // Suppress GTK warnings
        System.setProperty("jdk.gtk.version", "2");
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("javafx.gtk.warning", "false");
        System.setProperty("glass.gtk.uiScale", "1.0");

        // Suppress XSetErrorHandler warning
        System.setProperty("sun.awt.disablegrab", "true");

        launch();
    }

    public void setScene(String path) {
        System.out.println("Intentando cargar la escena: " + path);

        // Verificar que la ruta no sea null
        if (path == null) {
            System.err.println("Error: La ruta es null");
            return;
        }

        // Verificar que el recurso existe
        if (getClass().getResource(path) == null) {
            System.err.println("Error: No se pudo encontrar el recurso: " + path);
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        try {
            System.out.println("Cargando el archivo FXML: " + path);
            BorderPane pane = loader.load();
            System.out.println("Archivo FXML cargado correctamente");

            Scene scene = new Scene(pane, WINDOW_WIDTH, WINDOW_HEIGHT);

            try {
                scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            } catch (Exception e) {
                System.err.println("Error al cargar la hoja de estilos: " + e.getMessage());
                // Continuar sin la hoja de estilos
            }

            stageW.setScene(scene);
            stageW.setTitle("Menú Principal");
            stageW.setResizable(false);
            stageW.setWidth(WINDOW_WIDTH);
            stageW.setHeight(WINDOW_HEIGHT);
            stageW.show();
            System.out.println("Escena configurada y mostrada correctamente");

            // Almacenar el controlador
            Object controller = loader.getController();
            controllers.put(path, controller);
            System.out.println("Controlador almacenado: " + (controller != null ? controller.getClass().getName() : "null"));

            // Pasar la función al controlador correspondiente
            if (userFunction != null) {
                if (path.equals(Paths.METODO_BIS) && controller instanceof CtrlMetodoBiseccion) {
                    ((CtrlMetodoBiseccion) controller).setUserFunction(userFunction);
                    System.out.println("Función pasada al controlador de Bisección");
                } else if (path.equals(Paths.METODO_PTO_FIJO) && controller instanceof CtrlMetodoPtoFijo) {
                    ((CtrlMetodoPtoFijo) controller).setUserFunction(userFunction);
                    System.out.println("Función pasada al controlador de Punto Fijo");
                }
            }
        } catch (IOException e) {
            System.err.println("Error al cargar el archivo FXML: " + path);
            System.err.println("Mensaje de error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al cargar la escena: " + path, e);
        } catch (Exception e) {
            System.err.println("Error inesperado al configurar la escena: " + path);
            System.err.println("Mensaje de error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error inesperado al configurar la escena: " + path, e);
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
