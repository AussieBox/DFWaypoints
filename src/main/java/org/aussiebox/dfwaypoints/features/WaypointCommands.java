package org.aussiebox.dfwaypoints.features;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.dfonline.flint.Flint;
import dev.dfonline.flint.feature.trait.CommandFeature;
import dev.dfonline.flint.feature.trait.PacketListeningFeature;
import dev.dfonline.flint.util.result.EventResult;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.aussiebox.dfwaypoints.Dfwaypoints;
import org.aussiebox.dfwaypoints.helpers.MessageSystem;
import org.aussiebox.dfwaypoints.util.CommandSender;
import org.aussiebox.dfwaypoints.waypoints.Waypoint;
import org.aussiebox.dfwaypoints.waypoints.Waypoints;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class WaypointCommands implements CommandFeature, PacketListeningFeature {

    static boolean teleported = false;

    @Override
    public EventResult onReceivePacket(Packet<?> packet) {
        if (packet instanceof PlaySoundS2CPacket || packet instanceof PlaySoundFromEntityS2CPacket) {
            if (teleported) {
                teleported = false;
                return EventResult.CANCEL;
            }
        }
        return EventResult.PASS;
    }

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
        ).then(
                literal("teleport").then(
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
                                        return teleportToWaypoint(context, StringArgumentType.getString(context, "text"));
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                )
        ).then(
                literal("tp").then(
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
                                        return teleportToWaypoint(context, StringArgumentType.getString(context, "text"));
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                )
        ).then(
                literal("delete").then(
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
                                .executes(context -> deleteWaypoint(context, StringArgumentType.getString(context, "text")))
                )
        ).then(
                literal("remove").then(
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
                                .executes(context -> deleteWaypoint(context, StringArgumentType.getString(context, "text")))
                )
        );
    }

    public int createWaypoint(CommandContext<FabricClientCommandSource> context, String text) {
        assert Dfwaypoints.MC.player != null;

        if (Flint.getUser().getPlot() == null) {
            MessageSystem.ErrorMessage(
                    Text.literal("You must be on a plot to create waypoints!")
            );
            return 0;
        }

        if (!Flint.getUser().getMode().isEditor()) {
            MessageSystem.ErrorMessage(
                    Text.literal("You must be in dev/build mode to create waypoints!")
            );
            return 0;
        }

        String plotID = String.valueOf(Flint.getUser().getPlot().getId());
        String rawPlotName = (Flint.getUser().getPlot().getName()) + "replace";
        String plotName = rawPlotName.replace("literal{", "").replace("}replace", "");

        MutableText hoverText = Text.empty()
                .append(
                        Text.literal("Waypoint Name: ")
                                .withColor(0xFFFFFF)
                )
                .append(
                        Text.literal(text)
                                .withColor(0xFFFF55)
                )
                .append(
                        Text.literal("\nPlot: ")
                                .withColor(0xFFFFFF)
                )
                .append(
                        Text.literal(plotName)
                                .withColor(0x55FFFF)
                )
                .append(
                        Text.literal(" (" + plotID + ")")
                                .withColor(0xAAAAAA)
                );

        MutableText waypointCreatedMessage = Text.empty()
                .copy().append(
                        Text.literal("Waypoint Created. ")
                                .styled(style -> style.withHoverEvent(
                                            new HoverEvent.ShowText(hoverText)
                                        )
                                )
                                .styled(style -> style.withFont(Identifier.of("minecraft:default")))
                ).append(
                        Text.literal("(Hover for details)")
                                .withColor(0xAAAAAA)
                                .styled(style -> style.withHoverEvent(
                                            new HoverEvent.ShowText(hoverText)
                                        )
                                )
                );

        MutableText waypointCreatedNotSavedMessage = Text.empty()
                .copy().append(
                        Text.literal("DFWaypoints ran into an issue saving waypoint data. Your waypoint has been created, but may not save upon closing the game. ")
                                .withColor(0xFFFFFF)
                                .styled(style -> style.withHoverEvent(
                                            new HoverEvent.ShowText(hoverText)
                                        )
                                )
                                .styled(
                                        style -> style.withFont(Identifier.of("minecraft:default"))
                                )
                )
                .append(
                        Text.literal("(Hover for details)")
                                .withColor(0xAAAAAA)
                                .styled(style -> style.withHoverEvent(
                                            new HoverEvent.ShowText(hoverText)
                                        )
                                )
                );

        List<Waypoint> waypoints = Waypoints.getWaypoints(Flint.getUser().getPlot().getId());

        for (Waypoint waypoint : waypoints) {
            if (Objects.equals(waypoint.getName(), text)) {

                MessageSystem.ErrorMessage(
                        Text.literal("You already have a waypoint with this name!")
                );

                return 0;

            }
        }

        if (Dfwaypoints.MC.player != null) {
            Waypoints.addWaypoint(Flint.getUser().getPlot().getId(), new Waypoint(text, Dfwaypoints.MC.player.getPos()));
            try {
                Waypoints.save();
                MessageSystem.SuccessMessage(waypointCreatedMessage);
            } catch (IOException e) {
                MessageSystem.ErrorMessage(waypointCreatedNotSavedMessage);
            }
        }

        return 0;
    }

    public int listWaypoints(CommandContext<FabricClientCommandSource> context) {
        assert Dfwaypoints.MC.player != null;

        if (Flint.getUser().getPlot() == null) {
            MessageSystem.ErrorMessage(
                    Text.literal("You must be on a plot to list your waypoints!")
            );
            return 0;
        }

        MutableText teleportHover = Text.empty()
                .append(
                        Text.literal("Teleport to waypoint")
                                .withColor(0xFFFFFF)
                                .styled(
                                        style -> style.withFont(Identifier.of("minecraft:default"))
                                )
                                .styled(
                                        style -> style.withHoverEvent(null)
                                )
                );

        MutableText deleteHover = Text.empty()
                .append(
                        Text.literal("Delete Waypoint")
                                .withColor(0xFFFFFF)
                );

        MutableText waypointListMessage = Text.empty()
                .copy().append(
                        Text.literal("Your waypoints for ")
                ).append(
                        Text.literal("Plot ")
                                .withColor(0x55FFFF)
                ).append(
                        Text.literal(String.valueOf(Flint.getUser().getPlot().getId()))
                                .withColor(0x55FFFF)
                ).append(
                        Text.literal(":")
                );

        List<Waypoint> waypoints = Waypoints.getWaypoints(Flint.getUser().getPlot().getId());
        List<String> names = waypoints
                .stream()
                .map(Waypoint::getName)
                .sorted()
                .toList();

        MessageSystem.InfoMessage(waypointListMessage);

        for (String name : names) {

            MutableText listWaypoint = Text.literal(" » ")
                    .withColor(0xFFFFFF)
                    .append(
                            Text.literal(name)
                                    .withColor(0xFFFFFF)
                    )
                    .append(
                            Text.literal(" - ")
                                    .withColor(0xAAAAAA)
                    )
                    .append(
                            Text.literal("[")
                                    .withColor(0x55FF55)
                                    .styled(style -> style.withClickEvent(
                                                    new ClickEvent.RunCommand("/wtp " + name)
                                            )
                                    )
                                    .styled(style -> style.withHoverEvent(
                                                    new HoverEvent.ShowText(teleportHover)
                                            )
                                    )
                    )
                    .append(
                            Text.literal("⌘")
                                    .withColor(0x23FF24)
                                    .styled(style -> style.withClickEvent(
                                                    new ClickEvent.RunCommand("/wtp " + name)
                                            )
                                    )
                                    .styled(style -> style.withHoverEvent(
                                                    new HoverEvent.ShowText(teleportHover)
                                            )
                                    )
                    )
                    .append(
                            Text.literal("] ")
                                    .withColor(0x55FF55)
                                    .styled(style -> style.withClickEvent(
                                                    new ClickEvent.RunCommand("/wtp " + name)
                                            )
                                    )
                                    .styled(style -> style.withHoverEvent(
                                                    new HoverEvent.ShowText(teleportHover)
                                            )
                                    )
                    )
                    .append(
                            Text.literal("[")
                                    .withColor(0xFF5555)
                                    .styled(style -> style.withClickEvent(
                                                    new ClickEvent.RunCommand("/waypoint delete " + name)
                                            )
                                    )
                                    .styled(style -> style.withHoverEvent(
                                                    new HoverEvent.ShowText(deleteHover)
                                            )
                                    )
                    )
                    .append(
                            Text.literal("✘")
                                    .withColor(0xFF3535)
                                    .styled(style -> style.withClickEvent(
                                                    new ClickEvent.RunCommand("/waypoint delete " + name)
                                            )
                                    )
                                    .styled(style -> style.withHoverEvent(
                                                    new HoverEvent.ShowText(deleteHover)
                                            )
                                    )
                    )
                    .append(
                            Text.literal("]")
                                    .withColor(0xFF5555)
                                    .styled(style -> style.withClickEvent(
                                                    new ClickEvent.RunCommand("/waypoint delete " + name)
                                            )
                                    )
                                    .styled(style -> style.withHoverEvent(
                                                    new HoverEvent.ShowText(deleteHover)
                                            )
                                    )
                    );


            Dfwaypoints.MC.player.sendMessage(listWaypoint, false);
        }
        return 0;
    }

    public static int teleportToWaypoint(CommandContext<FabricClientCommandSource> context, String text) throws InterruptedException {
        assert Flint.getClient().player != null;
        assert Dfwaypoints.MC.player != null;

        if (Flint.getUser().getPlot() == null) {
            MessageSystem.ErrorMessage(
                    Text.literal("You must be on a plot to teleport to waypoints!")
            );
            return 0;
        }

        if (!Flint.getUser().getMode().isEditor()) {
            MessageSystem.ErrorMessage(
                    Text.literal("You must be in dev/build mode to teleport to waypoints!")
            );
            return 0;
        }

        List<Waypoint> waypoints = Waypoints.getWaypoints(Flint.getUser().getPlot().getId());

        for (Waypoint waypoint : waypoints) {
            if (Objects.equals(waypoint.getName(), text)) {

                Vec3d position = waypoint.getPosition();

                teleported = true;
                CommandSender.queue("ptp " + position.x + " " + position.y + " " + position.z);

                return 0;

            }
        }

        MessageSystem.ErrorMessage(
                Text.literal("That waypoint does not exist!")
        );

        return 0;
    }

    public int deleteWaypoint(CommandContext<FabricClientCommandSource> context, String text) {
        assert Dfwaypoints.MC.player != null;

        if (Flint.getUser().getPlot() == null) {
            MessageSystem.ErrorMessage(
                    Text.literal("You must be on a plot to delete waypoints!")
            );
            return 0;
        }

        List<Waypoint> waypoints = Waypoints.getWaypoints(Flint.getUser().getPlot().getId());

        for (Waypoint waypoint : waypoints) {
            if (Objects.equals(waypoint.getName(), text)) {

                Waypoints.removeWaypoint(Flint.getUser().getPlot().getId(), waypoint);
                try {
                    Waypoints.save();
                    MessageSystem.SuccessMessage(
                            Text.literal("Waypoint Deleted. ")
                    );
                } catch (IOException e) {
                    MessageSystem.ErrorMessage(
                            Text.literal("DFWaypoints ran into an issue saving waypoint data. Your waypoint has been deleted, but may not save upon closing the game.")
                    );
                }

                return 0;
            }

        }

        MessageSystem.ErrorMessage(
                Text.literal("That waypoint does not exist!")
        );

        return 0;

    }
}