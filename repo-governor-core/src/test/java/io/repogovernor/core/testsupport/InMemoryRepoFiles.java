package io.repogovernor.core.testsupport;

import io.repogovernor.core.scanner.RepoFiles;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory {@link RepoFiles} for rule tests. Directories are derived from
 * file paths; an explicit empty directory can be added with {@link #withDir}.
 */
public final class InMemoryRepoFiles implements RepoFiles {

    private final Map<String, String> files = new LinkedHashMap<>();
    private final java.util.Set<String> dirs = new java.util.LinkedHashSet<>();
    private final String name;

    public InMemoryRepoFiles(String name) {
        this.name = name;
    }

    public static InMemoryRepoFiles repo() {
        return new InMemoryRepoFiles("test-repo");
    }

    public InMemoryRepoFiles withFile(String path, String content) {
        files.put(path, content);
        return this;
    }

    public InMemoryRepoFiles withFile(String path) {
        return withFile(path, "");
    }

    public InMemoryRepoFiles withDir(String path) {
        dirs.add(path);
        return this;
    }

    @Override
    public boolean exists(String relativePath) {
        return files.containsKey(relativePath) || isDirectory(relativePath);
    }

    @Override
    public boolean isDirectory(String relativePath) {
        String prefix = relativePath.endsWith("/") ? relativePath : relativePath + "/";
        return dirs.contains(relativePath) || files.keySet().stream().anyMatch(f -> f.startsWith(prefix));
    }

    @Override
    public Optional<String> read(String relativePath) {
        return Optional.ofNullable(files.get(relativePath));
    }

    @Override
    public List<String> allFiles() {
        return files.keySet().stream().filter(f -> !f.startsWith(".git/")).sorted().toList();
    }

    @Override
    public String repositoryName() {
        return name;
    }
}
