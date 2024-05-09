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
import java.util.concurrent.CompletableFuture;

public class JsonMineStorage {

    private static JsonMineStorage instance;

    public JsonMineStorage() { // No need for this.load as it is called in the MineManager constructor
        instance = this;
    }

    /**
     * Save a mine to the JSON file
     */
    public void saveAll() {
        PlotMines.getInstance().getServer().getScheduler().runTaskAsynchronously(PlotMines.getInstance(), () -> { // Save the mines asynchronously (non-blocking)
            try {
                final Gson gson = new GsonBuilder().setPrettyPrinting().create();
                final File file = new File(PlotMines.getInstance().getDataFolder().getAbsolutePath() + "/data/minedata.json");

                if (!file.exists()) {
                    file.getParentFile().mkdir(); // Creates the /data/
                    file.createNewFile(); // Creates the /data/minedata.json
                }

                final Set<Mine> mines = MineManager.getMines();
                if (mines != null) {
                    final Writer writer = new FileWriter(file, false);
                    gson.toJson(mines, writer);
                    writer.flush();
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Load all mines from the JSON file
     */
    public CompletableFuture<Void> load() {
        return CompletableFuture.runAsync(() -> {
            try {
                final Gson gson = new Gson();
                final File file = new File(PlotMines.getInstance().getDataFolder().getAbsolutePath() + "/data/minedata.json");

                if (!file.exists()) {
                    file.getParentFile().mkdir(); // Creates the /data/
                    file.createNewFile(); // Creates the /data/minedata.json
                }

                if (file.exists()) {
                    final Reader reader = new FileReader(file);
                    final TypeToken<Set<Mine>> typeToken = new TypeToken<>() {
                    };
                    final Set<Mine> mineSet = gson.fromJson(reader, typeToken.getType());
                    if (mineSet != null) {
                        MineManager.setMines(mineSet);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static JsonMineStorage get() {
        return instance;
    }
}
