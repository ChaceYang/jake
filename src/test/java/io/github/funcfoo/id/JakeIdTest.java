package io.github.funcfoo.id;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.*;

class JakeIdTest {

    private static int[] splitIpv4(String ipv4) {
        String[] ipv4Address = ipv4.split("\\.");
        return new int[] {
                Integer.parseInt(ipv4Address[0]),
                Integer.parseInt(ipv4Address[1]),
                Integer.parseInt(ipv4Address[2]),
                Integer.parseInt(ipv4Address[3]),
        };
    }

    private static void logId(long id) {
        String binaryString = Long.toBinaryString(id);
        int splitMachineIdIndex = binaryString.length() - 9 - 13;
        int splitSequenceIndex = binaryString.length() - 9;
        System.out.println(id + ":"
                + binaryString.substring(0,splitMachineIdIndex)
                + "-"
                + binaryString.substring(splitMachineIdIndex, splitSequenceIndex)
                + "-"
                + binaryString.substring(splitSequenceIndex));
    }

    @Test
    void machineID() {
        int machineID1 = JakeId.machineId(splitIpv4("192.168.33.0"));
        assertEquals(256, machineID1);
        int machineID2 = JakeId.machineId(splitIpv4("192.168.33.1"));
        assertEquals(257, machineID2);
        int machineID3 = JakeId.machineId(splitIpv4("192.168.0.1"));
        assertEquals(1, machineID3);
        int machineID5 = JakeId.machineId(splitIpv4("192.168.31.255"));
        assertEquals(8191, machineID5);
    }

    @Test
    void nextSameMilliId() {
        long currentTimeMillis = Instant.parse("2022-01-01T00:00:00.000Z").toEpochMilli();
        JakeId jIdGenerator = new JakeId() {
            @Override
            public long currentTimeMillis() {
                return currentTimeMillis;
            }
        };
        for(long l = 0; l<512; l++) {
            long id = jIdGenerator.nextId();
            if(l == 0 || l == 511) System.out.println(id);
            assertEquals(1058897343284909568L + l, id);
        }
    }

    @Test
    void nexIncreaseMilliId() {
        final AtomicLong currentTimeMillis = new AtomicLong(1663412306944L);
        JakeId jIdGenerator = new JakeId() {
            @Override
            public long currentTimeMillis() {
                return currentTimeMillis.get();
            }
        };
        for(long l = 0; l < 1024; l++) {
            long id = jIdGenerator.nextId();
            if(l % 512 == 0 || l % 512 == 511) {
                  logId(id);
            }
            if(l < 512) {
                assertEquals(1152921504608556544L + l, id);
            }else {
                assertEquals(1152921504612750848L + (l % 512), id);
            }
            if(l % 512 == 511) {
                System.out.println("l: " + l + "," + "nextMillis: " + currentTimeMillis.incrementAndGet());
            }
        }
    }

    @Test
    void timestampGreaterThan1000() {
        final AtomicLong currentTimeMillis = new AtomicLong(1663412306944L);
        JakeId jIdGenerator = new JakeId() {
            @Override
            public long currentTimeMillis() {
                return currentTimeMillis.get();
            }
        };
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
                long id = jIdGenerator.nextId();
                logId(id);
            }
        } catch (JakeIdException e) {
            ex = e;
        }
        assertNotNull(ex);
    }

    @Test
    void timestampLessThan1000() {
        final AtomicLong currentTimeMillis = new AtomicLong(1663412306944L);
        JakeId jIdGenerator = new JakeId() {
            @Override
            public long currentTimeMillis() {
                return currentTimeMillis.get();
            }
        };
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
            long id = jIdGenerator.nextId();
            assertNotEquals(id, lastId);
            lastId = id;
            logId(id);
            if(l == 0) {
                currentTimeMillis.set(currentTimeMillis.get() - 1000);
            }
        }
    }

    @Test
    void productionTest() {
        JakeId jIdGenerator = new JakeId();
        long lastId = 0;
        long generatorIdCount = 5120000;
        long timestamp = System.currentTimeMillis();
        for(long l = 0; l <= generatorIdCount; l++) {
            long id = jIdGenerator.nextId();
            assertNotEquals(id, lastId);
            lastId = id;
            if(l == 0 || l == 5120000) {
                logId(id);
            }
        }
        long millis = System.currentTimeMillis() - timestamp;
        System.out.println(millis);
        System.out.println(generatorIdCount / millis);
    }
}
