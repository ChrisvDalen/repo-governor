package io.repogovernor.cli;

import picocli.CommandLine;

public final class ToolVersion implements CommandLine.IVersionProvider {

    public static final String VERSION = "0.1.0";

    @Override
    public String[] getVersion() {
        return new String[]{"repo-governor " + VERSION};
    }
}
