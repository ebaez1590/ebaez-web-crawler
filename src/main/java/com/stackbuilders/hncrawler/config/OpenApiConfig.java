package com.stackbuilders.hncrawler.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hackerNewsCrawlerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hacker News Web Crawler API")
                        .description("""
                                Scrapes the first 30 Hacker News entries, applies title-length filters,
                                and stores usage events (timestamp + filter applied).
                                """)
                        .version("0.0.1")
                        .contact(new Contact()
                                .name("ebaez-web-crawler")
                                .url("https://github.com/ebaez1590/ebaez-web-crawler")));
    }
}
