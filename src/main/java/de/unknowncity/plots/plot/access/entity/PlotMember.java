package de.unknowncity.plots.plot.access.entity;

import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import de.unknowncity.plots.plot.access.type.PlotMemberRole;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.NodePath;

import java.util.List;
import java.util.UUID;

public class PlotMember extends PlotPlayer {
    private PlotMemberRole role;

    public PlotMember(UUID uuid, String name, PlotMemberRole role) {
        super(uuid, name);
        this.role = role;
    }

    public PlotMemberRole role() {
        return role;
    }

    public void role(PlotMemberRole role) {
        this.role = role;
    }

    public TagResolver[] tagResolversWithRole(Player player, PaperMessenger messenger) {
        var rolePlaceholder = Placeholder.component("role", messenger.component(player, NodePath.path("member-role", "name", role.name())));
        return new TagResolver[]{
                TagResolver.resolver(List.of(rolePlaceholder, TagResolver.resolver(super.tagResolvers())))
        };
    }
}
