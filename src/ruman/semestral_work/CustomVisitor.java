package ruman.semestral_work;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

public class CustomVisitor extends SimpleFileVisitor<Path> {
    FileTree file_tree;
    Path folder_path;

    public CustomVisitor(FileTree file_tree, Path folder_path) {
        this.file_tree = file_tree;
        this.folder_path = folder_path;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        Path path = folder_path.relativize(file);
        file_tree.addRecursively(path, Paths.get(""), ElementType.FILE);
        return CONTINUE;
    }

    // Print each directory visited.
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        Path path = folder_path.relativize(dir);
        file_tree.addRecursively(path, Paths.get(""), ElementType.DIRECTORY);
        return CONTINUE;
    }
}
