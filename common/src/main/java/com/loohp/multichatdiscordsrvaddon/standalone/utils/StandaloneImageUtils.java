package com.loohp.multichatdiscordsrvaddon.standalone.utils;

import com.loohp.multichatdiscordsrvaddon.objectholders.PreviewableImageContainer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.sticker.Sticker;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class StandaloneImageUtils {

    public static PreviewableImageContainer fromSticker(Sticker sticker) {
        return new PreviewableImageContainer(sticker.getName(), sticker.getIconUrl(), Collections.emptyList(), sticker.getFormatType().getExtension(), null);
    }

    public static PreviewableImageContainer fromAttachment(Message.Attachment attachment) {
        return new PreviewableImageContainer(attachment.getFileName(), attachment.getUrl(), Collections.singletonList(attachment.getProxyUrl()), attachment.getContentType(), () -> retrieveInputStream(attachment.getProxyUrl()));
    }

    private static CompletableFuture<InputStream> retrieveInputStream(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                return connection.getInputStream();
            } catch (Exception e) {
                throw new RuntimeException("Failed to retrieve InputStream from URL: " + url, e);
            }
        });
    }
}
