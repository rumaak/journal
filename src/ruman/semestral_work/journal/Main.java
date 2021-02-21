package ruman.semestral_work.journal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Application entry point class.
 */
public class Main extends Application {

    /**
     * Loads graphical interface of application.
     */
    @Override
    public void start(Stage primary_stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("journal.fxml"));
        Parent root = loader.load();
        primary_stage.setTitle("Journal");
        primary_stage.setScene(new Scene(root));

        Controller controller = loader.getController();
        controller.setupKeyboardShortcuts();

        primary_stage.show();
    }

    /**
     * Application entry point function.
     */
    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}