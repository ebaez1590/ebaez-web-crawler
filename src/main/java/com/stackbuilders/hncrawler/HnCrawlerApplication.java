package com.stackbuilders.hncrawler;

import com.stackbuilders.hncrawler.scraper.HackerNewsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(HackerNewsProperties.class)
public class HnCrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HnCrawlerApplication.class, args);
    }
}
