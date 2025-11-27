package wbs.enchants.command;

import org.jetbrains.annotations.NotNull;
import wbs.enchants.definition.DescribeOption;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.List;

public class SubcommandFullInfo extends SubcommandInfo {
    public SubcommandFullInfo(@NotNull WbsPlugin plugin) {
        super(plugin, "fullinfo");

        describeOptions = List.of(
                DescribeOption.TYPE,
                DescribeOption.MAX_LEVEL,
                DescribeOption.TARGET,
                DescribeOption.ACTIVE_SLOTS,
                DescribeOption.DESCRIPTION,
                DescribeOption.ANVIL_COST,
                DescribeOption.WEIGHT,
                DescribeOption.COSTS,
                DescribeOption.TAGS,
                DescribeOption.GENERATION,
                DescribeOption.CONFLICTS
        );
    }
}
