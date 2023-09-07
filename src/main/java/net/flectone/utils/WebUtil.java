package net.flectone.utils;

import net.flectone.Main;
import net.flectone.managers.FileManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WebUtil {

    public static void checkNewerVersion() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.modrinth.com/v2/project/flectonechat/version"))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(s -> {
                    JSONParser parser = new JSONParser();
                    try {
                        JSONArray json = (JSONArray) parser.parse(s);

                        String currentVersion = Main.getInstance().getDescription().getVersion();
                        String lastVersion = (String) ((JSONObject) json.get(0)).get("version_number");

                        if (FileManager.compareVersions(currentVersion, lastVersion) == -1) {
                            Main.warning("⚠ Upgrade your " + currentVersion + " version of plugin to " + lastVersion);
                            Main.warning("⚠ Url: https://modrinth.com/plugin/flectonechat/version/" + lastVersion);
                        }

                    } catch (ParseException e) {
                        Main.warning("⚠ Failed to get latest plugin version");
                        e.printStackTrace();
                    }

                });

    }
}
