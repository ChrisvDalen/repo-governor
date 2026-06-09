package io.repogovernor.core.scanner;

import java.util.Optional;

/**
 * Branch and commit information read from the repository's .git directory,
 * without requiring a git executable.
 */
public record GitInfo(Optional<String> branch, Optional<String> commitHash) {

    public static GitInfo none() {
        return new GitInfo(Optional.empty(), Optional.empty());
    }

    public static GitInfo from(RepoFiles repo) {
        Optional<String> head = repo.read(".git/HEAD").map(String::trim);
        if (head.isEmpty()) {
            return none();
        }
        String headContent = head.get();
        if (headContent.startsWith("ref: ")) {
            String ref = headContent.substring("ref: ".length()).trim();
            String branch = ref.startsWith("refs/heads/") ? ref.substring("refs/heads/".length()) : ref;
            Optional<String> commit = repo.read(".git/" + ref).map(String::trim)
                    .or(() -> commitFromPackedRefs(repo, ref));
            return new GitInfo(Optional.of(branch), commit);
        }
        // Detached HEAD: the file contains the commit hash itself.
        return new GitInfo(Optional.empty(), Optional.of(headContent));
    }

    private static Optional<String> commitFromPackedRefs(RepoFiles repo, String ref) {
        return repo.read(".git/packed-refs").stream()
                .flatMap(String::lines)
                .filter(line -> line.endsWith(" " + ref))
                .map(line -> line.split(" ")[0])
                .findFirst();
    }
}
