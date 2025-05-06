package com.thomas.minigame.games.tnttag;

import com.thomas.minigame.core.PlayerData;

public class TNTTagPlayer extends PlayerData {

    private boolean isTagged = false;

    public TNTTagPlayer(org.bukkit.entity.Player player) {
        super(player);
    }

    public boolean isTagged() {
        return isTagged;
    }

    public void setTagged(boolean tagged) {
        this.isTagged = tagged;
        getPlayer().getInventory().clear();
        if (tagged) {
            getPlayer().getInventory().setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.TNT));
            getPlayer().sendMessage("You are TAGGED! Hit someone to pass it!");
        } else {
            getPlayer().sendMessage("You are no longer tagged.");
        }
    }
}
