package io.repogovernor.cli.commands;

import io.repogovernor.cli.upload.ReportUploader;
import io.repogovernor.core.config.ConfigLoader;
import io.repogovernor.core.scanner.DiskRepoFiles;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "upload", description = "Uploads an existing scan report to the server.")
public final class UploadCommand implements Callable<Integer> {

    @Parameters(index = "0", defaultValue = ".",
            description = "Repository path containing " + ScanCommand.JSON_REPORT + ".")
    private Path repositoryPath;

    @Option(names = "--server", description = "Server base URL (overrides repo-governor.yml).")
    private String serverUrl;

    @Option(names = "--api-key", required = true, description = "API key for the server.")
    private String apiKey;

    @Option(names = "--file", description = "Report file to upload (default: " + ScanCommand.JSON_REPORT + ").")
    private Path file;

    @Override
    public Integer call() throws Exception {
        Path jsonFile = file != null ? file : repositoryPath.resolve(ScanCommand.JSON_REPORT);
        if (!Files.exists(jsonFile)) {
            System.err.println("Report file not found: " + jsonFile.toAbsolutePath()
                    + ". Run 'repo-governor scan' first.");
            return 1;
        }
        String server = serverUrl;
        if (server == null && Files.isDirectory(repositoryPath)) {
            server = new ConfigLoader().load(new DiskRepoFiles(repositoryPath)).server().url();
        }
        if (server == null) {
            System.err.println("No server URL given (--server) or configured in repo-governor.yml.");
            return 1;
        }

        var result = new ReportUploader().upload(server, apiKey, Files.readString(jsonFile));
        if (result.isSuccess()) {
            System.out.println("Report uploaded to " + server);
            return 0;
        }
        System.err.println("Upload failed with HTTP " + result.statusCode() + ": " + result.body());
        return 1;
    }
}
