package fr.brindy.globalpasswd.utils;

import net.kyori.adventure.key.Key;

public enum Keys {
    CONFIRM("confirm"),
    CANCEL("cancel");

    private final Key key;

    Keys(String key) {
        this.key = Key.key(Constants.PLUGIN_KEY + ":" + key);
    }

    @Override
    public String toString() {
        return this.key.toString();
    }

    public Key toKey() {
        return this.key;
    }
}
