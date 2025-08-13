package org.aussiebox.dfwaypoints.features;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.dfonline.flint.Flint;
import dev.dfonline.flint.feature.trait.CommandFeature;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.aussiebox.dfwaypoints.Dfwaypoints;
import org.aussiebox.dfwaypoints.waypoints.Waypoint;
import org.aussiebox.dfwaypoints.waypoints.Waypoints;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class WaypointCommand implements CommandFeature {
    @Override
    public String commandName() {
        return "waypoint";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("wp");
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> createCommand(LiteralArgumentBuilder<FabricClientCommandSource> builder, CommandRegistryAccess commandRegistryAccess) {
        return builder.then(
                literal("create").then(
                        argument("text", StringArgumentType.greedyString()).executes(context -> createWaypoint(context, StringArgumentType.getString(context, "text")))
                )
        ).then(
                literal("list").executes(this::listWaypoints)
        );
    }

    private int createWaypoint(CommandContext<FabricClientCommandSource> context, String text) {
        assert Flint.getUser().getPlot() != null;
        String plotID = String.valueOf(Flint.getUser().getPlot().getId());
        String rawPlotName = (Flint.getUser().getPlot().getName())+"replace";
        String plotName = rawPlotName.replace("literal{", "").replace("}replace", "");

        MutableText hoverText = Text.empty()
                .append(
                        Text.literal("» ")
                                .withColor(0x55FF55)
                                .styled(style -> style.withBold(true))
                )
                .append(
                        Text.literal("Waypoint Name: ")
                                .withColor(0xFFFFFF)
                                .styled(style -> style.withBold(false))
                )
                .append(
                        Text.literal(text)
                                .withColor(0xFFFF55)
                )
                .append(
                        Text.literal("\n» ")
                                .withColor(0x55FF55)
                                .styled(style -> style.withBold(true))
                )
                .append(
                        Text.literal("Plot: ")
                                .withColor(0xFFFFFF)
                                .styled(style -> style.withBold(false))
                )
                .append(
                        Text.literal(plotName)
                                .withColor(0x55FFFF)
                )
                .append(
                        Text.literal(" (" + plotID + ")")
                                .withColor(0xAAAAAA)
                );

        MutableText waypointCreatedMessage = Text.literal("» ")
                .withColor(0x55FF55)
                .styled(style -> style.withBold(true))
                .append(
                        Text.literal("Waypoint Created. ")
                                .withColor(0xFFFFFF)
                                .styled(
                                        style -> style.withBold(false))
                ).append(
                        Text.literal("(Hover for details)")
                                .withColor(0xAAAAAA)
                                .styled(
                                        style -> style.withBold(false))
                )
                .styled(style -> style.withHoverEvent(
                                new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        hoverText
                                )
                        )
                );

        MutableText waypointCreatedNotSavedMessage = Text.literal("» ")
                .withColor(0xFF5555)
                .styled(style -> style.withBold(true))
                .append(
                        Text.literal("DFWaypoints ran into an issue saving waypoint data. Your waypoint has been created, but may not save upon closing the game. ")
                                .withColor(0xFFFFFF)
                                .styled(
                                        style -> style.withBold(false))
                ).append(
                        Text.literal("(Hover for details)")
                                .withColor(0xAAAAAA)
                                .styled(
                                        style -> style.withBold(false))
                )
                .styled(style -> style.withHoverEvent(
                                new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        hoverText
                                )
                        )
                );

        MutableText waypointEnterPlotMessage = Text.literal("» ")
                .withColor(0xFF5555)
                .styled(style -> style.withBold(true))
                .append(
                        Text.literal("Join a plot to create waypoints!")
                                .withColor(0xFFFFFF)
                                .styled(
                                        style -> style.withBold(false))
                );

        if (Flint.getUser().getPlot() == null) {
            assert Dfwaypoints.MC.player != null;
            Dfwaypoints.MC.player.sendMessage(waypointEnterPlotMessage, false);
            return 0;
        }

        if (Dfwaypoints.MC.player != null) {
            Waypoints.addWaypoint(Flint.getUser().getPlot().getId(), new Waypoint(text, Dfwaypoints.MC.player.getPos()));
            try {
                Waypoints.save();
                Dfwaypoints.MC.player.sendMessage(waypointCreatedMessage, false);
            } catch (IOException e) {
                Dfwaypoints.MC.player.sendMessage(waypointCreatedNotSavedMessage, false);
            }
        }

        return 0;
    }

    private int listWaypoints(CommandContext<FabricClientCommandSource> context) {
        assert Flint.getUser().getPlot() != null;
        assert Dfwaypoints.MC.player != null;

        List<Waypoint> waypoints = Waypoints.getWaypoints(Flint.getUser().getPlot().getId());
        List<String> names = waypoints
                .stream()
                .map(Waypoint::getName)
                .sorted()
                .toList();

        for (String name : names) {

            MutableText listWaypoint = Text.literal("» ")
                    .withColor(0x55FFFF)
                    .styled(style -> style.withBold(true))
                    .append(
                            Text.literal(name)
                                    .withColor(0xFFFFFF)
                                    .styled(
                                            style -> style.withBold(false))
                    );

            Dfwaypoints.MC.player.sendMessage(listWaypoint, false);
        }
        return 0;
    }
}
