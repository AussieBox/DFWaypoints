package org.aussiebox.dfwaypoints.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dfonline.flint.hypercube.Node;
import org.aussiebox.dfwaypoints.DFWaypoints;
import org.aussiebox.dfwaypoints.waypoints.Waypoints;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {
    public static JsonObject loadJSONExternal(String folder, String fileName, Node node, boolean shouldCreateIfNotExist) {
        File file = new File(folder + File.separator + fileName);
        if (node != null) file = new File(folder + File.separator + node.getName() + File.separator + fileName);

        if (!file.exists()) {
            if (shouldCreateIfNotExist) {
                try {
                    if (file.getParentFile() != null) {
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                    }
                    file.createNewFile();
                } catch (IOException e) {
                    DFWaypoints.LOGGER.error("Failed to create {} empty file", fileName);
                }
            }
            return new JsonObject();
        }
        try {
            String jsonStr = new String(Files.readAllBytes(file.toPath()));
            return JsonParser.parseString(jsonStr).getAsJsonObject();
        } catch (Exception e) {
            DFWaypoints.LOGGER.error("Failed to load {} (invalid format?)", fileName);
        }
        return new JsonObject();
    }

    public static JsonObject loadJSON(String filename, Node node) {
        return loadJSONExternal("DFWaypoints", filename, node, true);
    }

    public static void saveJSON(String filename, Node node, JsonObject data) throws IOException {
        File file = new File("DFWaypoints" + File.separator + filename);
        if (node != null) file = new File("DFWaypoints" + File.separator + node.getName() + File.separator + filename);

        try {
            if (file.getParentFile() != null) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
            }
            file.createNewFile();
        } catch (IOException e) {
            DFWaypoints.LOGGER.error("Failed to create {} empty file", filename);
        }

        FileWriter fileWriter = new FileWriter(file);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        DFWaypoints.LOGGER.info("Saving: {}", filename);

        fileWriter.write(gson.toJson(data));
        fileWriter.flush();
        fileWriter.close();
    }

    public static void deleteJSON(String filename, Node node) throws IOException {
        if (node == null) Files.deleteIfExists(Path.of("DFWaypoints" + File.separator + filename));
        else {
            Files.deleteIfExists(Path.of("DFWaypoints" + File.separator + node.getName() + File.separator + filename));
            if (!Waypoints.nodes.containsValue(node)) Files.deleteIfExists(Path.of("DFWaypoints" + File.separator + node.getName()));
        }
    }
}
