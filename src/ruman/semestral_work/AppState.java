package ruman.semestral_work;

import java.io.*;
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
        try (FileInputStream file = new FileInputStream(".config")) {
            ObjectInputStream config = new ObjectInputStream(file);
            configuration = (HashMap<String, String>) config.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void changeConfiguration(String new_folder) {
        configuration = new HashMap<>();
        configuration.put("folder", new_folder);
        try (FileOutputStream file = new FileOutputStream(".config")) {
            ObjectOutputStream config = new ObjectOutputStream(file);
            config.writeObject(configuration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadTree() {
        fileTree = new FileTree("Journal", Paths.get(""), ElementType.DIRECTORY);
        Path root = Paths.get(configuration.get("folder"));
        CustomVisitor customVisitor = new CustomVisitor(fileTree, root);

        try {
            walkFileTree(root, customVisitor);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // System.out.println(fileTree.toStringDebug());
    }

    public Path resolveJournalPath(Path path) {
        Path p = Paths.get(configuration.get("folder"));
        return p.resolve(path);
    }
}
