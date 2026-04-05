package com.a114193.prefixmod.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {

    private static final Path CONFIG_DIR  = Path.of("config/prefixmod");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");

    private boolean tabEnabled      = true;
    private String  tabTitle        = "&6&lMy Server";
    private String  tabSubtitle     = "&7Welcome to the server!";
    private String  tabFooterFormat = "&7Ping: &a{ping}ms &r| &7Online: &a{online}&7/&a{max} &r| &7Session: &a{session}";
    private boolean chatEnabled     = true;
    private String  chatFormat      = "{prefix}&f{name}&r: {msg}";

    public void load() {
        try {
            Files.createDirectories(CONFIG_DIR);
            if (!Files.exists(CONFIG_FILE)) {
                save();
                return;
            }
            try (Reader r = Files.newBufferedReader(CONFIG_FILE)) {
                JsonObject o = JsonParser.parseReader(r).getAsJsonObject();
                tabEnabled      = bool(o, "tabEnabled",      tabEnabled);
                tabTitle        = str(o, "tabTitle",         tabTitle);
                tabSubtitle     = str(o, "tabSubtitle",      tabSubtitle);
                tabFooterFormat = str(o, "tabFooterFormat",  tabFooterFormat);
                chatEnabled     = bool(o, "chatEnabled",     chatEnabled);
                chatFormat      = str(o, "chatFormat",       chatFormat);
            }
        } catch (IOException e) {
            LoggerFactory.getLogger("prefixmod").error("Failed to load config.json", e);
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            JsonObject o = new JsonObject();
            o.addProperty("tabEnabled",      tabEnabled);
            o.addProperty("tabTitle",        tabTitle);
            o.addProperty("tabSubtitle",     tabSubtitle);
            o.addProperty("tabFooterFormat", tabFooterFormat);
            o.addProperty("chatEnabled",     chatEnabled);
            o.addProperty("chatFormat",      chatFormat);
            try (Writer w = Files.newBufferedWriter(CONFIG_FILE)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(o, w);
            }
        } catch (IOException e) {
            LoggerFactory.getLogger("prefixmod").error("Failed to save config.json", e);
        }
    }

    // ---- helpers --------------------------------------------------------

    private boolean bool(JsonObject o, String key, boolean def) {
        return o.has(key) ? o.get(key).getAsBoolean() : def;
    }

    private String str(JsonObject o, String key, String def) {
        return o.has(key) ? o.get(key).getAsString() : def;
    }

    // ---- getters / setters ----------------------------------------------

    public boolean isTabEnabled()  { return tabEnabled; }
    public void setTabEnabled(boolean v)  { tabEnabled = v;  save(); }

    public String getTabTitle()    { return tabTitle; }
    public void setTabTitle(String v)     { tabTitle = v;    save(); }

    public String getTabSubtitle() { return tabSubtitle; }
    public void setTabSubtitle(String v)  { tabSubtitle = v; save(); }

    public String getTabFooterFormat() { return tabFooterFormat; }

    public boolean isChatEnabled() { return chatEnabled; }
    public void setChatEnabled(boolean v) { chatEnabled = v; save(); }

    public String getChatFormat()  { return chatFormat; }
}
