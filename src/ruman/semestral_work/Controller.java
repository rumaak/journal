package ruman.semestral_work;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.Text;

public class Controller {
    @FXML CustomHTMLEditor editor;
    @FXML Button test_but;
    @FXML Text test_text;
    @FXML TreeView note_tree;

    @FXML
    void initialize() {
        TreeItem root = new TreeItem("Root item");

        TreeItem ti1 = new TreeItem("Item 1");
        TreeItem ti2 = new TreeItem("Item 2");
        TreeItem ti3 = new TreeItem("Item 3");

        note_tree.setRoot(root);
        root.getChildren().addAll(ti1, ti2, ti3);

        // New buttons should enable / disable together with original ones
        editor.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            Node node = editor.lookup(".html-editor-foreground");
            if (node instanceof ColorPicker) {
                node.disabledProperty().addListener((observableValue1, aBoolean1, t11) -> {
                    editor.add_group_button.setDisable(observableValue1.getValue());
                    editor.save_button.setDisable(observableValue1.getValue());
                    editor.image_button.setDisable(observableValue1.getValue());
                });
            }
        });
    }

    @FXML
    void handleTestButtClick() {
        test_text.setText("curious");
    }

}
