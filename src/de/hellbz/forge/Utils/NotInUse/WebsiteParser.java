package de.hellbz.forge.Utils.NotInUse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class WebsiteParser {
    public static void main(String[] args) {
        try {
            // URL der Website
            String url = "https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.4.html";

            // HTML der Website herunterladen
            Document doc = Jsoup.connect(url).get();

            // Nach <table class="download-list"> suchen
            Element downloadTable = doc.select("table.download-list").first();
            if (downloadTable != null) {
                // Alle <a> Tags innerhalb des <table> durchgehen
                Elements downloadLinks = downloadTable.select("tbody tr");
                for (Element table : downloadLinks) {
                    // Den Text des <td> Tags mit der Klasse "download-version" ausgeben
                    String version = table.select("td.download-version").text();
                    System.out.println("Version: " + version);

                    // Den ersten <a> Tag im ersten <li> Element ausgeben
                    String firstLink = table.select("ul.download-links li:first-of-type a").attr("href");
                    System.out.println("Changelog: " + firstLink);

                    String installer = table.select("a.info-link").attr("href");
                    System.out.println("Installer: " + installer);

                    // Den zweiten <a> Tag im zweiten <li> Element ausgeben
                    //String secondLink = table.select("ul.download-links li:nth-of-type(2) a").attr("href");
                    //System.out.println("Zweiter Link: " + secondLink);
                }
            } else {
                System.out.println("Keine Download-Tabelle gefunden.");
            }

            // Nach <ul> mit der Klasse "section-content" suchen
            Element uls = doc.select("ul.section-content").first();
            if (uls != null) {

                // Alle <ul> Tags mit der Klasse "nav-collapsible" durchgehen
                Elements ulElements = uls.select("ul.nav-collapsible");
                for (Element ul : ulElements) {
                    // Alle <li> Tags innerhalb des aktuellen <ul> durchgehen
                    Elements liElements = ul.select("li");
                    for (Element li : liElements) {
                        // Den href-Wert des <a> Tags ausgeben
                        String hrefText = li.select("a").attr("href");
                        String version = li.select("a").text();
                        System.out.println("Version: " + version + " Href Text: " + hrefText);
                    }
                }

            } else {
                System.out.println("Keine Versionsliste gefunden.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}