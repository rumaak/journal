package ruman.semestral_work.journal;

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

/**
 * Manages setup of GUI components, primarily their action handlers.
 */
public class Controller {
    private static final String NOTE_FILE = "note.png";
    private static final String RESOURCE_DIR = "resources";

    private static final String ADD_GROUP_BUTTON_FILE = "add_group_btn.png";
    private static final String ADD_NOTE_BUTTON_FILE = "add_note_btn.png";
    private static final String REMOVE_BUTTON_FILE = "remove_btn.png";
    private static final String RENAME_BUTTON_FILE = "rename_btn.png";

    private static final String EDITOR_FOREGROUND_IDENTIFIER = ".html-editor-foreground";

    @FXML Button add_group_button1;
    @FXML Button add_note_button1;
    @FXML Button remove_button1;
    @FXML Button rename_button1;
    @FXML CustomHTMLEditor editor;
    @FXML TreeView<FileTree> note_tree;

    AppState app_state;

    /**
     * Create individual components, connect them and provide with handlers.
     */
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

    /**
     * Fills {@link TreeView} component with items corresponding to journal entries and groups recursively.
     *
     * @param   root    {@link TreeItem} containing {@link FileTree} whose descendants we want to add
     */
    void fillTreeView(TreeItem<FileTree> root) throws IOException {
        for (FileTree tree : root.getValue().descendants) {
            TreeItem<FileTree> t;
            if (tree.type == ElementType.FILE) {
                t = new TreeItem<>(tree, getGraphic(NOTE_FILE));
            } else {
                t = new TreeItem<>(tree);
            }

            fillTreeView(t);
            root.getChildren().add(t);
        }
    }

    /**
     * Creates graphic corresponding to supplied to file name (file in {@link #RESOURCE_DIR}).
     *
     * @param   image_name  name of the image file
     * @return  graphic object created from image file
     */
    ImageView getGraphic(String image_name) throws IOException {
        Path path = Paths.get(RESOURCE_DIR).resolve(image_name);
        return new ImageView(new Image(Files.newInputStream(path)));
    }

    /**
     * Create buttons for TreeView management, assign handlers to them.
     */
    void setupTreeViewButtons() throws IOException {
        add_group_button1.setGraphic(getGraphic(ADD_GROUP_BUTTON_FILE));
        add_note_button1.setGraphic(getGraphic(ADD_NOTE_BUTTON_FILE));
        remove_button1.setGraphic(getGraphic(REMOVE_BUTTON_FILE));
        rename_button1.setGraphic(getGraphic(RENAME_BUTTON_FILE));

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

            if (note_tree.getRoot() == item) {
                Helpers.alertWarning("Root journal tree element cannot be deleted!");
                return;
            }

            TreeItem<FileTree> parent_item = item.getParent();
            FileTree tree = item.getValue();
            FileTree parent_tree = parent_item.getValue();

            // Remove from TreeView
            parent_item.getChildren().remove(item);

            // Remove files
            try {
                tree.delete(app_state);
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

    /**
     * Connects new buttons to the rest of editor buttons, sets action handler for {@link CustomHTMLEditor#save_button}.
     */
    void setupEditor() {
        // New buttons should enable / disable together with original ones
        editor.focusedProperty().addListener((observable_value, a_boolean, t1) -> {
            Node node = editor.lookup(EDITOR_FOREGROUND_IDENTIFIER);
            if (node instanceof ColorPicker) {
                node.disabledProperty().addListener((observable_value1, a_boolean1, t11) -> {
                    editor.save_button.setDisable(observable_value1.getValue());
                    editor.image_button.setDisable(observable_value1.getValue());
                });
            }
        });

        // By default, editor is disabled
        editor.setDisable(true);

        // Save button saves contents to file corresponding to currently edited note
        editor.save_button.setOnAction(arg0 -> {
            FileTree file_tree = note_tree.getSelectionModel().getSelectedItem().getValue();
            Path file_path = app_state.resolveJournalPath(file_tree.path);
            try {
                Files.deleteIfExists(file_path);
                Files.writeString(file_path, editor.getHtmlText(), CREATE);
            } catch (IOException e) {
                Helpers.alertWarning("Couldn't save file!");
            }
        });
    }

    /**
     * Sets up selection functionality of {@link TreeView} component {@link #note_tree} (connects it with buttons that
     * manage it and editor), assigns a custom cell factory to it.
     */
    void setupNoteTree() {
        note_tree.getSelectionModel().selectedItemProperty().addListener((observable_value, file_tree_tree_item, t1) -> {
            if ((t1 != null) && (t1.getValue().type != ElementType.DIRECTORY)) {
                editor.setDisable(false);
                Path path = app_state.resolveJournalPath(t1.getValue().path);

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

        note_tree.setCellFactory(file_tree_tree_view -> new FileTreeCellImpl());
    }

    /**
     * Initializes AppState instance corresponding to currently running Journal application with existing / new
     * configuration, displays the journal in TreeView component.
     */
    void setupAppState() {
        // Attempt to load existing config
        app_state = new AppState();
        app_state.loadConfiguration();

        // Force user to select a folder where journal will be stored (if not selected already)
        if (!app_state.configurationExists()) {
            DirectoryChooser directory_chooser = new DirectoryChooser();
            directory_chooser.setTitle("Journal location");

            // We have to use new window
            StackPane root = new StackPane();
            Stage stage = new Stage();
            stage.setTitle("New Stage Title");
            stage.setScene(new Scene(root, 150, 150));

            File selected_directory = directory_chooser.showDialog(stage.getScene().getWindow());
            if (selected_directory != null) {
                Path dir = selected_directory.toPath();
                app_state.changeConfiguration(dir.toString(), "Journal");
            } else {
                Helpers.alertErrorExit("No journal directory was selected!");
            }

            stage.close();
        }

        app_state.loadTree();
        TreeItem<FileTree> root = new TreeItem<>(app_state.file_tree);
        note_tree.setRoot(root);

        try {
            fillTreeView(root);
        } catch (IOException e) {
            Helpers.alertErrorExit("Couldn't load a note image!");
        }
    }

    /**
     * Sets the disabled property of buttons managing {@link TreeView}.
     *
     * @param   new_value   value the disabled property will be set to
     */
    void setTreeViewButtonsDisabled(boolean new_value) {
        add_group_button1.setDisable(new_value);
        add_note_button1.setDisable(new_value);
        remove_button1.setDisable(new_value);
        rename_button1.setDisable(new_value);
    }

    /**
     * Add new {@link TreeItem} into {@link #note_tree} together with associated {@link FileTree}, save to filesystem.
     *
     * @param   type    type of newly added item
     */
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
                new_item = new TreeItem<>(new_file_tree, getGraphic(NOTE_FILE));
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
            new_file_tree.save(app_state);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a unique name for the new {@link TreeItem} based on already existing siblings.
     *
     * @param   type        type of item
     * @param   file_tree   a {@link FileTree} to which we are trying to append a child
     * @return  a name no other item on that level has
     */
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

    /**
     * Set keyboard shortcuts used in this application.
     */
    public void setupKeyboardShortcuts() {
        // Setup ctrl+S combination to fire save button
        editor.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN), () -> {
            if (!editor.save_button.isDisabled()) {
                editor.save_button.fire();
            }
        });
    }

    /**
     * A custom implementation of {@link TreeCell} class that enables changes at the level of {@link AppState#file_tree}
     * as well as on the filesystem level reflecting name change initiated by user.
     */
    private final class FileTreeCellImpl extends TreeCell<FileTree> {
        // This field is used only when editing
        private TextField text_field;

        /**
         * Set up the cell for editing.
         */
        @Override
        public void startEdit() {
            super.startEdit();

            if (text_field == null) {
                createTextField();
            }
            setText(null);
            setGraphic(text_field);
            text_field.selectAll();
        }

        /**
         * Return to previous state if user cancels editing in some way.
         */
        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem().toString());
            setGraphic(getTreeItem().getGraphic());
        }

        /**
         * Redraws the component properly when needed.
         */
        @Override
        public void updateItem(FileTree item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (text_field != null) {
                        text_field.setText(getString());
                    }
                    setText(null);
                    setGraphic(text_field);
                } else {
                    setText(getString());
                    setGraphic(getTreeItem().getGraphic());
                }
            }
        }

        /**
         * Set up {@link TextField} used for editing purposes.
         */
        private void createTextField() {
            text_field = new TextField(getString());
            text_field.setOnKeyReleased(t -> {
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

        /**
         * Return String representation of {@link TreeItem}
         *
         * @return  String representation
         */
        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }

        /**
         * Based on the user input, change note / group name (if it doesn't already exist).
         *
         * @param   tree_item   currently edited {@link TreeItem}
         * @param   file_tree   {@link FileTree} corresponding to currently edited {@link TreeItem}
         * @return  success of change
         */
        private boolean changeItemName(TreeItem<FileTree> tree_item, FileTree file_tree) {
            String new_name = text_field.getText();
            if (file_tree.type == ElementType.FILE) {
                new_name += ".html";
            }

            // Root element translates to root directory, which we do not wish to rename
            if (tree_item == getTreeView().getRoot()) {
                app_state.changeConfiguration(app_state.getJournalDirectory(), new_name);
                file_tree.name = new_name;
            } else {

                if (alreadyExists(file_tree, tree_item.getParent(), new_name)) {
                    return false;
                }

                try {
                    file_tree.rename(new_name, app_state);
                } catch (IOException e) {
                    Helpers.alertErrorExit("An error occurred during changing element name!");
                }
            }
            return true;
        }

        /**
         * Check whether an element with same name already exists.
         *
         * @param   file_tree   {@link FileTree} corresponding to currently edited {@link TreeItem}
         * @param   parent      parent of currently edited {@link TreeItem}
         * @param   new_name    new name
         * @return  identically named element already exists
         */
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
