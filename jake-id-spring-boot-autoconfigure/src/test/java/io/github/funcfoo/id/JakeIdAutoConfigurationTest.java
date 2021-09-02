package io.github.funcfoo.id;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JakeIdAutoConfigurationTest {
    private static final Logger log = LoggerFactory.getLogger(JakeIdAutoConfigurationTest.class);

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    JakeId jakeId;

    @Test
    void testId() {
        long startAt = System.currentTimeMillis();
        long lastId = 0;
        for(int i = 0; i < 512000; i++) {
            long id = jakeId.nextId();
            assertTrue(id > lastId);
        }
        log.info("512000 | " + (System.currentTimeMillis() - startAt));
    }
}