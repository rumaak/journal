package ruman.semestral_work;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
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
            Helpers.alertErrorExit("Couldn't load a TreeView button image!");
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

        Tooltip add_group_tooltip = new Tooltip("Add group");
        Tooltip add_note_tooltip = new Tooltip("Add note");
        Tooltip remove_tooltip = new Tooltip("Remove group / note");
        Tooltip rename_tooltip = new Tooltip("Rename group / note");

        add_group_button1.setTooltip(add_group_tooltip);
        add_note_button1.setTooltip(add_note_tooltip);
        remove_button1.setTooltip(remove_tooltip);
        rename_button1.setTooltip(rename_tooltip);

        add_group_button1.setOnAction(arg0 -> addItem(ElementType.DIRECTORY));
        add_note_button1.setOnAction(arg0 -> addItem(ElementType.FILE));
        remove_button1.setOnAction(arg0 -> {
            TreeItem<FileTree> item = note_tree.getSelectionModel().getSelectedItem();
            TreeItem<FileTree> parent_item = item.getParent();
            FileTree tree = item.getValue();
            FileTree parent_tree = parent_item.getValue();

            // Remove from TreeView
            parent_item.getChildren().remove(item);

            // Remove files
            try {
                tree.delete(appState);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Remove from parent tree
            parent_tree.descendants.remove(tree);
        });
        rename_button1.setOnAction(arg0 -> {
            TreeItem<FileTree> target = note_tree.getSelectionModel().getSelectedItem();
            note_tree.setEditable(true);
            note_tree.edit(target);
            note_tree.setEditable(false);
        });

        setTreeViewButtonsDisabled(true);
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
                Files.deleteIfExists(file_path);
                Files.writeString(file_path, editor.getHtmlText(), CREATE);
            } catch (IOException e) {
                Helpers.alertWarning("Couldn't save file!");
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
                    Helpers.alertErrorExit("Couldn't read a selected file!");
                }

            } else {
                editor.setDisable(true);
            }

            setTreeViewButtonsDisabled(t1 == null);
        });

        // Auto-select root element (for user-friendliness - otherwise all buttons are disabled on start)
        TreeItem<FileTree> root = note_tree.getRoot();
        note_tree.getSelectionModel().select(root);

        note_tree.setCellFactory(fileTreeTreeView -> new FileTreeCellImpl());
    }

    void setupAppState() {
        // Attempt to load existing config
        appState = new AppState();
        appState.loadConfiguration();

        // Force user to select a folder where journal will be stored (if not selected already)
        if (!appState.configurationExists()) {
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
                appState.changeConfiguration(dir.toString(), "Journal");
            }

            stage.close();
        }

        appState.loadTree();
        TreeItem<FileTree> root = new TreeItem<>(appState.fileTree);
        note_tree.setRoot(root);

        try {
            fillTreeView(root);
        } catch (IOException e) {
            Helpers.alertErrorExit("Couldn't load a note image!");
        }
    }

    void setTreeViewButtonsDisabled(boolean new_value) {
        add_group_button1.setDisable(new_value);
        add_note_button1.setDisable(new_value);
        remove_button1.setDisable(new_value);
        rename_button1.setDisable(new_value);
    }

    void addItem(ElementType type) {
        // Get node to which we are going to append a child
        TreeItem<FileTree> tree_item = note_tree.getSelectionModel().getSelectedItem();
        if (tree_item.getValue().type == ElementType.FILE) {
            tree_item = tree_item.getParent();
        }

        // Create new TreeItem and FileTree
        FileTree file_tree = tree_item.getValue();
        String new_name = getNewName(type, file_tree);
        Path new_path = file_tree.path.resolve(new_name);
        FileTree new_file_tree = new FileTree(new_name, new_path, type);
        TreeItem<FileTree> new_item;
        if (type == ElementType.FILE) {
            try {
                new_item = new TreeItem<>(new_file_tree, getGraphic("note.png"));
            } catch (IOException e) {
                new_item = new TreeItem<>(new_file_tree);
                e.printStackTrace();
            }
        } else {
            new_item = new TreeItem<>(new_file_tree);
        }

        // Add to existing counterparts
        file_tree.descendants.add(new_file_tree);
        tree_item.getChildren().add(new_item);

        // Do corresponding changes on filesystem level
        try {
            new_file_tree.save(appState);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getNewName(ElementType type, FileTree file_tree) {
        StringBuilder new_name = new StringBuilder("");
        new_name.append("Item ");
        new_name.append(FileTree.created);
        StringBuilder new_name_full = new StringBuilder(new_name);
        if (type == ElementType.FILE) {
            new_name_full.append(".html");
        }

        boolean found = true;
        while (found) {
            found = false;
            for (FileTree d : file_tree.descendants) {
                if (d.name.equals(new_name_full.toString())) {
                    found = true;
                    break;
                } else {
                    found = false;
                }
            }
            if (found) {
                new_name.append("_");
                new_name_full = new StringBuilder(new_name);
                if (type == ElementType.FILE) {
                    new_name_full.append(".html");
                }
            }
        }

        return new_name_full.toString();
    }

    public void setupKeyboardShortcuts() {
        // Setup ctrl+S combination to fire save button
        editor.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN), () -> {
            if (!editor.save_button.isDisabled()) {
                editor.save_button.fire();
            }
        });
    }

    private final class FileTreeCellImpl extends TreeCell<FileTree> {
        // This field is used only when editing
        private TextField textField;

        @Override
        public void startEdit() {
            super.startEdit();

            if (textField == null) {
                createTextField();
            }
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem().toString());
            setGraphic(getTreeItem().getGraphic());
        }

        @Override
        public void updateItem(FileTree item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(getTreeItem().getGraphic());
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setOnKeyReleased(t -> {
                if (t.getCode() == KeyCode.ENTER) {
                    TreeItem<FileTree> tree_item = getTreeItem();
                    FileTree file_tree = getItem();

                    boolean successful = changeItemName(tree_item, file_tree);
                    if (successful) {
                        commitEdit(file_tree);
                    } else {
                        Helpers.alertWarning("There cannot be two elements with identical name in the same group!");
                        cancelEdit();
                    }

                } else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }

        private boolean changeItemName(TreeItem<FileTree> tree_item, FileTree file_tree) {
            String new_name = textField.getText();
            if (file_tree.type == ElementType.FILE) {
                new_name += ".html";
            }

            // Root element translates to root directory, which we do not wish to rename
            if (tree_item == getTreeView().getRoot()) {
                appState.changeConfiguration(appState.getJournalDirectory(), new_name);
                file_tree.name = new_name;
            } else {

                if (alreadyExists(file_tree, tree_item.getParent(), new_name)) {
                    return false;
                }

                try {
                    file_tree.rename(new_name, appState);
                } catch (IOException e) {
                    Helpers.alertErrorExit("An error occurred during changing element name!");
                }
            }
            return true;
        }

        private boolean alreadyExists(FileTree file_tree, TreeItem<FileTree> parent, String new_name) {
            for (FileTree d : parent.getValue().descendants) {
                if (d.name.equals(new_name) && (d != file_tree)) {
                    return true;
                }
            }
            return false;
        }
    }
}
