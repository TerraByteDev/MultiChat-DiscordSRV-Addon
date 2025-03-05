package com.loohp.multichatdiscordsrvaddon.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class InternalServerChatEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled;
    private String formatted;
    private String plainText;
    private Player emitter;

    public InternalServerChatEvent(String formatted, String plainText, Player emitter, boolean async) {
        super(async);

        this.formatted = formatted;
        this.plainText = plainText;
        this.emitter = emitter;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
