package com.loohp.multichatdiscordsrvaddon.discordsrv.utils;

import com.loohp.multichatdiscordsrvaddon.objectholders.PreviewableImageContainer;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageSticker;

import java.util.Collections;

public class DiscordSRVImageUtils {
    public static PreviewableImageContainer fromSticker(MessageSticker sticker) {
        return new PreviewableImageContainer(sticker.getName(), sticker.getIconUrl(), Collections.emptyList(), sticker.getFormatType().getExtension(), null);
    }

    public static PreviewableImageContainer fromAttachment(Message.Attachment attachment) {
        return new PreviewableImageContainer(attachment.getFileName(), attachment.getUrl(), Collections.singletonList(attachment.getProxyUrl()), attachment.getContentType(), () -> attachment.retrieveInputStream());
    }
}
