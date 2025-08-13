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
import java.util.Set;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class WaypointCreationCommand implements CommandFeature {
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
        );
    }

    private int createWaypoint(CommandContext<FabricClientCommandSource> context, String text) {
        if (Flint.getUser().getPlot() == null) {
            return 0;
        }
        String plotID = String.valueOf(Flint.getUser().getPlot().getId());
        String plotName = String.valueOf((Flint.getUser().getPlot().getName()));

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

        MutableText waypointCreationMessage = Text.literal("» ")
                .withColor(0x55FFFF)
                .styled(style -> style.withBold(true))
                .append(
                        Text.literal("Creating new waypoint...")
                                .withColor(0xFFFFFF)
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

        MutableText waypointCreatedMessage = Text.literal("» ")
                .withColor(0x55FF55)
                .styled(style -> style.withBold(true))
                .append(
                        Text.literal("Waypoint Created.")
                                .withColor(0xFFFFFF)
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

        MutableText errorHoverText = Text.empty()
                .append(
                        Text.literal("» ")
                                .withColor(0xFF5555)
                                .styled(style -> style.withBold(true))
                )
                .append(
                        Text.literal("Could not save waypoints.json.")
                                .withColor(0xFFFFFF)
                                .styled(style -> style.withBold(false))
                );

        MutableText waypointErrorMessage = Text.literal("» ")
                .withColor(0xFF5555)
                .styled(style -> style.withBold(true))
                .append(
                        Text.literal("Waypoint creation failed!")
                                .withColor(0xFFFFFF)
                                .styled(
                                        style -> style.withBold(false))
                )
                .styled(style -> style.withHoverEvent(
                                new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        errorHoverText
                                )
                        )
                );

        if (Dfwaypoints.MC.player != null) {
            Dfwaypoints.MC.player.sendMessage(waypointCreationMessage, false);
            Waypoints.addWaypoint(Flint.getUser().getPlot().getId(), new Waypoint(text, Dfwaypoints.MC.player.getPos()));
            try {
                Waypoints.save();
            } catch (IOException e) {
                Dfwaypoints.MC.player.sendMessage(waypointErrorMessage, false);
            }
            Dfwaypoints.MC.player.sendMessage(waypointCreatedMessage, false);
        }

        return 0;
    }
}
