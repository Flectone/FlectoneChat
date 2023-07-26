package net.flectone.utils;

import net.flectone.Main;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WebUtil {

    public static String getFirstLineFromURL(String url) throws IOException {
        URL urlObj = new URL(url);
        BufferedReader reader = new BufferedReader(new InputStreamReader(urlObj.openStream()));
        String firstLine = reader.readLine();
        reader.close();
        return firstLine;
    }

    public static void checkNewerVersion(){
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

                        String lastVersion = (String) ((JSONObject) json.get(0)).get("version_number");
                        String currentVersion = Main.getInstance().getDescription().getVersion();
                        boolean isOld = !currentVersion.equals(lastVersion);

                        if(isOld) Main.warning("⚠ There is a newer version of plugin " + lastVersion + " than your " + currentVersion);

                    } catch (ParseException e) {
                        Main.warning("⚠ Failed to get latest plugin version");
                        e.printStackTrace();
                    }

                })
                .join();

    }
}
