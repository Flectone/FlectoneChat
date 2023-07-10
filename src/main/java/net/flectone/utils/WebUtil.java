package net.flectone.utils;

import net.flectone.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class WebUtil {

    public static String getFirstLineFromURL(String url) throws IOException {
        URL urlObj = new URL(url);
        BufferedReader reader = new BufferedReader(new InputStreamReader(urlObj.openStream()));
        String firstLine = reader.readLine();
        reader.close();
        return firstLine;
    }

    public static void checkNewerVersion(){
        String url = "https://flectone.net/flectonechat/last.version";

        try {
            String lastVersion = getFirstLineFromURL(url);
            String currentVersion = Main.getInstance().getDescription().getVersion();
            boolean isOld = !currentVersion.equals(lastVersion);

            if(isOld) Main.warning("⚠ There is a newer version of plugin " + lastVersion + " than your " + currentVersion);

        } catch (IOException e){
            Main.warning("⚠ Failed to verify plugin version");
        }
    }
}
