package com.lukemango.plotmines.config;

import com.lukemango.plotmines.config.impl.Config;
import com.lukemango.plotmines.config.impl.ManageGuiConfig;
import com.lukemango.plotmines.config.impl.Messages;

public class ConfigManager {

    private static ConfigManager instance;

    private Config config;
    private Messages messages;
    private ManageGuiConfig manageGuiConfig;

    public ConfigManager() {
        instance = this;
        this.init();
    }

    private void init() {
        final String path = "/configs/";
        config = new Config(path);
        messages = new Messages(path);
        manageGuiConfig = new ManageGuiConfig(path);
    }

    public void reload() {
        config.reload();
        messages.reload();
        manageGuiConfig.reload();
    }

    public static ConfigManager get() {
        return instance;
    }

    public Config getConfig() {
        return config;
    }

    public Messages getMessages() {
        return messages;
    }

    public ManageGuiConfig getManageGuiConfig() {
        return manageGuiConfig;
    }

}
