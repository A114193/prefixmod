package com.a114193.prefixmod.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PrefixStorage {

    private static final Path CONFIG_DIR    = Path.of("config/prefixmod");
    private static final Path PREFIXES_FILE = CONFIG_DIR.resolve("prefixes.json");

    private final Map<String, String> prefixes = new HashMap<>();

    public void load() {
        prefixes.clear();
        try {
            Files.createDirectories(CONFIG_DIR);
            if (!Files.exists(PREFIXES_FILE)) {
                save();
                return;
            }
            try (Reader r = Files.newBufferedReader(PREFIXES_FILE)) {
                JsonObject o = JsonParser.parseReader(r).getAsJsonObject();
                for (Map.Entry<String, JsonElement> e : o.entrySet()) {
                    prefixes.put(e.getKey(), e.getValue().getAsString());
                }
            }
        } catch (IOException e) {
            LoggerFactory.getLogger("prefixmod").error("Failed to load prefixes.json", e);
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            JsonObject o = new JsonObject();
            prefixes.forEach(o::addProperty);
            try (Writer w = Files.newBufferedWriter(PREFIXES_FILE)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(o, w);
            }
        } catch (IOException e) {
            LoggerFactory.getLogger("prefixmod").error("Failed to save prefixes.json", e);
        }
    }

    public String getPrefix(String nick) {
        return prefixes.getOrDefault(nick, "");
    }

    public void setPrefix(String nick, String prefix) {
        prefixes.put(nick, prefix);
        save();
    }

    public void clearPrefix(String nick) {
        prefixes.remove(nick);
        save();
    }

    public Map<String, String> getAll() {
        return Collections.unmodifiableMap(prefixes);
    }
}
