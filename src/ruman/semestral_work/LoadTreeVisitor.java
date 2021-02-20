package ruman.semestral_work;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * Implementation of {@link SimpleFileVisitor} used to load journal files and directories into a {@link FileTree}.
 */
public class LoadTreeVisitor extends SimpleFileVisitor<Path> {
    FileTree file_tree;
    Path folder_path;

    /**
     * Initialize {@link #file_tree} and {@link #folder_path}.
     *
     * @param file_tree     a {@link FileTree} we want to load journal files / directories into
     * @param folder_path   a {@link Path} pointing to the journal root directory
     */
    public LoadTreeVisitor(FileTree file_tree, Path folder_path) {
        this.file_tree = file_tree;
        this.folder_path = folder_path;
    }

    /**
     * Whenever file is encountered, add it to {@link #file_tree}.
     *
     * @param file  file visited
     * @param attr  file attributes
     * @return result of visit
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        Path path = folder_path.relativize(file);
        file_tree.addRecursively(path, Paths.get(""), ElementType.FILE);
        return CONTINUE;
    }

    /**
     * Whenever directory is left, add it to {@link #file_tree}.
     *
     * @param dir  directory left
     * @param exc  an exception encountered
     * @return result of visit
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        Path path = folder_path.relativize(dir);
        file_tree.addRecursively(path, Paths.get(""), ElementType.DIRECTORY);
        return CONTINUE;
    }
}
