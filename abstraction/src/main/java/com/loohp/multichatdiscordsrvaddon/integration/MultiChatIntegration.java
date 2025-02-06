package com.loohp.multichatdiscordsrvaddon.integration;

public interface MultiChatIntegration {

    String getPluginName();

    boolean shouldEnable();

    void enable();

}
