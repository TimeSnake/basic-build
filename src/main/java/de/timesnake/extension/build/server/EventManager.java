package de.timesnake.extension.build.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.UserJoinEvent;
import de.timesnake.extension.build.chat.Plugin;
import de.timesnake.extension.build.main.ExBuild;
import de.timesnake.library.basic.util.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EventManager implements Listener {

    public EventManager() {
        Server.registerListener(this, ExBuild.getPlugin());
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        e.setYield(0);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        e.setYield(0);
    }

    @EventHandler
    public void onUserJoin(UserJoinEvent e) {
        e.getUser().sendPluginMessage(Plugin.BUILD, Component.text("Use ", ExTextColor.PERSONAL)
                .append(Component.text("/mw tp", ExTextColor.VALUE, TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "/mw tp "))
                        .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Click to copy"))))
                .append(Component.text(" to teleport to a world", ExTextColor.PERSONAL)));
    }

}
