package io.github.funcfoo.id;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = JakeIdTestApplication.class)
public class JakeIdAutoConfigurationTest {
    private static final Logger log = LoggerFactory.getLogger(JakeIdAutoConfigurationTest.class);

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    JakeId jakeId;

    @Test
    public void testId() {
        long startAt = System.currentTimeMillis();
        long lastId = 0;
        for(int i = 0; i < 512000; i++) {
            long id = jakeId.nextId();
            assertTrue(id > lastId);
        }
        log.info("512000 | " + (System.currentTimeMillis() - startAt));
    }
}