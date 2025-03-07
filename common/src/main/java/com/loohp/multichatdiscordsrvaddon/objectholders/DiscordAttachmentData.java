package com.loohp.multichatdiscordsrvaddon.objectholders;

import com.loohp.multichatdiscordsrvaddon.listeners.discordsrv.InboundToGameEvents;
import com.loohp.multichatdiscordsrvaddon.wrappers.GraphicsToPacketMapWrapper;
import lombok.Getter;

import java.util.UUID;

public static class DiscordAttachmentData {

    @Getter
    private final String fileName;
    @Getter
    private final String url;
    @Getter
    private final GraphicsToPacketMapWrapper imageMap;
    private final UUID uuid;
    private final boolean isVideo;

    public DiscordAttachmentData(String fileName, String url, GraphicsToPacketMapWrapper imageMap, boolean isVideo) {
        this.fileName = fileName;
        this.url = url;
        this.imageMap = imageMap;
        this.uuid = UUID.randomUUID();
        this.isVideo = isVideo;
    }

    public DiscordAttachmentData(String fileName, String url) {
        this(fileName, url, null, false);
    }

    public boolean isImage() {
        return imageMap != null && !isVideo;
    }

    public boolean isVideo() {
        return imageMap != null && isVideo;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public int hashCode() {
        return 17 * uuid.hashCode();
    }

    public boolean equals(Object object) {
        if (object instanceof InboundToGameEvents.DiscordAttachmentData) {
            return ((InboundToGameEvents.DiscordAttachmentData) object).uuid.equals(this.uuid);
        }
        return false;
    }

}
