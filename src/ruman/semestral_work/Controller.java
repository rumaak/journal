package ruman.semestral_work;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Controller {
    @FXML Button add_group_button1;
    @FXML Button add_note_button1;
    @FXML Button remove_button1;
    @FXML Button rename_button1;
    @FXML CustomHTMLEditor editor;
    @FXML TreeView<FileTree> note_tree;

    AppState appState;

    @FXML
    void initialize() {
        try {
            setupTreeViewButtons();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setupEditor();

        appState = new AppState();
        appState.changeConfiguration("test_journal/");
        appState.loadConfiguration();
        if (appState.configurationExists()) {
            appState.loadTree();
            TreeItem<FileTree> root = new TreeItem<>(appState.fileTree);
            note_tree.setRoot(root);
            try {
                fillTreeView(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        setupNoteTree();
    }

    void fillTreeView(TreeItem<FileTree> root) throws IOException {
        for (FileTree tree : root.getValue().descendants) {
            TreeItem<FileTree> t;
            if (tree.type == ElementType.FILE) {
                t = new TreeItem<>(tree, getGraphic("note.png"));
            } else {
                t = new TreeItem<>(tree);
            }

            fillTreeView(t);
            root.getChildren().add(t);
        }
    }

    ImageView getGraphic(String image_name) throws IOException {
        Path path = Paths.get("resources").resolve(image_name);
        return new ImageView(new Image(Files.newInputStream(path)));
    }

    void setupTreeViewButtons() throws IOException {
        add_group_button1.setGraphic(getGraphic("add_group_btn.png"));
        add_note_button1.setGraphic(getGraphic("add_note_btn.png"));
        remove_button1.setGraphic(getGraphic("remove_btn.png"));
        rename_button1.setGraphic(getGraphic("rename_btn.png"));

        add_group_button1.setOnAction(arg0 -> {
            System.out.println("Added group!");
        });
        add_note_button1.setOnAction(arg0 -> {
            System.out.println("Added note!");
        });
        remove_button1.setOnAction(arg0 -> {
            System.out.println("Removed group / note!");
        });
        rename_button1.setOnAction(arg0 -> {
            System.out.println("Renamed group / note!");
        });
    }

    void setupEditor() {
        // New buttons should enable / disable together with original ones
        editor.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            Node node = editor.lookup(".html-editor-foreground");
            if (node instanceof ColorPicker) {
                node.disabledProperty().addListener((observableValue1, aBoolean1, t11) -> {
                    editor.save_button.setDisable(observableValue1.getValue());
                    editor.image_button.setDisable(observableValue1.getValue());
                });
            }
        });
        editor.setDisable(true);
    }

    void setupNoteTree() {
        note_tree.getSelectionModel().selectedItemProperty().addListener((observableValue, fileTreeTreeItem, t1) -> {
            if ((t1 != null) && (t1.getValue().type != ElementType.DIRECTORY)) {
                editor.setDisable(false);
                Path path = appState.resolveJournalPath(t1.getValue().path);

                try {
                    editor.setHtmlText(Files.readString(path));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                editor.setDisable(true);
            }
        });
    }
}
