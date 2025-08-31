package org.aussiebox.dfwaypoints;

import dev.dfonline.flint.FlintAPI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.aussiebox.dfwaypoints.features.WaypointCommands;
import org.aussiebox.dfwaypoints.features.WaypointTeleportCommand;
import org.aussiebox.dfwaypoints.util.CommandSender;
import org.aussiebox.dfwaypoints.waypoints.Waypoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Dfwaypoints implements ClientModInitializer {
    public static final MinecraftClient MC = MinecraftClient.getInstance();
    public static final String MOD_ID = "Dfwaypoints";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        FlintAPI.confirmLocationWithLocate();
        Waypoints.init();

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            try {
                Waypoints.save();
            } catch (IOException e) {
                LOGGER.error("Failed to save Waypoints: %s", e);
            }
        });
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            CommandSender.tick();
        });

        FlintAPI.registerFeature(
                new WaypointCommands()
        );
        Dfwaypoints.LOGGER.info("[1/2] WaypointCommands has been registered.");
        FlintAPI.registerFeature(
                new WaypointTeleportCommand()
        );
        Dfwaypoints.LOGGER.info("[2/2] WaypointTeleportCommand has been registered.");
        Dfwaypoints.LOGGER.info("DFWaypoints is up and running!");
    }
}
