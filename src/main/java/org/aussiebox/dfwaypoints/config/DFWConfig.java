package org.aussiebox.dfwaypoints.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
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

import java.util.ArrayList;
import java.util.List;

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
        YetAnotherConfigLib config = YetAnotherConfigLib.createBuilder()
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
                        .build())
                .build();

        return config;
    }

    public static void openConfig() {
        MinecraftClient.getInstance().setScreen(DFWConfig.getLibConfig().generateScreen(MinecraftClient.getInstance().currentScreen));
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
