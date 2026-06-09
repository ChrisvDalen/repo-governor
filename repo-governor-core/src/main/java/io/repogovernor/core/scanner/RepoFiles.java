package io.repogovernor.core.scanner;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction over the repository file system so rules can be unit tested
 * without touching disk.
 */
public interface RepoFiles {

    /** Repository-relative existence check, e.g. {@code exists("README.md")}. */
    boolean exists(String relativePath);

    /** True if the relative path exists and is a directory. */
    boolean isDirectory(String relativePath);

    /** Reads a text file, empty when missing or unreadable. */
    Optional<String> read(String relativePath);

    /**
     * All file paths in the repository, relative with {@code /} separators.
     * Common build output and dependency directories are excluded.
     */
    List<String> allFiles();

    /** Display name of the repository root (usually the directory name). */
    String repositoryName();

    default boolean existsAny(String... relativePaths) {
        for (String path : relativePaths) {
            if (exists(path)) {
                return true;
            }
        }
        return false;
    }
}
