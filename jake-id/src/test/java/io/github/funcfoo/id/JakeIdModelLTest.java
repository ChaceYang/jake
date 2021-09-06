package io.github.funcfoo.id;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.*;

class JakeIdModelLTest {
    private static final Instant INSTANT = Instant.parse("2022-01-01T00:00:00.000Z");
    private static final Integer MACHINE_ID = JakeUtils.machineId(JakeIdTestUtils.splitIpv4("192.168.13.11"));

    private static JakeId sameMill() {
        long currentTimeMillis = INSTANT.toEpochMilli();
        JakeIdBuilder jakeIdBuilder = JakeIdBuilder.create().modelL();
        return new JakeIdImpl(
                JakeUtils.defaultStartTime(), jakeIdBuilder.getBitLengthOfTime(),
                jakeIdBuilder.getBitLengthOfMachineId(), jakeIdBuilder.getBitLengthOfSequence(),
                jakeIdBuilder.getSequenceLifeCycle(), MACHINE_ID
        ) {
            @Override
            public long currentTimeMillis() {
                return currentTimeMillis;
            }
        };
    }

    private static JakeId customMill(AtomicLong currentTimeMillis) {
        JakeIdBuilder jakeIdBuilder = JakeIdBuilder.create().modelL();
        return new JakeIdImpl(
                JakeUtils.defaultStartTime(), jakeIdBuilder.getBitLengthOfTime(),
                jakeIdBuilder.getBitLengthOfMachineId(), jakeIdBuilder.getBitLengthOfSequence(),
                jakeIdBuilder.getSequenceLifeCycle(), MACHINE_ID
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
        long firstId = ((INSTANT.toEpochMilli()/10 - JakeUtils.defaultStartTime()/10) << 25)
                | (MACHINE_ID << 9);
        for(long l = 0; l<512; l++) {
            long id = jakeId.nextId();
            if(l == 0 || l == 511) {
                JakeIdTestUtils.logId("nextSameMilliId", id);
            }
            assertEquals(firstId + l, id);
        }
    }

    @Test
    void nexIncreaseMilliId() {
        final AtomicLong currentTimeMillis = new AtomicLong(INSTANT.toEpochMilli());
        long firstId = ((INSTANT.toEpochMilli()/10 - JakeUtils.defaultStartTime()/10) << 25)
                | (MACHINE_ID << 9);
        long nextId = (((INSTANT.toEpochMilli() + 10) /10 - JakeUtils.defaultStartTime()/10) << 25)
                | (MACHINE_ID << 9);
        JakeId jakeId = customMill(currentTimeMillis);
        for(long l = 0; l < 1024; l++) {
            long id = jakeId.nextId();
            if(l % 512 == 0 || l % 512 == 511) {
                JakeIdTestUtils.logId("nexIncreaseMilliId", id);
            }
            if(l < 512) {
                assertEquals(firstId + l, id);
            }else {
                assertEquals(nextId + (l % 512), id);
            }
            if(l % 512 == 511) {
                long value = currentTimeMillis.get();
                JakeIdTestUtils.log("nexIncreaseMilliId","l: " + l + ","
                        + "nextMillis: " + currentTimeMillis.compareAndSet(value, value + 10));
            }
        }
    }

    @Test
    void timestampGreaterThanOneSecond() {
        final AtomicLong currentTimeMillis = new AtomicLong(INSTANT.toEpochMilli());
        JakeId jakeId = customMill(currentTimeMillis);
        Thread thread = new Thread() {
            @Override
            public void run() {
                for(int i = 0; i < 2000; i++) {
                    long l = currentTimeMillis.get();
                    currentTimeMillis.compareAndSet(l, l-10);
                    LockSupport.parkNanos(10000000);
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
    void timestampLessThanOneSecond() {
        final AtomicLong currentTimeMillis = new AtomicLong(1663412306944L);
        JakeId jakeId = customMill(currentTimeMillis);
        Thread thread = new Thread() {
            @Override
            public void run() {
                for(int i = 0; i < 2000; i++) {
                    long l = currentTimeMillis.get();
                    currentTimeMillis.compareAndSet(l, l+10);
                    LockSupport.parkNanos(10000000);
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
        JakeId jakeId = JakeIdBuilder.create().modelL().build();
        long lastId = 0;
        long generatorIdCount = 512000;
        long timestamp = System.currentTimeMillis();
        for(long l = 0; l <= generatorIdCount; l++) {
            long id = jakeId.nextId();
            assertTrue(id > lastId);
            lastId = id;
            if(l == 0 || l == 512000) {
                JakeIdTestUtils.logId("productionTest", id);
            }
        }
        long millis = System.currentTimeMillis() - timestamp;
        JakeIdTestUtils.log("productionTest", millis);
        JakeIdTestUtils.log("productionTest", generatorIdCount / millis);
    }
}
