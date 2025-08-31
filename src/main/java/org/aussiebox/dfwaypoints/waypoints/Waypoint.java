package org.aussiebox.dfwaypoints.waypoints;

import com.google.gson.JsonObject;
import net.minecraft.util.math.Vec3d;

public class Waypoint {
    private final String name;
    private final Vec3d position;

    public Waypoint(String name, Vec3d position) {
        this.name = name;
        this.position = position;
    }

    public static Waypoint fromJson(JsonObject json) {
        String name = json.get("name").getAsString();
        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();
        return new Waypoint(name, new Vec3d(x, y, z));
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("x", position.x);
        json.addProperty("y", position.y);
        json.addProperty("z", position.z);
        return json;
    }

    public String getName() {
        return name;
    }

    public Vec3d getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "Waypoint [position=" + position + "]";
    }
}