package org.aussiebox.dfwaypoints.features;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.flint.Flint;
import dev.dfonline.flint.feature.trait.CommandFeature;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import org.aussiebox.dfwaypoints.waypoints.Waypoint;
import org.aussiebox.dfwaypoints.waypoints.Waypoints;

import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class WaypointTeleportCommand implements CommandFeature {

    @Override
    public String commandName() {
        return "wtp";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> createCommand(LiteralArgumentBuilder<FabricClientCommandSource> builder, CommandRegistryAccess commandRegistryAccess) {
        return builder.then(
                argument("text", StringArgumentType.greedyString())
                        .suggests((context, suggestionsBuilder) -> {
                            if (Flint.getUser().getPlot() == null) {
                                return suggestionsBuilder.buildFuture();
                            }
                            List<Waypoint> waypoints = Waypoints.getWaypoints(Flint.getUser().getPlot().getId());
                            List<String> names = waypoints
                                    .stream()
                                    .map(Waypoint::getName)
                                    .filter(name -> name.startsWith(suggestionsBuilder.getRemaining()))
                                    .sorted()
                                    .toList();

                            for (String name : names) {
                                suggestionsBuilder.suggest(name);
                            }
                            return suggestionsBuilder.buildFuture();
                        })
                        .executes(context -> {
                            try {
                                return WaypointCommands.teleportToWaypoint(context, StringArgumentType.getString(context, "text"));
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        })
        );
    }

}