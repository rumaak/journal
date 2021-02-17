package ruman.semestral_work;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.CREATE;

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
        setupAppState();

        try {
            setupTreeViewButtons();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setupEditor();
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

        // By default, editor is disabled
        editor.setDisable(true);

        // Save button saves contents to file corresponding to currently edited note
        editor.save_button.setOnAction(arg0 -> {
            FileTree file_tree = note_tree.getSelectionModel().getSelectedItem().getValue();
            Path file_path = appState.resolveJournalPath(file_tree.path);
            try {
                Files.writeString(file_path, editor.getHtmlText(), CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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

    void setupAppState() {
        // Attempt to load existing config
        appState = new AppState();
        appState.loadConfiguration();

        // Force user to select a folder where journal will be stored (if not selected already)
        while (!appState.configurationExists()) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Journal location");

            // We have to use new window
            StackPane root = new StackPane();
            Stage stage = new Stage();
            stage.setTitle("New Stage Title");
            stage.setScene(new Scene(root, 150, 150));

            File selectedDirectory = directoryChooser.showDialog(stage.getScene().getWindow());
            if (selectedDirectory != null) {
                Path dir = selectedDirectory.toPath();
                appState.changeConfiguration(dir.toString());
            }

            stage.close();
        }

        appState.loadTree();
        TreeItem<FileTree> root = new TreeItem<>(appState.fileTree);
        note_tree.setRoot(root);

        try {
            fillTreeView(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
