package com.nobidev.dotenv;

public class DotEnvEntry {

    protected final String key;
    protected final String value;

    public DotEnvEntry(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }
}

