package ruman.semestral_work;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileTree {
    List<FileTree> descendants = new ArrayList<>();
    String name;
    ElementType type;

    public FileTree(String name, ElementType type) {
        this.name = name;
        this.type = type;
    }

    public void addRecursively(Path path, ElementType type) {
        // Check whether not at the end of recursion
        if (path.compareTo(Paths.get("")) != 0) {

            Path root = path.getName(0);
            String name = root.getFileName().toString();
            Path newPath = root.relativize(path);
            boolean found = false;

            for (FileTree tree : descendants) {
                if (tree.name.equals(name)) {
                    tree.addRecursively(newPath, type);
                    found = true;
                }
            }

            if (!found) {
                ElementType current_type = type;
                if (newPath.compareTo(Paths.get("")) != 0) {
                    current_type = ElementType.DIRECTORY;
                }
                FileTree newTree = new FileTree(root.getFileName().toString(), current_type);
                descendants.add(newTree);
                newTree.addRecursively(newPath, type);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("");
        result.append("{name: ");
        result.append(name);
        result.append(", type: ");
        result.append(type);
        result.append(", subtrees: [");
        for (FileTree tree : descendants) {
            result.append(tree.toString());
            if (!(tree == descendants.get(descendants.size()-1))) {
                result.append(", ");
            }
        }
        result.append("]}");
        return result.toString();
    }
}
