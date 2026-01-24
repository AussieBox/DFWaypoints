package org.aussiebox.dfwaypoints.waypoints;

import net.minecraft.util.StringIdentifiable;

public enum WaypointType implements StringIdentifiable {
    WAYPOINT("waypoint");

    private final String id;

    WaypointType(String id) {
        this.id = id;
    }

    public static WaypointType fromId(String id) {
        for (WaypointType type : values()) {
            if (type.id.equals(id)) return type;
        }
        throw new IllegalArgumentException("Unknown WaypointType ID: " + id);
    }

    @Override
    public String asString() {
        return id;
    }
}
