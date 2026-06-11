package io.repogovernor.core.scanner;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * {@link RepoFiles} backed by a directory on disk.
 */
public final class DiskRepoFiles implements RepoFiles {

    private static final Set<String> EXCLUDED_DIRS = Set.of(
            ".git", "node_modules", "build", "target", "dist", ".gradle", ".angular", ".idea", "out");

    private final Path root;
    private List<String> cachedFiles;

    public DiskRepoFiles(Path root) {
        this.root = root.toAbsolutePath().normalize();
        if (!Files.isDirectory(this.root)) {
            throw new IllegalArgumentException("Not a directory: " + this.root);
        }
    }

    @Override
    public boolean exists(String relativePath) {
        return Files.exists(resolve(relativePath));
    }

    @Override
    public boolean isDirectory(String relativePath) {
        return Files.isDirectory(resolve(relativePath));
    }

    @Override
    public Optional<String> read(String relativePath) {
        Path path = resolve(relativePath);
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<String> allFiles() {
        if (cachedFiles == null) {
            try (Stream<Path> stream = Files.walk(root)) {
                cachedFiles = stream
                        .filter(Files::isRegularFile)
                        .map(root::relativize)
                        .map(p -> p.toString().replace('\\', '/'))
                        .filter(p -> !isExcluded(p))
                        .sorted()
                        .toList();
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to walk " + root, e);
            }
        }
        return cachedFiles;
    }

    @Override
    public String repositoryName() {
        return root.getFileName().toString();
    }

    private Path resolve(String relativePath) {
        Path resolved = root.resolve(relativePath).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Path escapes repository root: " + relativePath);
        }
        return resolved;
    }

    private static boolean isExcluded(String relativePath) {
        for (String segment : relativePath.split("/")) {
            if (EXCLUDED_DIRS.contains(segment)) {
                return true;
            }
        }
        return false;
    }
}
