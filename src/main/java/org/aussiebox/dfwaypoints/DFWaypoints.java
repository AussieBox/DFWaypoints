package org.aussiebox.dfwaypoints;

import dev.dfonline.flint.FlintAPI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.aussiebox.dfwaypoints.config.DFWConfig;
import org.aussiebox.dfwaypoints.features.WaypointCommands;
import org.aussiebox.dfwaypoints.features.WaypointTeleportCommand;
import org.aussiebox.dfwaypoints.render.WaypointHudRenderer;
import org.aussiebox.dfwaypoints.util.CommandSender;
import org.aussiebox.dfwaypoints.waypoints.Waypoint;
import org.aussiebox.dfwaypoints.waypoints.Waypoints;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DFWaypoints implements ClientModInitializer {
    public static final MinecraftClient MC = MinecraftClient.getInstance();
    public static final String MOD_ID = "dfwaypoints";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static KeyBinding lookwarp;

    @Override
    public void onInitializeClient() {
        FlintAPI.confirmLocationWithLocate();
        Waypoints.init();

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            DFWConfig.HANDLER.save();
            try {
                Waypoints.save();
            } catch (IOException e) {
                LOGGER.error("Failed to save Waypoints: %s", e);
            }
        });
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            CommandSender.tick();
        });

        DFWConfig.HANDLER.load();
        DFWaypoints.LOGGER.info("[1/5] Config has been loaded.");

        FlintAPI.registerFeature(
                new WaypointCommands()
        );
        DFWaypoints.LOGGER.info("[2/5] WaypointCommands has been registered.");

        FlintAPI.registerFeature(
                new WaypointTeleportCommand()
        );
        DFWaypoints.LOGGER.info("[3/5] WaypointTeleportCommand has been registered.");

        registerKeybinds();
        DFWaypoints.LOGGER.info("[4/5] Keybinds have been registered.");

        HudElementRegistry.addFirst(Identifier.of(DFWaypoints.MOD_ID, "waypoint_hud"), WaypointHudRenderer::render);
        DFWaypoints.LOGGER.info("[5/5] WaypointHudRenderer has been registered.");

        DFWaypoints.LOGGER.info("DFWaypoints is up and running!");
        DFWaypoints.LOGGER.info("Report bugs or suggest features at https://github.com/AussieBox/DFWaypoints/issues.");
    }

    public void registerKeybinds() {
        final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of(DFWaypoints.MOD_ID, "keybinds"));

        lookwarp = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dfwaypoints.lookwarp",
                InputUtil.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (lookwarp.wasPressed()) {
                if (Waypoints.waypointsLookingAt.isEmpty()) return;
                Waypoint target = Waypoints.waypointsLookingAt.keySet().stream().toList().getFirst();
                try {
                    WaypointCommands.teleportToWaypoint(target.getName());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
