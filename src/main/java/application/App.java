package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            // Cargar el archivo FXML
            Parent root = FXMLLoader.load(getClass().getResource("/views/menu.fxml"));

            // Crear la escena
            Scene scene = new Scene(root);

            // Cargar el archivo CSS
            scene.getStylesheets().add(getClass().getResource("/CSS/menu.css").toExternalForm());

            // Configuración del Stage
            stage.setScene(scene);
            stage.setTitle("Aplicación JavaFX");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
