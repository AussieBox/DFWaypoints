package org.aussiebox.dfwaypoints.config;

import dev.dfonline.flint.Flint;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import org.aussiebox.dfwaypoints.DFWaypoints;
import org.aussiebox.dfwaypoints.waypoints.Waypoint;
import org.aussiebox.dfwaypoints.waypoints.WaypointType;
import org.aussiebox.dfwaypoints.waypoints.Waypoints;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DFWConfig {
    public static ConfigClassHandler<DFWConfig> HANDLER = ConfigClassHandler.createBuilder(DFWConfig.class)
            .id(Identifier.of(DFWaypoints.MOD_ID, "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getGameDir().resolve("DFWaypoints/config.json5"))
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry(comment = "Shows a tip reading \"[Keybind] Warp\" when looking towards a waypoint.")
    public static boolean showLookwarpTip = true;

    @SerialEntry(comment = "Sets the alignment of waypoint lists shown on the HUD.")
    public static WaypointHudAlignment waypointHudAlignment = WaypointHudAlignment.MIDDLE_CENTER;

    public static YetAnotherConfigLib getLibConfig() {
        YetAnotherConfigLib.Builder config = YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("config.dfwaypoints.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("config.dfwaypoints.category.general"))
                        .tooltip(Text.translatable("config.dfwaypoints.category.general.tooltip"))
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("config.dfwaypoints.category.rendering"))
                        .tooltip(Text.translatable("config.dfwaypoints.category.rendering.tooltip"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.dfwaypoints.rendering.show_lookwarp_tip"))
                                .description(OptionDescription.of(Text.translatable("config.dfwaypoints.rendering.show_lookwarp_tip.desc")))
                                .binding(true, () -> showLookwarpTip, newVal -> showLookwarpTip = newVal)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<WaypointHudAlignment>createBuilder()
                                .name(Text.translatable("config.dfwaypoints.rendering.waypoint_hud_alignment"))
                                .description(OptionDescription.of(Text.translatable("config.dfwaypoints.rendering.waypoint_hud_alignment.desc")))
                                .binding(WaypointHudAlignment.MIDDLE_CENTER, () -> waypointHudAlignment, newVal -> waypointHudAlignment = newVal)
                                .controller(opt -> EnumControllerBuilder.create(opt)
                                        .enumClass(WaypointHudAlignment.class)
                                        .formatValue(value -> Text.translatable("config.dfwaypoints.alignment." + value.toString().toLowerCase()))
                                )
                                .build())
                        .build());

        if (Flint.getUser().getPlot() == null) return config.build();
        Map<WaypointType, Waypoint[]> waypoints = Waypoints.getWaypoints(Flint.getUser().getPlot().getId());

        ConfigCategory.Builder waypointsCategory = ConfigCategory.createBuilder()
                .name(Text.translatable("config.dfwaypoints.category.waypoints"))
                .tooltip(Text.translatable("config.dfwaypoints.category.waypoints.tooltip"));

        for (Map.Entry<WaypointType, Waypoint[]> waypointList : waypoints.entrySet()) {
            for (Waypoint waypoint : waypointList.getValue()) {
                OptionGroup.Builder waypointGroup = OptionGroup.createBuilder()
                        .name(Text.literal(waypoint.getName()))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.dfwaypoints.waypoints.render_waypoint"))
                                .description(OptionDescription.of(Text.translatable("config.dfwaypoints.waypoints.render_waypoint.desc")))
                                .binding(true, () -> waypoint.render, newVal -> waypoint.render = newVal)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("config.dfwaypoints.waypoints.waypoint_color"))
                                .description(OptionDescription.of(Text.translatable("config.dfwaypoints.waypoints.waypoint_color.desc")))
                                .binding(new Color(0xFF8CF4E2), () -> waypoint.waypointColor, newVal -> waypoint.waypointColor = newVal)
                                .controller(opt -> ColorControllerBuilder.create(opt)
                                        .allowAlpha(true))
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("config.dfwaypoints.waypoints.text_color"))
                                .description(OptionDescription.of(Text.translatable("config.dfwaypoints.waypoints.text_color.desc")))
                                .binding(new Color(0xFFFFFFFF), () -> waypoint.textColor, newVal -> waypoint.textColor = newVal)
                                .controller(opt -> ColorControllerBuilder.create(opt)
                                        .allowAlpha(true))
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("config.dfwaypoints.waypoints.text_outline_color"))
                                .description(OptionDescription.of(Text.translatable("config.dfwaypoints.waypoints.text_outline_color.desc")))
                                .binding(new Color(0xFF000000), () -> waypoint.textOutlineColor, newVal -> waypoint.textOutlineColor = newVal)
                                .controller(opt -> ColorControllerBuilder.create(opt)
                                        .allowAlpha(true))
                                .build());
                waypointsCategory.group(waypointGroup.build());
            }
        }

        config.category(waypointsCategory.build());
        return config.build();
    }

    public static int openConfig() {
        MinecraftClient.getInstance().setScreen(DFWConfig.getLibConfig().generateScreen(MinecraftClient.getInstance().currentScreen));
        return 0;
    }

    public enum WaypointHudAlignment implements StringIdentifiable {
        TOP_LEFT("top_left"),
        TOP_CENTER("top_center"),
        TOP_RIGHT("top_right"),
        MIDDLE_LEFT("middle_left"),
        MIDDLE_CENTER("middle_center"),
        MIDDLE_RIGHT("middle_right"),
        BOTTOM_LEFT("bottom_left"),
        BOTTOM_CENTER("bottom_center"),
        BOTTOM_RIGHT("bottom_right");

        private final String id;

        WaypointHudAlignment(String id) {
            this.id = id;
        }

        public static WaypointHudAlignment fromId(String id) {
            for (WaypointHudAlignment alignment : values()) {
                if (alignment.id.equals(id)) return alignment;
            }
            throw new IllegalArgumentException("Unknown WaypointHudAlignment ID: " + id);
        }

        public static List<String> toIDList() {
            List<String> list = new ArrayList<>();
            for (WaypointHudAlignment option : WaypointHudAlignment.values()) {
                list.add(option.asString());
            }
            return list;
        }

        @Override
        public String asString() {
            return id;
        }
    }
}
