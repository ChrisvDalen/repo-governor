package io.repogovernor.cli.commands;

import io.repogovernor.core.rules.Rules;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "rules", description = "Rule management.", subcommands = RulesCommand.ListCommand.class)
public final class RulesCommand {

    @Command(name = "list", description = "Lists all available rules.")
    public static final class ListCommand implements Callable<Integer> {

        @Override
        public Integer call() {
            System.out.printf("%-42s %-13s %-8s %s%n", "RULE ID", "CATEGORY", "SEVERITY", "TITLE");
            Rules.defaultRules().forEach(rule ->
                    System.out.printf("%-42s %-13s %-8s %s%n",
                            rule.id(), rule.category(), rule.severity(), rule.title()));
            return 0;
        }
    }
}
