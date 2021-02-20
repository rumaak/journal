package ruman.semestral_work;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static java.nio.file.Files.walkFileTree;

public class AppState {

    HashMap<String, String> configuration;
    public FileTree fileTree;

    public AppState() {
        this.configuration = null;
    }

    public boolean configurationExists() {
        return configuration != null;
    }

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

    public void loadTree() {
        String journal_name = configuration.get("journal_name");
        fileTree = new FileTree(journal_name, Paths.get(""), ElementType.DIRECTORY);
        Path root = Paths.get(configuration.get("folder"));
        CustomVisitor custom_visitor = new CustomVisitor(fileTree, root);

        try {
            walkFileTree(root, custom_visitor);
        } catch (IOException e) {
            Helpers.alertErrorExit("An error occurred during loading journal tree!");
        }

        // System.out.println(fileTree.toStringDebug());
    }

    public Path resolveJournalPath(Path path) {
        Path p = Paths.get(configuration.get("folder"));
        return p.resolve(path);
    }

    public Path relativizeJournalPath(Path path) {
        Path p = Paths.get(configuration.get("folder"));
        return p.relativize(path);
    }

    public String getJournalDirectory() {
        return configuration.get("folder");
    }

    public String getJournalName() {
        return configuration.get("journal_name");
    }
}
