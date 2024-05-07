package com.lukemango.plotmines.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lukemango.plotmines.PlotMines;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JsonMinesToGiveStorage {

    private Map<UUID, List<String>> minesToGiveBack = new HashMap<>();

    private static JsonMinesToGiveStorage instance;

    public JsonMinesToGiveStorage() {
        instance = this;
        this.load();
    }

    /**
     * Save a mine to the JSON file
     */
    public void saveAll() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            File file = new File(PlotMines.getInstance().getDataFolder().getAbsolutePath() + "/data/minestogive.json");

            if (!file.exists()) {
                file.getParentFile().mkdir(); // Creates the /data/
                file.createNewFile(); // Creates the /data/minestogive.json
            }

            if (minesToGiveBack != null) {
                final Writer writer = new FileWriter(file, false);
                gson.toJson(minesToGiveBack, writer);
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load all mines from the JSON file
     */
    public void load() {
        try {
            Gson gson = new Gson();
            File file = new File(PlotMines.getInstance().getDataFolder().getAbsolutePath() + "/data/minestogive.json");

            if (!file.exists()) {
                file.getParentFile().mkdir(); // Creates the /data/
                file.createNewFile(); // Creates the /data/minestogive.json
            }

            if (file.exists()) {
                final Reader reader = new FileReader(file);
                final TypeToken<Map<UUID, List<String>>> typeToken = new TypeToken<>() {
                };
                final Map<UUID, List<String>> mineSet = gson.fromJson(reader, typeToken.getType());
                if (mineSet != null) {
                    minesToGiveBack = mineSet;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addMineToGiveBack(UUID uuid, String mine) {
        this.minesToGiveBack.computeIfAbsent(uuid, k -> new ArrayList<>());
        this.minesToGiveBack.get(uuid).add(mine);
        saveAll();
    }

    public void removeMinesToGiveBack(UUID uuid) {
        this.minesToGiveBack.remove(uuid);
        saveAll();
    }

    public List<String> getMinesToGiveBack(UUID uuid) {
        return this.minesToGiveBack.get(uuid);
    }

    public static JsonMinesToGiveStorage get() {
        return instance;
    }

}
