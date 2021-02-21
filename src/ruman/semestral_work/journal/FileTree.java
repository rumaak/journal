package ruman.semestral_work.journal;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * Class to represent journal structure.
 */
public class FileTree {
    static int created = 0;

    List<FileTree> descendants = new ArrayList<>();
    String name;
    ElementType type;
    Path path;

    /**
     * Initialize (sub)tree with its name, path relative to journal root directory and type (file / directory)
     *
     * @param name  name associated with tree / root node (name of file / directory)
     * @param path  path to corresponding file / directory relative to journal root directory
     * @param type  whether the root node represents a note (file) or group (directory)
     */
    public FileTree(String name, Path path, ElementType type) {
        this.name = name;
        this.type = type;
        this.path = path;
        created += 1;
    }

    /**
     * Based on the structure of path add a new node somewhere in the tree (create whole branch or subbranch if needed).
     *
     * @param path      path relative to this particular tree, describes where the new subtree belongs
     * @param cut_off   path relative to the journal root directory, points to a parent of this tree
     * @param type      what is the type of the new subtree
     */
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

    /**
     * Save a file / directory to filesystem, path {@link #path}, corresponding to the root of this tree.
     *
     * @param app_state     instance of {@link AppState} that holds configuration
     */
    public void save(AppState app_state) throws IOException {
        Path target = app_state.resolveJournalPath(path);
        if (type == ElementType.DIRECTORY) {
            Files.createDirectory(target);
        } else {
            Files.createFile(target);
        }
    }

    /**
     * Delete file / directory corresponding to this whole tree (not only root node) on filesystem.
     *
     * @param app_state     instance of {@link AppState} that holds configuration
     */
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

    /**
     * Name of directory or file (without extension).
     *
     * @return  name corresponding to the root of this tree
     */
    @Override
    public String toString() {
        if (type == ElementType.FILE) {
            // Remove .html
            return name.substring(0, name.length()-5);
        }
        return name;
    }

    /**
     * A more detailed version of {@link #toString()} for debugging purposes.
     *
     * @return  String representation of whole tree
     */
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

    /**
     * Change the name associated with this tree (its root node) and write changes to filesystem.
     *
     * @param new_name      new name of tree
     * @param app_state     instance of {@link AppState} that holds configuration
     */
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

    /**
     * Change paths of all subtrees to reflect the change in name of this tree.
     *
     * @param new_parent_path   path to parent tree with changes applied
     */
    public void propagateNameChange(Path new_parent_path) {
        path = new_parent_path.resolve(name);
        for (FileTree d : descendants) {
            d.propagateNameChange(path);
        }
    }
}
