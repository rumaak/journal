package ruman.semestral_work;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("journal.fxml"));
            Parent root = loader.load();
            primaryStage.setTitle("Journal");
            primaryStage.setScene(new Scene(root));

            Controller c = loader.getController();
            c.setupKeyboardShortcuts();

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}