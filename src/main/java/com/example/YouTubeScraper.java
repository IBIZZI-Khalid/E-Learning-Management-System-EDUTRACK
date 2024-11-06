package com.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;

public class YouTubeScraper {
    public static void main(String[] args) {

        // Configure ChromeOptions to specify the path to Chrome binary
        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");
        // options.addArguments("--headless"); // Run in headless mode
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");

        System.setProperty("webdriver.chrome.driver",
                "C:\\Program Files\\chromedriver-win64\\chromedriver.exe");
        WebDriver driver = new ChromeDriver(options);
        YouTubeScraper scraper = new YouTubeScraper();
        // hna ghir ghantestiw bhad search term :
        List<String> videoLinks = scraper.scrapeYouTubeLinks(driver, "java programming");

        // Print the scraped video links
        for (String link : videoLinks) {
            System.out.println(link);
        }
        // driver.quit();
    }

    public List<String> scrapeYouTubeLinks(WebDriver driver, String searchTerm) {
        List<String> videoLinks = new ArrayList<>();

        try {
            String searchUrl = "https://www.youtube.com/results?search_query=" + searchTerm.replace(" ", "+");
            driver.get(searchUrl);

            // Wait for content to load
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            // Get page source and parse with Jsoup
            String pageSource = driver.getPageSource();
            Document doc = Jsoup.parse(pageSource);

            // Select all video elements
            for (Element element : doc.select("a#video-title")) {
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
