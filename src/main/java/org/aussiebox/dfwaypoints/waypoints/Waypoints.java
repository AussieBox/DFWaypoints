package org.aussiebox.dfwaypoints.waypoints;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.aussiebox.dfwaypoints.util.FileUtil;

import java.io.IOException;
import java.util.*;

public class Waypoints {
    private static final Map<Integer, List<Waypoint>> waypoints = new HashMap<>();

    public static void init() {
        JsonObject json = FileUtil.loadJSON("waypoints.json");
        for (String plotID : json.keySet()) {
            Integer id = Integer.parseInt(plotID);
            JsonArray waypoints = json.getAsJsonArray(plotID);
            List<Waypoint> waypointList = new ArrayList<>();
            for (JsonElement waypoint : waypoints) {
                JsonObject waypointObject = waypoint.getAsJsonObject();
                waypointList.add(Waypoint.fromJson(waypointObject));
            }
            Waypoints.waypoints.put(id, waypointList);
        }
    }

    public static void save() throws IOException {
        JsonObject toSave = new JsonObject();
        for (Map.Entry<Integer, List<Waypoint>> entry : waypoints.entrySet()) {
            JsonArray waypoints = new JsonArray();
            for (Waypoint waypoint : entry.getValue()) {
                waypoints.add(waypoint.toJson());
            }
            toSave.add(entry.getKey().toString(), waypoints);
        }
        FileUtil.saveJSON("waypoints.json", toSave);
    }

    public static List<Waypoint> getWaypoints(Integer id) {
        return Collections.unmodifiableList(waypoints.computeIfAbsent(id, k -> new ArrayList<>()));
    }

    public static void addWaypoint(Integer id, Waypoint waypoint) {
        waypoints.computeIfAbsent(id, k -> new ArrayList<>()).add(waypoint);
    }

    public static void removeWaypoint(Integer id, Waypoint waypoint) {
        waypoints.computeIfAbsent(id, k -> new ArrayList<>()).remove(waypoint);
    }
}