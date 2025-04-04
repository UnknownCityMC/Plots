package de.unknowncity.plots.util;

import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class SkullHelper {

    public static ItemStack getSkull(String textures) {
        var head = new ItemStack(Material.PLAYER_HEAD);

        head.editMeta(SkullMeta.class, skullMeta -> {
            var playerProfile = Bukkit.createProfile( UUID.randomUUID());
            playerProfile.setProperty(new ProfileProperty("textures", textures));

            skullMeta.setPlayerProfile(playerProfile);
        });
        return head;
    }
}