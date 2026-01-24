package org.aussiebox.dfwaypoints.features;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.dfonline.flint.Flint;
import dev.dfonline.flint.feature.trait.CommandFeature;
import dev.dfonline.flint.feature.trait.PacketListeningFeature;
import dev.dfonline.flint.util.result.EventResult;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.HexColorArgumentType;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import org.aussiebox.dfwaypoints.DFWaypoints;
import org.aussiebox.dfwaypoints.config.DFWConfig;
import org.aussiebox.dfwaypoints.helpers.MessageSystem;
import org.aussiebox.dfwaypoints.util.CommandSender;
import org.aussiebox.dfwaypoints.waypoints.Waypoint;
import org.aussiebox.dfwaypoints.waypoints.WaypointType;
import org.aussiebox.dfwaypoints.waypoints.Waypoints;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class WaypointCommands implements CommandFeature, PacketListeningFeature {

    static boolean teleported = false;

    @Override
    public EventResult onReceivePacket(Packet<?> packet) {
        if (packet instanceof PlaySoundS2CPacket || packet instanceof PlaySoundFromEntityS2CPacket) {
            if (teleported) {
                teleported = false;
                return EventResult.PASS;
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
        var waypointEntry = argument("waypoint", StringArgumentType.greedyString())
                .suggests((context, suggestionsBuilder) -> {
                    if (Flint.getUser().getPlot() == null) {
                        return suggestionsBuilder.buildFuture();
                    }

                    List<String> names = new ArrayList<>();
                    Map<WaypointType, Waypoint[]> waypoints = Waypoints.getWaypoints(Flint.getUser().getPlot().getId());
                    for (Waypoint[] waypointList : waypoints.values()) {
                        for (Waypoint waypoint : waypointList) {
                            names.add(waypoint.getName());
                        }
                    }

                    for (String name : names.stream().filter(name -> name.startsWith(suggestionsBuilder.getRemaining())).sorted().toList()) {
                        suggestionsBuilder.suggest(name);
                    }
                    return suggestionsBuilder.buildFuture();
                });

        return builder.then(
                literal("create").then(
                        argument("waypoint", StringArgumentType.greedyString())
                                .executes(context -> createWaypoint(context, StringArgumentType.getString(context, "waypoint")))
                )
        ).then(
                literal("list").executes(this::listWaypoints)
        ).then(
                literal("teleport").then(waypointEntry
                        .executes(context -> {
                            try {
                                return teleportToWaypoint(StringArgumentType.getString(context, "waypoint"));
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        })
                )
        ).then(
                literal("tp").then(waypointEntry
                        .executes(context -> {
                            try {
                                return teleportToWaypoint(StringArgumentType.getString(context, "waypoint"));
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        })
                )
        ).then(
                literal("delete").then(waypointEntry
                        .executes(context -> deleteWaypoint(context, StringArgumentType.getString(context, "waypoint")))
                )
        ).then(
                literal("remove").then(waypointEntry
                        .executes(context -> deleteWaypoint(context, StringArgumentType.getString(context, "waypoint")))
                )
        ).then(
                literal("appearance")
                        .then(literal("render")
                                .then(argument("boolean", BoolArgumentType.bool())
                                        .then(waypointEntry
                                                .executes((context) -> setBooleanSetting(context, StringArgumentType.getString(context, "waypoint"), BooleanSetting.RENDER, context.getArgument("boolean", Boolean.class)))))
                        )
                        .then(literal("waypoint_color")
                                .then(argument("color", HexColorArgumentType.hexColor())
                                        .then(waypointEntry
                                                .executes((context) -> setColorSetting(context, StringArgumentType.getString(context, "waypoint"), ColorSetting.WAYPOINT_COLOR, context.getArgument("color", Integer.class)))))
                        )
                        .then(literal("text_color")
                                .then(argument("color", HexColorArgumentType.hexColor())
                                        .then(waypointEntry
                                                .executes((context) -> setColorSetting(context, StringArgumentType.getString(context, "waypoint"), ColorSetting.TEXT_COLOR, context.getArgument("color", Integer.class)))))
                        )
                        .then(literal("text_outline_color")
                                .then(argument("color", HexColorArgumentType.hexColor())
                                        .then(waypointEntry
                                                .executes((context) -> setColorSetting(context, StringArgumentType.getString(context, "waypoint"), ColorSetting.TEXT_OUTLINE_COLOR, context.getArgument("color", Integer.class)))))
                        )
        ).then(
                literal("config")
                        .executes((context) -> DFWConfig.openConfig())
        );
    }

    public int createWaypoint(CommandContext<FabricClientCommandSource> context, String text) {
        assert DFWaypoints.MC.player != null;

        if (Flint.getUser().getPlot() == null) {
            MessageSystem.ErrorMessage(
                    Text.translatable("message.dfwaypoints.error.create.not_on_plot"),
                    true
            );
            return 0;
        }

        String plotID = String.valueOf(Flint.getUser().getPlot().getId());
        String rawPlotName = (Flint.getUser().getPlot().getName()) + "replace";
        String plotName = rawPlotName.replace("literal{", "").replace("}replace", "");

        MutableText hoverText = Text.empty()
                .append(
                        Text.translatable("hover.dfwaypoints.info.create.name")
                                .withColor(0xFFFFFF)
                )
                .append(
                        Text.literal(text)
                                .withColor(0xFFFF55)
                )
                .append(
                        Text.translatable("hover.dfwaypoints.info.create.plot")
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
                        Text.translatable("message.dfwaypoints.success.create.created")
                                .styled(style -> style.withHoverEvent(
                                            new HoverEvent.ShowText(hoverText)
                                        )
                                )
                                .styled(style -> style.withFont(new StyleSpriteSource.Font(Identifier.of("minecraft:default"))))
                ).append(
                        Text.translatable("message.dfwaypoints.info.general.hover_for_details")
                                .withColor(0xAAAAAA)
                                .styled(style -> style.withHoverEvent(
                                            new HoverEvent.ShowText(hoverText)
                                        )
                                )
                );

        MutableText waypointCreatedNotSavedMessage = Text.empty()
                .copy().append(
                        Text.translatable("message.dfwaypoints.error.create.could_not_save")
                                .styled(style -> style.withHoverEvent(
                                            new HoverEvent.ShowText(hoverText)
                                        )
                                )
                                .styled(
                                        style -> style.withFont(new StyleSpriteSource.Font(Identifier.of("minecraft:default")))
                                )
                )
                .append(
                        Text.translatable("message.dfwaypoints.info.general.hover_for_details")
                                .withColor(0xAAAAAA)
                                .styled(style -> style.withHoverEvent(
                                            new HoverEvent.ShowText(hoverText)
                                        )
                                )
                );

        Map<WaypointType, Waypoint[]> waypoints = Waypoints.getWaypoints(Flint.getUser().getPlot().getId());

        for (Waypoint[] waypointList : waypoints.values()) {
            for (Waypoint waypoint : waypointList) {
                if (Objects.equals(waypoint.getName(), text)) {

                    MessageSystem.ErrorMessage(
                            Text.translatable("message.dfwaypoints.error.create.waypoint_already_exists"),
                            true
                    );

                    return 0;

                }
            }
        }

        if (DFWaypoints.MC.player != null) {
            Waypoints.addWaypoint(Flint.getUser().getPlot().getId(), WaypointType.WAYPOINT, new Waypoint(text, WaypointType.WAYPOINT, DFWaypoints.MC.player.getEntityPos()));
            Waypoints.nodes.put(Flint.getUser().getPlot().getId(), Flint.getUser().getNode());
            try {
                Waypoints.save();
                MessageSystem.SuccessMessage(waypointCreatedMessage, true);
            } catch (IOException e) {
                MessageSystem.ErrorMessage(waypointCreatedNotSavedMessage, true);
            }
        }
        return 0;
    }

    public int listWaypoints(CommandContext<FabricClientCommandSource> context) {
        assert DFWaypoints.MC.player != null;

        if (Flint.getUser().getPlot() == null) {
            MessageSystem.ErrorMessage(
                    Text.translatable("message.dfwaypoints.error.list.not_on_plot"),
                    true
            );
            return 0;
        }

        MutableText teleportHover = Text.empty()
                .append(
                        Text.translatable("hover.dfwaypoints.info.list.teleport")
                                .withColor(0xFFFFFF)
                                .styled(
                                        style -> style.withFont(new StyleSpriteSource.Font(Identifier.of("minecraft:default")))
                                )
                                .styled(
                                        style -> style.withHoverEvent(null)
                                )
                );

        MutableText deleteHover = Text.empty()
                .append(
                        Text.translatable("hover.dfwaypoints.info.list.delete")
                                .withColor(0xFFFFFF)
                );

        MutableText waypointListMessage = Text.empty()
                .copy().append(
                        Text.translatable("message.dfwaypoints.info.list.waypoint_list.1")
                ).append(
                        Text.translatable("message.dfwaypoints.info.list.waypoint_list.2")
                                .withColor(0x55FFFF)
                ).append(
                        Text.literal(String.valueOf(Flint.getUser().getPlot().getId()))
                                .withColor(0x55FFFF)
                ).append(
                        Text.literal(":")
                );

        Map<WaypointType, Waypoint[]> waypoints = Waypoints.getWaypoints(Flint.getUser().getPlot().getId());
        List<String> names = new ArrayList<>();
        for (Waypoint[] waypointList : waypoints.values()) {
            for (Waypoint waypoint : waypointList) {
                names.add(waypoint.getName());
            }
        }

        MessageSystem.InfoMessage(waypointListMessage, false);

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
            DFWaypoints.MC.player.sendMessage(listWaypoint, false);
        }
        return 0;
    }

    public static int teleportToWaypoint(String text) throws InterruptedException {
        assert Flint.getClient().player != null;
        assert DFWaypoints.MC.player != null;

        if (Flint.getUser().getPlot() == null) {
            MessageSystem.ErrorMessage(
                    Text.translatable("message.dfwaypoints.error.teleport.not_on_plot"),
                    true
            );
            return 0;
        }

        if (!Flint.getUser().getMode().isEditor()) {
            MessageSystem.ErrorMessage(
                    Text.translatable("message.dfwaypoints.error.teleport.not_in_editor"),
                    true
            );
            return 0;
        }

        Map<WaypointType, Waypoint[]> waypoints = Waypoints.getWaypoints(Flint.getUser().getPlot().getId());

        for (Waypoint[] waypointList : waypoints.values()) {
            for (Waypoint waypoint : waypointList) {
                if (Objects.equals(waypoint.getName(), text)) {

                    Vec3d position = waypoint.getPosition();

                    teleported = true;
                    CommandSender.queue("ptp " + position.x + " " + position.y + " " + position.z);

                    return 0;
                }
            }
        }

        MessageSystem.ErrorMessage(
                Text.translatable("message.dfwaypoints.error.general.waypoint_non_existent"),
                true
        );

        return 0;
    }

    public int deleteWaypoint(CommandContext<FabricClientCommandSource> context, String text) {
        assert DFWaypoints.MC.player != null;

        if (Flint.getUser().getPlot() == null) {
            MessageSystem.ErrorMessage(
                    Text.translatable("message.dfwaypoints.error.delete.not_on_plot"),
                    true
            );
            return 0;
        }

        Map<WaypointType, Waypoint[]> waypoints = Waypoints.getWaypoints(Flint.getUser().getPlot().getId());

        for (Waypoint[] waypointList : waypoints.values()) {
            for (Waypoint waypoint : waypointList) {
                if (Objects.equals(waypoint.getName(), text)) {

                    Waypoints.removeWaypoint(Flint.getUser().getPlot().getId(), WaypointType.WAYPOINT, waypoint);
                    try {
                        Waypoints.save();
                        MessageSystem.SuccessMessage(
                                Text.translatable("message.dfwaypoints.success.delete.deleted"),
                                true
                        );
                    } catch (IOException e) {
                        MessageSystem.ErrorMessage(
                                Text.translatable("message.dfwaypoints.error.delete.could_not_save"),
                                true
                        );
                    }
                    return 0;
                }

            }
        }

        MessageSystem.ErrorMessage(
                Text.translatable("message.dfwaypoints.error.general.waypoint_non_existent"),
                true
        );
        return 0;
    }

    public int setColorSetting(CommandContext<FabricClientCommandSource> context, String waypointName, ColorSetting setting, int color) {
        if (Flint.getUser().getPlot() == null) {
            MessageSystem.ErrorMessage(
                    Text.translatable("message.dfwaypoints.error.appearance.not_on_plot"),
                    true
            );
            return 0;
        }

        Map<WaypointType, Waypoint[]> waypoints = Waypoints.getWaypoints(Flint.getUser().getPlot().getId());

        for (Waypoint[] waypointList : waypoints.values()) {
            for (Waypoint waypoint : waypointList) {
                if (Objects.equals(waypoint.getName(), waypointName)) {
                    if (setting == ColorSetting.WAYPOINT_COLOR) {
                        MessageSystem.SuccessMessage(
                                Text.translatable("message.dfwaypoints.success.appearance.set_waypoint_color")
                                        .append(Text.literal(waypoint.getName()).withColor(waypoint.textColor.getRGB()))
                                        .append(Text.translatable("message.dfwaypoints.success.appearance.set_to").withColor(0x8CF4E2))
                                        .append(Text.literal("#" + HexFormat.of().withUpperCase().toHexDigits(ColorHelper.withAlpha(0, color), 6)).withColor(color))
                                        .append(Text.translatable("message.dfwaypoints.general.period").withColor(0x8CF4E2)),
                                true
                        );
                        waypoint.waypointColor = new Color(color);
                    }
                    if (setting == ColorSetting.TEXT_COLOR) {
                        MessageSystem.SuccessMessage(
                                Text.translatable("message.dfwaypoints.success.appearance.set_text_color")
                                        .append(Text.literal(waypoint.getName()).withColor(waypoint.textColor.getRGB()))
                                        .append(Text.translatable("message.dfwaypoints.success.appearance.set_to").withColor(0x8CF4E2))
                                        .append(Text.literal("#" + HexFormat.of().withUpperCase().toHexDigits(ColorHelper.withAlpha(0, color), 6)).withColor(color))
                                        .append(Text.translatable("message.dfwaypoints.general.period").withColor(0x8CF4E2)),
                                true
                        );
                        waypoint.textColor = new Color(color);
                    }
                    if (setting == ColorSetting.TEXT_OUTLINE_COLOR) {
                        MessageSystem.SuccessMessage(
                                Text.translatable("message.dfwaypoints.success.appearance.set_text_outline_color")
                                        .append(Text.literal(waypoint.getName()).withColor(waypoint.textColor.getRGB()))
                                        .append(Text.translatable("message.dfwaypoints.success.appearance.set_to").withColor(0x8CF4E2))
                                        .append(Text.literal("#" + HexFormat.of().withUpperCase().toHexDigits(ColorHelper.withAlpha(0, color), 6)).withColor(color))
                                        .append(Text.translatable("message.dfwaypoints.general.period").withColor(0x8CF4E2)),
                                true
                        );
                        waypoint.textOutlineColor = new Color(color);
                    }
                }
            }
        }
        return 0;
    }

    public int setBooleanSetting(CommandContext<FabricClientCommandSource> context, String waypointName, BooleanSetting setting, boolean bool) {
        if (Flint.getUser().getPlot() == null) {
            MessageSystem.ErrorMessage(
                    Text.translatable("message.dfwaypoints.error.appearance.not_on_plot"),
                    true
            );
            return 0;
        }

        Map<WaypointType, Waypoint[]> waypoints = Waypoints.getWaypoints(Flint.getUser().getPlot().getId());

        for (Waypoint[] waypointList : waypoints.values()) {
            for (Waypoint waypoint : waypointList) {
                if (Objects.equals(waypoint.getName(), waypointName)) {
                    if (setting == BooleanSetting.RENDER) {
                        MessageSystem.SuccessMessage(
                                Text.translatable("message.dfwaypoints.success.appearance.set_rendering")
                                        .append(Text.literal(waypoint.getName()).withColor(waypoint.textColor.getRGB()))
                                        .append(Text.translatable("message.dfwaypoints.success.appearance.set_to").withColor(0x8CF4E2))
                                        .append(Text.literal(String.valueOf(bool)).withColor((bool) ? 0xFF55FF55 : 0xFFFF5555))
                                        .append(Text.translatable("message.dfwaypoints.general.period").withColor(0x8CF4E2)),
                                true
                        );
                        waypoint.render = bool;
                    }
                }
            }
        }
        return 0;
    }

    public enum BooleanSetting {
        RENDER
    }

    public enum ColorSetting {
        WAYPOINT_COLOR,
        TEXT_COLOR,
        TEXT_OUTLINE_COLOR
    }
}