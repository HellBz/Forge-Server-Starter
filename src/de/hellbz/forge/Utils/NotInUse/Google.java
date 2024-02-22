package de.hellbz.forge.Utils.NotInUse;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Google {

    static void LogToGForm() {

        System.out.println(
                Stream.of(requireNonNull(new File(".").listFiles()))
                        .filter(file -> !file.isDirectory())
                        .map(File::getName)
                        .collect(Collectors.toSet())
        );

        Pattern pattern = Pattern.compile("(.*)-([.0-9]{1,10})-([.0-9]{1,10}).txt");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("."))) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {

                    System.out.println(path.getFileName().toString());

                    Matcher matcher = pattern.matcher(path.getFileName().toString());

                    if (matcher.find()) {
                        // ...then you can use group() methods.
                        System.out.println(matcher.group(0)); // whole matched expression
                        System.out.println(matcher.group(1)); // first expression from round brackets (Testing)
                        System.out.println(matcher.group(2)); // second one (123)
                        System.out.println(matcher.group(3)); // third one (Testing)
                        break;
                    }

                }
            }
        } catch (IOException e) {
            // throw new RuntimeException(e);
        }


        URL url;
        try {

            String decodedUrl = new String(Base64.getDecoder().decode("aHR0cHM6Ly9kb2NzLmdvb2dsZS5jb20vZm9ybXMvZC9lLzFGQUlwUUxTZEVUenpfQVptZ2gwUkt1dHJJOXFXNFFSSTljMndISGxqQVNXdXJvemlXUEtOVlN3L2Zvcm1SZXNwb25zZQ=="));
            url = new URL(decodedUrl);

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        //https://docs.google.com/forms/d/e/1FAIpQLSdETzz_AZmgh0RKutrI9qW4QRI9c2wHHljASWuroziWPKNVSw/viewform?usp=pp_url&
        // entry.1419387411=128.1.1.2:23456&
        // entry.1665772952=All+The+Mods+7&
        // entry.844042064=1.5.1&
        // entry.438756549=1.18.2&
        // entry.166218453=2022-09-14&
        // entry.638475810=12:20
        HttpURLConnection http;
        try {
            http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("authority", "docs.google.com");
            http.setRequestProperty("origin", "https://docs.google.com");
            http.setRequestProperty("referer", "https://docs.google.com/forms/d/e/1FAIpQLSdETzz_AZmgh0RKutrI9qW4QRI9c2wHHljASWuroziWPKNVSw/viewform");
            http.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36");

            StringBuilder data = new StringBuilder();

            //OffsetDateTime now = OffsetDateTime.now( ZoneOffset.UTC );
            //System.out.println( "now.toString(): " + now );

            // https://docs.google.com/forms/d/e/1FAIpQLSdETzz_AZmgh0RKutrI9qW4QRI9c2wHHljASWuroziWPKNVSw/viewform?usp=pp_url&entry.1419387411=128.1.1.2:23456&entry.1665772952=All+The+Mods+7&entry.844042064=1.5.1&entry.438756549=1.18.2&entry.166218453=2022-09-14&entry.638475810=12:20&entry.1253727540=ModpackStats
            // https://docs.google.com/spreadsheets/d/185AijQIxZ64-ZE_URGKf4RlbIdWtfaooMsYNhNHV64o/edit?resourcekey#gid=1533227448

            data.append("entry.1419387411=128.1.1.2:23456");
            data.append("&entry.1665772952=All+The+Mods+8");
            data.append("&entry.844042064=1.5.2");
            data.append("&entry.438756549=1.18.2");
            // data.append("&entry.166218453=" + now.getYear() + "-" + String.format("%02d", now.getMonthValue() ) + "-" + String.format("%02d",  now.getDayOfMonth() ) );
            // data.append("&entry.638475810=" + String.format("%02d", now.getHour() ) + ":" + String.format("%02d", now.getMinute() )  );
            data.append("&entry.1253727540=ModpackStats");

            System.out.println("data.toString(): " + data);

            byte[] out = data.toString().getBytes(StandardCharsets.UTF_8);

            OutputStream stream = http.getOutputStream();
            stream.write(out);

            System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
            http.disconnect();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        System.exit(0);

    }
}
