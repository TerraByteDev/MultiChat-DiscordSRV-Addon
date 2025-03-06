package com.loohp.multichatdiscordsrvaddon.objectholders;

import javax.annotation.Nullable;

public class MCField {
    protected final String name;
    protected final String value;
    protected final boolean inline;

    public MCField(String name, String value, boolean inline, boolean checked) {
        if (checked) {
            if (name == null || value == null) {
                throw new IllegalArgumentException("Both Name and Value must be set!");
            }

            if (name.length() > 256) {
                throw new IllegalArgumentException("Name cannot be longer than 256 characters.");
            }

            if (value.length() > 1024) {
                throw new IllegalArgumentException("Value cannot be longer than 1024 characters.");
            }

            name = name.trim();
            value = value.trim();
            if (name.isEmpty()) {
                this.name = "\u200e";
            } else {
                this.name = name;
            }

            if (value.isEmpty()) {
                this.value = "\u200e";
            } else {
                this.value = value;
            }
        } else {
            this.name = name;
            this.value = value;
        }

        this.inline = inline;
    }

    public MCField(String name, String value, boolean inline) {
        this(name, value, inline, true);
    }

    @Nullable
    public String getName() {
        return this.name;
    }

    @Nullable
    public String getValue() {
        return this.value;
    }

    public boolean isInline() {
        return this.inline;
    }
}
