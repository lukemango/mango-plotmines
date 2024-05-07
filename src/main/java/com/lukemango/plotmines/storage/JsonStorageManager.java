package com.lukemango.plotmines.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lukemango.plotmines.PlotMines;
import com.lukemango.plotmines.manager.MineManager;
import com.lukemango.plotmines.manager.impl.Mine;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Set;

public class JsonStorageManager {

    /**
     * Save a mine to the JSON file
     */
    public static void saveAll() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            File file = new File(PlotMines.getInstance().getDataFolder().getAbsolutePath() + "/data/minedata.json");

            if (!file.exists()) {
                file.getParentFile().mkdir(); // Creates the /data/
                file.createNewFile(); // Creates the /data/minedata.json
            }

            final Set<Mine> mines = MineManager.getMines();
            if (mines != null) {
                Writer writer = new FileWriter(file, false);
                gson.toJson(mines, writer);
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
    public static void load() {
        try {
            Gson gson = new Gson();
            File file = new File(PlotMines.getInstance().getDataFolder().getAbsolutePath() + "/data/minedata.json");

            if (!file.exists()) {
                file.getParentFile().mkdir(); // Creates the /data/
                file.createNewFile(); // Creates the /data/minedata.json
            }

            if (file.exists()) {
                Reader reader = new FileReader(file);
                TypeToken<Set<Mine>> typeToken = new TypeToken<>() {};
                Set<Mine> mineSet = gson.fromJson(reader, typeToken.getType());
                if (mineSet != null) {
                    MineManager.setMines(mineSet);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
