package com.nammametro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class NammaMetroApplication {

    private static final Logger log = LoggerFactory.getLogger(NammaMetroApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(NammaMetroApplication.class, args);
        log.info("==========================================================================");
        log.info("   NAMMA METRO (ನಮ್ಮ ಮೆಟ್ರೋ) TICKETING SYSTEM INITIALIZED SUCCESSFULLY    ");
        log.info("   PORT: http://localhost:8080                                            ");
        log.info("   H2 CONSOLE: http://localhost:8080/h2-console                           ");
        log.info("==========================================================================");
    }
}
