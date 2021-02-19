package ruman.semestral_work;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;

public class FileTree {
    // TODO consider static AppState
    static int created = 0;

    List<FileTree> descendants = new ArrayList<>();
    String name;
    ElementType type;
    Path path;

    public FileTree(String name, Path path, ElementType type) {
        this.name = name;
        this.type = type;
        this.path = path;
        created += 1;
    }

    public void addRecursively(Path path, Path cutOff, ElementType type) {
        // Check whether not at the end of recursion
        if (path.compareTo(Paths.get("")) != 0) {

            Path root = path.getName(0);
            Path newCutOff = cutOff.resolve(root);
            String name = root.getFileName().toString();
            Path newPath = root.relativize(path);
            boolean found = false;

            for (FileTree tree : descendants) {
                if (tree.name.equals(name)) {
                    tree.addRecursively(newPath, newCutOff, type);
                    found = true;
                    break;
                }
            }

            if (!found) {
                ElementType current_type = type;
                if (newPath.compareTo(Paths.get("")) != 0) {
                    current_type = ElementType.DIRECTORY;
                }
                FileTree newTree = new FileTree(name, newCutOff, current_type);
                descendants.add(newTree);
                newTree.addRecursively(newPath, newCutOff, type);
            }
        }
    }

    public void save(AppState appState) throws IOException {
        Path target = appState.resolveJournalPath(path);
        if (type == ElementType.DIRECTORY) {
            Files.createDirectory(target);
        } else {
            Files.createFile(target);
        }
    }

    public void delete(AppState appState) throws IOException {
        Path target = appState.resolveJournalPath(path);
        Files.walkFileTree(target, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc == null) {
                    Files.delete(dir);
                    return CONTINUE;
                } else {
                    throw exc;
                }
            }
        });
    }

    @Override
    public String toString() {
        if (type == ElementType.FILE) {
            // Remove .html
            return name.substring(0, name.length()-5);
        }
        return name;
    }

    public String toStringDebug() {
        StringBuilder result = new StringBuilder("");
        result.append("{name: ");
        result.append(name);
        result.append(", type: ");
        result.append(type);
        result.append(", subtrees: [");
        for (FileTree tree : descendants) {
            result.append(tree.toStringDebug());
            if (!(tree == descendants.get(descendants.size()-1))) {
                result.append(", ");
            }
        }
        result.append("]}");
        return result.toString();
    }

    public void rename(String new_name, AppState appState) throws IOException {
        Path source = appState.resolveJournalPath(path);
        Path target = source.resolveSibling(new_name);

        Files.move(source, target);

        name = new_name;
        path = appState.relativizeJournalPath(target);

        for (FileTree d : descendants) {
            d.propagateNameChange(path);
        }
    }

    public void propagateNameChange(Path new_parent_path) {
        path = new_parent_path.resolve(name);
        for (FileTree d : descendants) {
            d.propagateNameChange(path);
        }
    }
}
