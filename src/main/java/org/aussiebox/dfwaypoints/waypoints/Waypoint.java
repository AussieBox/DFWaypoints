package org.aussiebox.dfwaypoints.waypoints;

import com.google.gson.JsonObject;
import net.minecraft.util.math.Vec3d;

public class Waypoint {
    private final String name;
    private final WaypointType type;
    private final Vec3d position;
    public int waypointColor;
    public int textColor;
    public int textOutlineColor;

    public Waypoint(String name, WaypointType type, Vec3d position) {
        this.name = name;
        this.type = type;
        this.position = position;
        this.waypointColor = 0xFF8CF4E2;
        this.textColor = 0xFFFFFFFF;
        this.textOutlineColor = 0xFF000000;
    }

    public static Waypoint fromJson(JsonObject json, WaypointType type) {
        String name = json.get("name").getAsString();
        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();

        Waypoint waypoint = new Waypoint(name, type, new Vec3d(x, y, z));
        waypoint.waypointColor = json.get("waypoint_color").getAsInt();
        waypoint.textColor = json.get("text_color").getAsInt();
        waypoint.textOutlineColor = json.get("text_outline_color").getAsInt();

        return waypoint;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("x", position.x);
        json.addProperty("y", position.y);
        json.addProperty("z", position.z);
        json.addProperty("waypoint_color", waypointColor);
        json.addProperty("text_color", textColor);
        json.addProperty("text_outline_color", textOutlineColor);
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