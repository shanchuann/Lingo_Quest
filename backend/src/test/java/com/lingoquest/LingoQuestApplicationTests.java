package com.lingoquest;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/** Full-context smoke test — disabled by default because it requires a
 *  running MySQL + Redis + DeepSeek API key. Run it locally as an integration
 *  check by removing the @Disabled. */
@Disabled("requires MySQL + Redis; run manually for full integration check")
@SpringBootTest
class LingoQuestApplicationTests {
    @Test
    void contextLoads() {}
}
