package ruman.semestral_work;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static java.nio.file.Files.walkFileTree;

/**
 * Class representing current state of application.
 */
public class AppState {

    HashMap<String, String> configuration;
    public FileTree file_tree;

    /**
     * Indicate that configuration wasn't loaded yet.
     */
    public AppState() {
        this.configuration = null;
    }

    /**
     * Check the current state of configuration.
     *
     * @return  does the configuration exist
     */
    public boolean configurationExists() {
        return configuration != null;
    }

    /**
     * Attempts to load already existing configuration.
     */
    public void loadConfiguration() {
        String config_name = ".config";
        Path config_path = Paths.get(config_name);
        if (Files.exists(config_path)) {
            try (FileInputStream file = new FileInputStream(config_name)) {
                ObjectInputStream config = new ObjectInputStream(file);
                configuration = (HashMap<String, String>) config.readObject();
            } catch (IOException | ClassNotFoundException e) {
                Helpers.alertWarning("Couldn't load existing configuration file!");
            }
        }
    }

    /**
     * Attempt to save configuration to filesystem (application exits on fail).
     *
     * @param   new_folder      where is the journal stored
     * @param   journal_name    name of the journal
     */
    public void changeConfiguration(String new_folder, String journal_name) {
        configuration = new HashMap<>();
        configuration.put("folder", new_folder);
        configuration.put("journal_name", journal_name);
        try (FileOutputStream file = new FileOutputStream(".config")) {
            ObjectOutputStream config = new ObjectOutputStream(file);
            config.writeObject(configuration);
        } catch (IOException e) {
            // if attempt to write fails, we cannot continue
            Helpers.alertErrorExit("Couldn't save configuration to configuration file!");
        }
    }

    /**
     * Attempts to reflect journal structure in filesystem into {@link #file_tree} (application exits on fail).
     */
    public void loadTree() {
        String journal_name = configuration.get("journal_name");
        file_tree = new FileTree(journal_name, Paths.get(""), ElementType.DIRECTORY);
        Path root = Paths.get(configuration.get("folder"));
        LoadTreeVisitor custom_visitor = new LoadTreeVisitor(file_tree, root);

        try {
            walkFileTree(root, custom_visitor);
        } catch (IOException e) {
            Helpers.alertErrorExit("An error occurred during loading journal tree!");
        }

        // System.out.println(fileTree.toStringDebug());
    }

    /**
     * Translates path relative to root journal directory into an absolute path.
     *
     * @param   path    path relative to root journal directory
     * @return absolute path pointing to same file
     */
    public Path resolveJournalPath(Path path) {
        Path p = Paths.get(configuration.get("folder"));
        return p.resolve(path);
    }

    /**
     * Translates an absolute path into one relative to root journal directory.
     *
     * @param   path    absolute path
     * @return  path relative to root journal directory pointing to same file
     */
    public Path relativizeJournalPath(Path path) {
        Path p = Paths.get(configuration.get("folder"));
        return p.relativize(path);
    }

    /**
     * Get journal location.
     *
     * @return String representation of journal directory location on filesystem
     */
    public String getJournalDirectory() {
        return configuration.get("folder");
    }

    /**
     * Get journal name.
     *
     * @return journal name
     */
    public String getJournalName() {
        return configuration.get("journal_name");
    }
}
