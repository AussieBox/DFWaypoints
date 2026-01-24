package org.aussiebox.dfwaypoints.waypoints;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.dfonline.flint.hypercube.Node;
import it.unimi.dsi.fastutil.objects.Object2DoubleLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.aussiebox.dfwaypoints.DFWaypoints;
import org.aussiebox.dfwaypoints.util.FileUtil;

import java.io.IOException;
import java.util.*;

public class Waypoints {
    public static final Object2DoubleMap<Waypoint> waypointsLookingAt = new Object2DoubleLinkedOpenHashMap<>();
    public static final Object2ObjectMap<Integer, Node> nodes = new Object2ObjectLinkedOpenHashMap<>();
    private static final Object2ObjectMap<Integer, Object2ObjectMap<WaypointType, Waypoint[]>> waypoints = new Object2ObjectLinkedOpenHashMap<>();

    public static void init() {
        JsonObject json = FileUtil.loadJSON("plots.json", null);
        if (json.getAsJsonArray("plots") == null) return;
        for (JsonElement plotElement : json.getAsJsonArray("plots")) {
            Integer id = plotElement.getAsJsonObject().get("id").getAsInt();
            Node node = Node.fromId(plotElement.getAsJsonObject().get("node").getAsString());
            Object2ObjectMap<WaypointType, Waypoint[]> plotData = new Object2ObjectLinkedOpenHashMap<>();

            JsonObject plotJson = FileUtil.loadJSON(id + "-waypoints.json", node);
            for (String type : plotJson.keySet()) {
                JsonArray array = plotJson.getAsJsonArray(type);
                List<Waypoint> waypointList = new ArrayList<>();
                for (JsonElement waypoint : array) {
                    JsonObject waypointObject = waypoint.getAsJsonObject();
                    waypointList.add(Waypoint.fromJson(waypointObject, WaypointType.fromId(type)));
                }
                plotData.put(WaypointType.fromId(type), waypointList.toArray(new Waypoint[]{}));
            }
            waypoints.put(id, plotData);
            nodes.put(id, node);
        }
    }

    public static void save() throws IOException {
        JsonObject plotJson = new JsonObject();
        JsonArray plotArray = new JsonArray();

        for (Map.Entry<Integer, Object2ObjectMap<WaypointType, Waypoint[]>> entry : waypoints.entrySet()) {
            JsonObject toSave = new JsonObject();
            JsonArray typeArray = new JsonArray();
            boolean wipe = true;
            for (WaypointType type : entry.getValue().keySet()) {
                Waypoint[] typeWaypoints = entry.getValue().get(type);
                if (!Arrays.stream(typeWaypoints).toList().isEmpty()) wipe = false;
                for (Waypoint waypoint : typeWaypoints) {
                    typeArray.add(waypoint.toJson());
                }
                toSave.add(type.asString(), typeArray);
            }
            if (!wipe) {
                JsonObject plotObject = new JsonObject();
                plotObject.addProperty("id", entry.getKey());
                plotObject.addProperty("node", nodes.getOrDefault(entry.getKey(), Node.LOCAL).getId());
                plotArray.add(plotObject);
                FileUtil.saveJSON(entry.getKey() + "-waypoints.json", nodes.getOrDefault(entry.getKey(), Node.LOCAL), toSave);
            } else {
                DFWaypoints.LOGGER.info("No Waypoints found for plot {}, removing...", entry.getKey());
                FileUtil.deleteJSON(entry.getKey() + "-waypoints.json", nodes.remove(entry.getKey()));
            }
        }
        plotJson.add("plots", plotArray);
        FileUtil.saveJSON("plots.json", null, plotJson);
    }

    public static Map<WaypointType, Waypoint[]> getWaypoints(Integer id) {
        return Collections.unmodifiableMap(waypoints.computeIfAbsent(id, k -> new Object2ObjectLinkedOpenHashMap<>()));
    }

    public static List<Waypoint> getWaypointsOfType(Integer id, WaypointType type) {
        Object2ObjectMap<WaypointType, Waypoint[]> map = waypoints.computeIfAbsent(id, k -> waypoints.put(id, new Object2ObjectLinkedOpenHashMap<>()));
        if (map == null) map = new Object2ObjectLinkedOpenHashMap<>();
        return List.of(map.computeIfAbsent(type, k -> new Waypoint[]{}));
    }

    public static void setWaypointsOfType(Integer id, WaypointType type, List<Waypoint> waypointArray) {
        Object2ObjectMap<WaypointType, Waypoint[]> map = waypoints.computeIfAbsent(id, k -> waypoints.put(id, new Object2ObjectLinkedOpenHashMap<>()));
        if (map == null) map = new Object2ObjectLinkedOpenHashMap<>();
        map.put(type, waypointArray.toArray(new Waypoint[]{}));
    }

    public static void addWaypoint(Integer id, WaypointType type, Waypoint waypoint) {
        List<Waypoint> list = new ArrayList<>(getWaypointsOfType(id, type));
        list.add(waypoint);
        setWaypointsOfType(id, type, list);
    }

    public static void removeWaypoint(Integer id, WaypointType type, Waypoint waypoint) {
        List<Waypoint> list = new ArrayList<>(getWaypointsOfType(id, type));
        list.remove(waypoint);
        setWaypointsOfType(id, type, list);
    }
}