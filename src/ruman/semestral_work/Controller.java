package ruman.semestral_work;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

public class Controller {
    @FXML CustomHTMLEditor editor;
    @FXML TreeView<String> note_tree;

    @FXML
    void initialize() {
        TreeItem<String> root = new TreeItem<>("Root item");

        TreeItem<String> ti1 = new TreeItem<>("Item 1");
        TreeItem<String> ti2 = new TreeItem<>("Item 2");
        TreeItem<String> ti3 = new TreeItem<>("Item 3");

        note_tree.setRoot(root);
        root.getChildren().addAll(ti1, ti2, ti3);

        // New buttons should enable / disable together with original ones
        editor.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            Node node = editor.lookup(".html-editor-foreground");
            if (node instanceof ColorPicker) {
                node.disabledProperty().addListener((observableValue1, aBoolean1, t11) -> {
                    editor.save_button.setDisable(observableValue1.getValue());
                    editor.image_button.setDisable(observableValue1.getValue());

                    editor.add_group_button.setDisable(observableValue1.getValue());
                    editor.add_note_button.setDisable(observableValue1.getValue());
                    editor.remove_button.setDisable(observableValue1.getValue());
                    editor.rename_button.setDisable(observableValue1.getValue());
                });
            }
        });

        Loader loader = new Loader();
        loader.loadConfiguration();
        if (loader.configurationExists()) {
            loader.loadTree();
            // TODO pass the data here and do something with them
        } else {
            // TODO default to something
        }
        loader.changeConfiguration("test_journal/");
    }
}
