package application;

import Controller.MenuController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {

    private double xOffset = 0;
    private double yOffset = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/menu.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 600); // Tamaño inicial más grande
            scene.getStylesheets().add(getClass().getResource("/CSS/menu.css").toExternalForm());

            String imagePath = getClass().getResource("/images/fondo.jpg").toExternalForm();
            System.out.println("Ruta de la imagen: " + imagePath);
            if (imagePath == null) {
                System.out.println("¡La imagen no se encuentra en /images/fondo.jpg!");
            }

            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(scene);
            stage.setMinWidth(600); // Ajustado para más flexibilidad
            stage.setMinHeight(400);

            MenuController controller = loader.getController();
            HBox navBar = controller.getNavBar();
            navBar.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            navBar.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}