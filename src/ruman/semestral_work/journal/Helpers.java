package ruman.semestral_work.journal;

import javafx.scene.control.Alert;

/**
 * Set of static methods used in application that don't quite fit anywhere.
 */
public class Helpers {
    /**
     * Present user with an error an end application.
     *
     * @param message   message that will be displayed to user
     */
    static void alertErrorExit(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

        System.exit(1);
    }

    /**
     * Present user with a warning and continue in execution.
     *
     * @param message   message that will be displayed to user
     */
    static void alertWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
