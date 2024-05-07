package com.lukemango.plotmines.config.impl;

import com.lukemango.plotmines.PlotMines;
import com.lukemango.plotmines.util.AbstractConfig;

public class Messages extends AbstractConfig {

    public Messages(String path) {
        super(path, "messages.yml");
    }

    public void setExampleId(int id) {
        getYamlConfiguration().set("example-id",  id);
        save();
    }

    // Player messages
    public String getPlayerCreatedMine() { return this.getMessage("player-messages.created-mine"); }
    public String getPlayerDeletedMine() { return this.getMessage("player-messages.deleted-mine"); }
    public String getPlayerDeletedByAdmin() { return this.getMessage("player-messages.deleted-by-admin"); }
    public String getPlayerResetMine() { return this.getMessage("player-messages.reset-mine"); }
    public String getPlayerMineResetTeleportedOut() { return this.getMessage("player-messages.mine-reset-teleported-out"); }
    public String getPlayerReceivedMine() { return this.getMessage("player-messages.received-mine"); }
    public String getPlayerReceivedFullInventory() { return this.getMessage("player-messages.received-full-inventory"); }
    public String getPlayerInvalidLocation() { return this.getMessage("player-messages.invalid-location"); }
    public String getPlayerMineAlreadyThere() { return this.getMessage("player-messages.mine-already-there"); }
    public String getPlayerClickAgainToConfirm() { return this.getMessage("player-messages.click-again-to-confirm"); }
    public String getPlayerRequestTimedOut() { return this.getMessage("player-messages.request-timed-out"); }
    public String getPlayerAlreadyHaveRequest() { return this.getMessage("player-messages.already-have-request"); }
    public String getPlayerDisplayNameChangeInitiated() { return this.getMessage("player-messages.display-name-change-initiated"); }
    public String getPlayerDisplayNameChanged() { return this.getMessage("player-messages.display-name-changed"); }
    public String getPlayerDisplayNameTooLong() { return this.getMessage("player-messages.display-name-too-long"); }
    public String getPlayerPlotCleared() { return this.getMessage("player-messages.plot-cleared"); }
    public String getPlayerPlotClearedFullInventory() { return this.getMessage("player-messages.plot-cleared-full-inventory"); }

    // Admin messages
    public String getAdminMineNotFound() { return this.getMessage("admin-messages.mine-not-found"); }
    public String getAdminMineList() { return this.getMessage("admin-messages.mine-list"); }
    public String getAdminMineListEmpty() { return this.getMessage("admin-messages.mine-list-empty"); }
    public String getAdminMineListEntry() { return this.getMessage("admin-messages.mine-list-entry"); }
    public String getAdminMineGiven() { return this.getMessage("admin-messages.mine-given"); }
    public String getAdminGivenFullInventory() { return this.getMessage("admin-messages.full-inventory"); }
    public String getAdminMaterialConfigError() { return this.getMessage("admin-messages.material-config-error"); }
    public String getAdminErrorOccurred() { return this.getMessage("admin-messages.error-occurred"); }
    public String getAdminReloaded() { return this.getMessage("admin-messages.reloaded"); }
    public String getAdminDeletedMine() { return this.getMessage("admin-messages.deleted-mine"); }

    /**
     * Get a message from the messages.yml file with error handling
     * @param path The path to the message
     * @return The message
     */
    private String getMessage(String path) {
        String message = getYamlConfiguration().getString(path);
        if (message == null) {
            PlotMines.getInstance().getLogger().warning("Message " + path + " not found or configured incorrectly.");
            return "<red>Message not found or configured incorrectly. Please contact an administrator.";
        }
        return getYamlConfiguration().getString(path);
    }
}
