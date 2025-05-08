package com.thomas.minigame.games.tnttag;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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

        if (tagged) {
            getPlayer().getInventory().setHelmet(new ItemStack(Material.TNT));
            getPlayer().sendMessage("Voce esta com a Batata Quente!");
        } else {
            getPlayer().getInventory().setHelmet(null); // Remove TNT helmet
        }
    }

}
