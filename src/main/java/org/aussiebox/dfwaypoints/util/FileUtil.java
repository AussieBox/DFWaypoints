package org.aussiebox.dfwaypoints.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.aussiebox.dfwaypoints.Dfwaypoints;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtil {
    public static JsonObject loadJSONExternal(String folder, String fileName, boolean shouldCreateIfNotExist) {
        File file = new File(folder + File.separator + fileName);
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
                    Dfwaypoints.LOGGER.error("Failed to create {} empty file", fileName);
                }
            }
            return new JsonObject();
        }
        try {
            String jsonStr = new String(Files.readAllBytes(file.toPath()));
            return JsonParser.parseString(jsonStr).getAsJsonObject();
        } catch (Exception e) {
            Dfwaypoints.LOGGER.error("Failed to load {} (invalid format?)", fileName);
        }
        return new JsonObject();
    }

    public static JsonObject loadJSON(String filename) {
        return loadJSONExternal("Dfwaypoints", filename, true);
    }

    public static void saveJSON(String filename, JsonObject data) throws IOException {
        File file = new File("Dfwaypoints" + File.separator + filename);

        FileWriter fileWriter = new FileWriter(file);

        Dfwaypoints.LOGGER.info("Saving: {}", filename);

        fileWriter.write(data.toString());
        fileWriter.flush();
        fileWriter.close();
    }
}
