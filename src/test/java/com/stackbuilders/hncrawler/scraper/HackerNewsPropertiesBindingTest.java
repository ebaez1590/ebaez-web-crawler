package com.stackbuilders.hncrawler.scraper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "hn.crawler.url=https://example.test/hn",
        "hn.crawler.user-agent=custom-agent/9.9",
        "hn.crawler.timeout-ms=1234",
        "hn.crawler.limit=15"
})
class HackerNewsPropertiesBindingTest {

    @Autowired
    private HackerNewsProperties properties;

    @Test
    void bindsCrawlerPropertiesFromConfiguration() {
        assertThat(properties.getUrl()).isEqualTo("https://example.test/hn");
        assertThat(properties.getUserAgent()).isEqualTo("custom-agent/9.9");
        assertThat(properties.getTimeoutMs()).isEqualTo(1234);
        assertThat(properties.getLimit()).isEqualTo(15);
    }
}
