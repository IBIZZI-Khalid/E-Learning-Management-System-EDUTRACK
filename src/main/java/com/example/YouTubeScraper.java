package com.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;

public class YouTubeScraper {
    public static void main(String[] args) {

        // Configure ChromeOptions to specify arguments
        ChromeOptions options = new ChromeOptions();

        // hna kan spicifyiw fin kayn software dyal chrome (only if its not our default
        // browser)
        options.setBinary("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");

        // options.addArguments("--headless"); // Run in headless mode
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");

        // Specifying the path to ChromeDriver exe
        System.setProperty("webdriver.chrome.driver",
                "C:\\Program Files\\chromedriver-win64\\chromedriver.exe");
        WebDriver driver = new ChromeDriver(options);
        // YouTubeScraper scraper = new YouTubeScraper();
        // hna ghir ghantestiw bhad search term :
        List<String> videoLinks = scrapeYouTubeLinks(driver, "java programming");

        // Print the scraped video links
        for (String link : videoLinks) {
            System.out.println(link);
        }
        try {
            writetofile(videoLinks);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // driver.quit();
    }

    // creating a txt file to save the video links in
    public static File createFile() {
        try {
            File myFile = new File("Videos.txt");
            if (myFile.createNewFile()) {
                System.out.println("File created: " + myFile.getName());
            } else {
                System.out.println("File already exists.");
            }
            return myFile;
        } catch (IOException e) {
            System.err.println("Error creating the file.");
            e.printStackTrace();
            return null;
        }
    }

    public static void writetofile(List<String> videoLinks) {
        File myFile = createFile();
        try (FileWriter writer = new FileWriter(myFile)) {
            for (String link : videoLinks) {
                writer.write(link + "\n");
            }
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.err.println("Error writing to the file.");
            e.printStackTrace();
        }
    }

    public static List<String> scrapeYouTubeLinks(WebDriver driver, String searchTerm) {
        List<String> videoLinks = new ArrayList<>();

        try {
            String searchUrl = "https://www.youtube.com/results?search_query=" + searchTerm.replace(" ", "+");
            driver.get(searchUrl);

            // Wait for content to load
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            // Get page source and parse with Jsoup
            String pageSource = driver.getPageSource();
            Document doc = Jsoup.parse(pageSource);
            Elements vidElements = doc.select("a#video-title");

            // Select the first 5 video elements
            // Math.min function insures that we dont get more than 5
            for (int i = 0; i < Math.min(5, vidElements.size()); i++) {
                Element element = vidElements.get(i);
                String videoUrl = "https://www.youtube.com" + element.attr("href");
                videoLinks.add(videoUrl);
            }

        } catch (Exception e) {
            System.out.println("Error occurred while scraping YouTube: " + e.getMessage());
            e.printStackTrace();

        } finally {
            // Close the WebDriver
            driver.quit();
        }

        return videoLinks;
    }
}
