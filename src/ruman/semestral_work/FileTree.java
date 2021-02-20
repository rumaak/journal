package ruman.semestral_work;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;

public class FileTree {
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

    public void addRecursively(Path path, Path cut_off, ElementType type) {
        // Check whether not at the end of recursion
        if (path.compareTo(Paths.get("")) != 0) {

            Path root = path.getName(0);
            Path new_cut_off = cut_off.resolve(root);
            String name = root.getFileName().toString();
            Path new_path = root.relativize(path);
            boolean found = false;

            for (FileTree tree : descendants) {
                if (tree.name.equals(name)) {
                    tree.addRecursively(new_path, new_cut_off, type);
                    found = true;
                    break;
                }
            }

            if (!found) {
                ElementType current_type = type;
                if (new_path.compareTo(Paths.get("")) != 0) {
                    current_type = ElementType.DIRECTORY;
                }
                FileTree new_tree = new FileTree(name, new_cut_off, current_type);
                descendants.add(new_tree);
                new_tree.addRecursively(new_path, new_cut_off, type);
            }
        }
    }

    public void save(AppState app_state) throws IOException {
        Path target = app_state.resolveJournalPath(path);
        if (type == ElementType.DIRECTORY) {
            Files.createDirectory(target);
        } else {
            Files.createFile(target);
        }
    }

    public void delete(AppState app_state) throws IOException {
        Path target = app_state.resolveJournalPath(path);
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

    public void rename(String new_name, AppState app_state) throws IOException {
        Path source = app_state.resolveJournalPath(path);
        Path target = source.resolveSibling(new_name);

        Files.move(source, target);

        name = new_name;
        path = app_state.relativizeJournalPath(target);

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
