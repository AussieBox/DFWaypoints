package org.aussiebox.dfwaypoints.waypoints;

import com.google.gson.JsonObject;
import net.minecraft.util.math.Vec3d;
import org.aussiebox.dfwaypoints.config.DFWConfig;

import java.awt.*;

public class Waypoint {
    private final String name;
    private final WaypointType type;
    private final Vec3d position;
    public boolean render;
    public Color waypointColor;
    public Color textColor;
    public Color textOutlineColor;

    public Waypoint(String name, WaypointType type, Vec3d position) {
        this.name = name;
        this.type = type;
        this.position = position;
        this.render = true;
        this.waypointColor = DFWConfig.defaultWaypointColor;
        this.textColor = DFWConfig.defaultTextColor;
        this.textOutlineColor = DFWConfig.defaultTextOutlineColor;
    }

    public static Waypoint fromJson(JsonObject json, WaypointType type) {
        String name = json.get("name").getAsString();
        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();

        Waypoint waypoint = new Waypoint(name, type, new Vec3d(x, y, z));
        waypoint.render = !json.has("render") || json.get("render").getAsBoolean();
        waypoint.waypointColor = json.has("waypoint_color") ? new Color(json.get("waypoint_color").getAsInt()) : DFWConfig.defaultWaypointColor;
        waypoint.textColor = json.has("text_color") ? new Color(json.get("text_color").getAsInt()) : DFWConfig.defaultTextColor;
        waypoint.textOutlineColor = json.has("text_outline_color") ? new Color(json.get("text_outline_color").getAsInt()) : DFWConfig.defaultTextOutlineColor;

        return waypoint;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("x", position.x);
        json.addProperty("y", position.y);
        json.addProperty("z", position.z);
        json.addProperty("render", render);
        json.addProperty("waypoint_color", waypointColor.getRGB());
        json.addProperty("text_color", textColor.getRGB());
        json.addProperty("text_outline_color", textOutlineColor.getRGB());
        return json;
    }

    public String getName() {
        return name;
    }

    public WaypointType getType() {
        return type;
    }

    public Vec3d getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "Waypoint(" + type.asString() + ") [position=" + position + "]";
    }
}