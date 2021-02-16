package ruman.semestral_work;

import javax.imageio.IIOException;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static java.nio.file.Files.walkFileTree;

public class Loader {

    HashMap<String, String> configuration;

    public Loader() {
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
        FileTree fileTree = new FileTree("Journal", ElementType.DIRECTORY);
        Path root = Paths.get(configuration.get("folder"));
        CustomVisitor customVisitor = new CustomVisitor(fileTree, root);

        try {
            walkFileTree(root, customVisitor);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(fileTree);
    }
}
