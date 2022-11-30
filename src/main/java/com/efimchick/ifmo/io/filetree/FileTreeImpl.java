package com.efimchick.ifmo.io.filetree;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileTreeImpl implements FileTree {
    private static final Comparator<File> fileComparator = (o1, o2) -> {
        if (o1.isFile() && o2.isFile() || o1.isDirectory() && o2.isDirectory()) {
            return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
        } else {
            return o1.isDirectory()
                    ? -1
                    : 1;
        }
    };

    private final Function<Path, String> getPathInfo = path1 ->
            path1.getFileName() + " " + sizeOfFile(path1) + " bytes\n";
    private static final Function<Boolean, String> addItemCharacter = hasNext1 -> Boolean.TRUE.equals(hasNext1) ? "├─ " : "└─ ";
    private static final Function<Boolean, String> addSeparateCharacter = hasNext1 -> Boolean.TRUE.equals(hasNext1) ? "│  " : "   ";


    @Override
    public Optional<String> tree(Path path) {
        if (path == null || !path.toFile().exists()){
            return Optional.empty();
        }
        StringBuilder result = buildTree(new StringBuilder(), path, false, true);
        return Optional.of(result.toString());
    }

    private Long sizeOfFile(Path path){
        if (path.toFile().isFile()) {
            return path.toFile().length();
        } else {
            return Arrays
                    .stream(path.toFile().listFiles())
                    .mapToLong(p -> sizeOfFile(p.toPath()))
                    .sum();
        }
    }

    private StringBuilder buildTree(StringBuilder prefix, Path path, boolean hasNext, boolean isRoot){
        StringBuilder result = new StringBuilder()
                .append(prefix)
                .append(isRoot ? "" : addItemCharacter.apply(hasNext))
                .append(getPathInfo.apply(path));

        if (path.toFile().isFile()){
            return result;
        }

        List<File> sortedContents = Arrays.stream(path.toFile().listFiles())
                .sorted(fileComparator)
                .collect(Collectors.toList());

        Iterator<File> fileIterator = sortedContents.iterator();
        while (fileIterator.hasNext()){
            File file = fileIterator.next();
            StringBuilder newPrefix = new StringBuilder()
                    .append(prefix)
                    .append(isRoot ? "" : addSeparateCharacter.apply(hasNext));
            result.append(buildTree(newPrefix,
                    file.toPath(),
                    fileIterator.hasNext(),
                    false));
        }

        return result;
    }
}
