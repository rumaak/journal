package ruman.semestral_work;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

public class CustomVisitor extends SimpleFileVisitor<Path> {
    FileTree fileTree;
    Path folderPath;

    public CustomVisitor(FileTree fileTree, Path folderPath) {
        this.fileTree = fileTree;
        this.folderPath = folderPath;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        Path path = folderPath.relativize(file);
        fileTree.addRecursively(path, ElementType.FILE);
        return CONTINUE;
    }

    // Print each directory visited.
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        Path path = folderPath.relativize(dir);
        fileTree.addRecursively(path, ElementType.DIRECTORY);
        return CONTINUE;
    }
}
