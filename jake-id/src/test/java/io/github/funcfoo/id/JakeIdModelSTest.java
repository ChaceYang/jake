package io.github.funcfoo.id;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.*;

class JakeIdModelSTest {

    private static JakeId sameMill() {
        long currentTimeMillis = Instant.parse("2022-01-01T00:00:00.000Z").toEpochMilli();
        JakeIdBuilder jakeIdBuilder = JakeIdBuilder.create().modelS();
        return new JakeIdImpl(
                JakeUtils.defaultStartTime(), jakeIdBuilder.getBitLengthOfTime(),
                jakeIdBuilder.getBitLengthOfMachineId(), jakeIdBuilder.getBitLengthOfSequence(),
                jakeIdBuilder.getSequenceLifeCycle(),JakeUtils.machineId(JakeIdTestUtils.splitIpv4("192.168.13.11"))
        ) {
            @Override
            public long currentTimeMillis() {
                return currentTimeMillis;
            }
        };
    }

    private static JakeId customMill(AtomicLong currentTimeMillis) {
        JakeIdBuilder jakeIdBuilder = JakeIdBuilder.create().modelS();
        return new JakeIdImpl(
                JakeUtils.defaultStartTime(), jakeIdBuilder.getBitLengthOfTime(),
                jakeIdBuilder.getBitLengthOfMachineId(), jakeIdBuilder.getBitLengthOfSequence(),
                jakeIdBuilder.getSequenceLifeCycle(),JakeUtils.machineId(JakeIdTestUtils.splitIpv4("192.168.13.11"))
        ) {
            @Override
            public long currentTimeMillis() {
                return currentTimeMillis.get();
            }
        };
    }

    @Test
    void nextSameMilliId() {
        JakeId jakeId = sameMill();
        for(long l = 0; l<512; l++) {
            long id = jakeId.nextId();
            if(l == 0 || l == 511) {
                JakeIdTestUtils.logId("nextSameMilliId", id);
            }
            assertEquals(1058897343284909568L + l, id);
        }
    }

    @Test
    void nexIncreaseMilliId() {
        final AtomicLong currentTimeMillis = new AtomicLong(1663412306944L);
        JakeId jakeId = customMill(currentTimeMillis);
        for(long l = 0; l < 1024; l++) {
            long id = jakeId.nextId();
            if(l % 512 == 0 || l % 512 == 511) {
                JakeIdTestUtils.logId("nexIncreaseMilliId", id);
            }
            if(l < 512) {
                assertEquals(1152921504608556544L + l, id);
            }else {
                assertEquals(1152921504612750848L + (l % 512), id);
            }
            if(l % 512 == 511) {
                JakeIdTestUtils.log("nexIncreaseMilliId","l: " + l + "," + "nextMillis: " + currentTimeMillis.incrementAndGet());
            }
        }
    }

    @Test
    void timestampGreaterThan1000() {
        final AtomicLong currentTimeMillis = new AtomicLong(1663412306944L);
        JakeId jakeId = customMill(currentTimeMillis);
        Thread thread = new Thread() {
            @Override
            public void run() {
                for(int i = 0; i < 2000; i++) {
                    currentTimeMillis.decrementAndGet();
                    LockSupport.parkNanos(1000000);
                }
            }
        };
        thread.start();
        JakeIdException ex = null;
        try {
            for(long l = 0; l < 10; l++) {
                long id = jakeId.nextId();
                JakeIdTestUtils.logId("timestampGreaterThan1000", id);
            }
        } catch (JakeIdException e) {
            ex = e;
        }
        assertNotNull(ex);
    }

    @Test
    void timestampLessThan1000() {
        final AtomicLong currentTimeMillis = new AtomicLong(1663412306944L);
        JakeId jakeId = customMill(currentTimeMillis);
        Thread thread = new Thread() {
            @Override
            public void run() {
                for(int i = 0; i < 2000; i++) {
                    currentTimeMillis.incrementAndGet();
                    LockSupport.parkNanos(1000000);
                }
            }
        };
        thread.start();
        long lastId = 0;
        for(long l = 0; l < 10; l++) {
            long id = jakeId.nextId();
            assertTrue(id > lastId);
            lastId = id;
            JakeIdTestUtils.logId("timestampLessThan1000", id);
            if(l == 0) {
                currentTimeMillis.set(currentTimeMillis.get() - 1000);
            }
        }
    }

    @Test
    void productionTest() {
        JakeId jakeId = JakeIdBuilder.create().modelS().build();
        long lastId = 0;
        long generatorIdCount = 5120000;
        long timestamp = System.currentTimeMillis();
        for(long l = 0; l <= generatorIdCount; l++) {
            long id = jakeId.nextId();
            assertTrue(id > lastId);
            lastId = id;
            if(l == 0 || l == 5120000) {
                JakeIdTestUtils.logId("productionTest", id);
            }
        }
        long millis = System.currentTimeMillis() - timestamp;
        JakeIdTestUtils.log("productionTest", millis);
        JakeIdTestUtils.log("productionTest", generatorIdCount / millis);
    }
}
