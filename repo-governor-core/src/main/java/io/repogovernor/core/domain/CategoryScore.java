package io.repogovernor.core.domain;

public record CategoryScore(Category category, int score) {

    public CategoryScore {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("score must be between 0 and 100, was " + score);
        }
    }
}
