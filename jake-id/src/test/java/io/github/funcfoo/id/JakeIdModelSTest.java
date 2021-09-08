package io.github.funcfoo.id;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JakeIdModelSTest {
    private static final Long INIT_TIME_MILLS = Instant.parse("2022-01-01T00:00:00.000Z").toEpochMilli();
    private static final Integer MACHINE_ID = JakeUtils.machineId(JakeIdTestUtils.splitIpv4("192.168.13.11"));
    private static final Integer SAME_MACHINE_ID = JakeUtils.machineId(JakeIdTestUtils.splitIpv4("192.168.45.11"));

    private static JakeId sameMillJakeId(long startTime, long currentTimeMillis, int machineId) {
        JakeIdBuilder jakeIdBuilder = JakeIdBuilder.create().modelS();
        return new JakeIdImpl(
                startTime, jakeIdBuilder.getBitLengthOfTime(),
                jakeIdBuilder.getBitLengthOfMachineId(), jakeIdBuilder.getBitLengthOfSequence(),
                jakeIdBuilder.getSequenceLifeCycle(), machineId
        ) {
            @Override
            public long currentTimeMillis() {
                return currentTimeMillis;
            }
        };
    }

    private static JakeId customMillJakeId(long startTime, AtomicLong currentTimeMillis, int machineId) {
        JakeIdBuilder jakeIdBuilder = JakeIdBuilder.create().modelS();
        return new JakeIdImpl(
                startTime, jakeIdBuilder.getBitLengthOfTime(),
                jakeIdBuilder.getBitLengthOfMachineId(), jakeIdBuilder.getBitLengthOfSequence(),
                jakeIdBuilder.getSequenceLifeCycle(), machineId
        ) {
            @Override
            public long currentTimeMillis() {
                return currentTimeMillis.get();
            }
        };
    }

    private static long id(long startTime, long currentTimeMillis, long machineId) {
        return ((currentTimeMillis - startTime) << 22)
                | (machineId << 9);
    }

    @Test
    void nextSameMilliId() {
        JakeId jakeId = sameMillJakeId(JakeUtils.defaultStartTime(), INIT_TIME_MILLS, MACHINE_ID);
        long firstId = id(JakeUtils.defaultStartTime(), INIT_TIME_MILLS, MACHINE_ID);
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
        final AtomicLong currentTimeMillis = new AtomicLong(INIT_TIME_MILLS);
        JakeId jakeId = customMillJakeId(JakeUtils.defaultStartTime(), currentTimeMillis, MACHINE_ID);
        long firstId = id(JakeUtils.defaultStartTime(), INIT_TIME_MILLS, MACHINE_ID);
        long nextId = id(JakeUtils.defaultStartTime(), INIT_TIME_MILLS + 1, MACHINE_ID);
        long maxSequence = 512;
        for(long l = 0; l < maxSequence*2; l++) {
            long id = jakeId.nextId();
            if(l < maxSequence) {
                assertEquals(firstId + l, id);
            }else {
                assertEquals( nextId + (l - maxSequence), id);
            }
            if(l % maxSequence == maxSequence-1) {
                long nextMills = currentTimeMillis.incrementAndGet();
                JakeIdTestUtils.log("nexIncreaseMilliId","l: " + l + "," + "nextMillis: " + nextMills);
            }
        }
    }

    @Test
    void timestampGreaterThan1000() {
        final AtomicLong currentTimeMillis = new AtomicLong(INIT_TIME_MILLS);
        JakeId jakeId = customMillJakeId(JakeUtils.defaultStartTime(), currentTimeMillis, MACHINE_ID);
        Thread thread = new Thread() {
            @Override
            public void run() {
                for(int i = 0; i < 2000; i++) {
                    currentTimeMillis.decrementAndGet();
                }
            }
        };
        thread.start();
        JakeIdException ex = null;
        try {
            while (currentTimeMillis.get() <= INIT_TIME_MILLS) {
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
        final AtomicLong currentTimeMillis = new AtomicLong(INIT_TIME_MILLS);
        JakeId jakeId = customMillJakeId(JakeUtils.defaultStartTime(), currentTimeMillis, MACHINE_ID);
        Thread thread = new Thread() {
            @Override
            public void run() {
                for(int i = 0; i < 999; i++) {
                    currentTimeMillis.decrementAndGet();
                }
                currentTimeMillis.set(currentTimeMillis.get() + 2000);
            }
        };
        thread.start();
        long lastId = 0;
        while (currentTimeMillis.get() <= INIT_TIME_MILLS) {
            long id = jakeId.nextId();
            assertTrue(id > lastId);
            lastId = id;
            JakeIdTestUtils.logId("timestampLessThan1000", id);
        }
    }

    @Test
    void sameMachineIdTest() {
        JakeId jakeId1 = sameMillJakeId(JakeUtils.defaultStartTime(), INIT_TIME_MILLS, MACHINE_ID);
        JakeId jakeId2 = sameMillJakeId(JakeUtils.defaultStartTime(), INIT_TIME_MILLS, SAME_MACHINE_ID);
        long l1 = jakeId1.nextId();
        long l2 = jakeId2.nextId();
        assertEquals(l1, l2);
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
            if(l == 0 || l == generatorIdCount) {
                JakeIdTestUtils.logId("productionTest", id);
            }
        }
        long millis = System.currentTimeMillis() - timestamp;
        JakeIdTestUtils.log("productionTest", millis);
        JakeIdTestUtils.log("productionTest", "per millis " + generatorIdCount / millis);
    }
}
