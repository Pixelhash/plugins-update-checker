package de.codehat.pluginupdatechecker;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.function.Consumer;

public class UpdateChecker {

    public static class Builder {

        private JavaPlugin plugin;
        private String url;
        private String pluginId;
        private String currentVersion;
        private Consumer<String> onLatestVersion;
        private Consumer<String> onNewVersion;
        private Consumer<String> onError;

        public Builder(JavaPlugin plugin) {
            if (plugin == null) throw new IllegalStateException("Plugin is null");

            this.plugin = plugin;
        }

        public Builder setUrl(String url) {
            this.url = url;

            return this;
        }

        public Builder setPluginId(String pluginId) {
            this.pluginId = pluginId;

            return this;
        }

        public Builder setCurrentVersion(String currentVersion) {
            this.currentVersion = currentVersion;

            return this;
        }

        public Builder onLatestVersion(Consumer<String> onLatestVersion) {
            this.onLatestVersion = onLatestVersion;

            return this;
        }

        public Builder onNewVersion(Consumer<String> onNewVersion) {
            this.onNewVersion = onNewVersion;

            return this;
        }

        public Builder onError(Consumer<String> onError) {
            this.onError = onError;

            return this;
        }

        public UpdateChecker build() {
            UpdateChecker updateChecker = new UpdateChecker();
            updateChecker.plugin = this.plugin;
            updateChecker.url = this.url;
            updateChecker.pluginId = this.pluginId;
            updateChecker.currentVersion = this.currentVersion;
            updateChecker.onLatestVersion = this.onLatestVersion;
            updateChecker.onNewVersion = this.onNewVersion;
            updateChecker.onError = this.onError;

            return updateChecker;
        }

    }

    JavaPlugin plugin;
    String url;
    String pluginId;
    String currentVersion;
    Consumer<String> onLatestVersion;
    Consumer<String> onNewVersion;
    Consumer<String> onError;

    private boolean isChecking = false;

    private UpdateChecker() {
        // Private constructor for Builder
    }

    public boolean check() {
        if (this.isChecking) return false;
        this.isChecking = true;

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            String result = null;

            try {
                result = HttpRequest.get(url + "/" + pluginId);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (result == null || result.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    onError.accept(null);
                });
                return;
            }

            String version = null;
            try {
                version = getVersionFromResponse(result);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (version == null || version.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    onError.accept(null);
                });
                return;
            }

            if (version.equals(currentVersion)) {
                String finalVersion = version;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    onLatestVersion.accept(finalVersion);
                });
            } else {
                String finalVersion1 = version;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    onNewVersion.accept(finalVersion1);
                });
            }

        });

        this.isChecking = true;
        return true;
    }

    private String getVersionFromResponse(String response) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject responseObj = (JSONObject) parser.parse(response);
        return (String) ((JSONObject) responseObj.get("data")).get("version");
    }

    public boolean isChecking() {
        return isChecking;
    }
}
