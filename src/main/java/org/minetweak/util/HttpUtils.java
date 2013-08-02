package org.minetweak.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class HttpUtils {

    public static void downloadFile(String path, String url) {
        File file = new File(path);
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new RuntimeException("Unable to download " + url + "! Cannot create file " + file.getAbsolutePath());
                }
            }
            URL urlC = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(urlC.openStream());
            FileOutputStream fos = new FileOutputStream(file);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}