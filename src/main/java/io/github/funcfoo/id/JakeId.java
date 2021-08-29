package io.github.funcfoo.id;

import java.time.Instant;
import java.util.concurrent.locks.LockSupport;

public class JakeId {
    // 512 per millis, 512000 per second
    private static final int BIT_LENGTH_SEQUENCE = 9;
    // max sequence
    private static final int MAX_SEQUENCE = ~(-1 << 9);
    // (?).(?).(0-31).(0-255)(0.0.11111.11111111) max machine 8192
    private static final int BIT_LENGTH_MACHINE_ID = 13;
    // timestamp left move
    private static final int BIT_LENGTH_TIMESTAMP_MOVE = BIT_LENGTH_SEQUENCE + BIT_LENGTH_MACHINE_ID;
    // 68 year
    private static final int BIT_LENGTH_TIMESTAMP = 41;
    // max timestamp
    private static final long MAX_TIMESTAMP = ~(-1L << BIT_LENGTH_TIMESTAMP);
    // nanos const
    private static final int ONE_MILLIS_NANOS = 1000000;
    private static final int HALF_MILLIS_NANOS = ONE_MILLIS_NANOS / 2;

    private final long startTime;
    private final long machineId;

    private long lastTime = 0;
    private int sequence = 0;

    public JakeId() {
        this(defaultStartTime(), defaultMachineId());
    }

    public JakeId(long startTime, long machineId) {
        long now = currentTimeMillis();
        if (startTime > now) {
            throw new IllegalArgumentException("StartTime must be less than currentTimeMillis");
        }
        long maxMachinedId = (1L << BIT_LENGTH_MACHINE_ID);
        if (machineId < 0 || machineId >= maxMachinedId) {
            throw new IllegalArgumentException("Machine id must be greater than 0 and less than " + maxMachinedId);
        }
        this.startTime = startTime;
        this.machineId = machineId << BIT_LENGTH_SEQUENCE;
    }

    public synchronized long nextId() {
        long now = currentTimeMillis();
        if (this.lastTime < now) {
            this.lastTime = now;
            this.sequence = 0;
            return id();
        }
        if (this.lastTime == now) {
            if(this.sequence < MAX_SEQUENCE) {
                this.sequence++;
                return id();
            }
            do {
                LockSupport.parkNanos(HALF_MILLIS_NANOS);
                now = currentTimeMillis();
            } while (this.lastTime == now);
            this.lastTime = now;
            this.sequence = 0;
            return id();
        }
        while (this.lastTime >= now && this.lastTime - now <= 1000) {
            // currentTimeMillis must greater than this.lastTime
            LockSupport.parkNanos(ONE_MILLIS_NANOS * (this.lastTime - now + 1));
            now = currentTimeMillis();
        }
        if(this.lastTime - now > 1000) {
            throw new JakeIdException("CurrentTimeMillis greater than lastTime 1000! Please check timestamp!");
        }
        this.lastTime = now;
        this.sequence = 0;
        return id();
    }

    private long id() {
        return ((this.lastTime - this.startTime) & MAX_TIMESTAMP) << BIT_LENGTH_TIMESTAMP_MOVE
                | this.machineId
                | this.sequence;
    }

    /**
     * get currentTimeMillis
     * overwrite on test
     */
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    private static long defaultStartTime() {
        return Instant.parse("2014-01-01T00:00:00Z").toEpochMilli();
    }

    private static int defaultMachineId() {
        return machineId(JakeUtils.privateIpv4());
    }

    /**
     * Generate machine id, visible for test
     * @param ipv4 int arr
     * @return machineId
     */
    static int machineId(int[] ipv4) {
        return ((ipv4[2] % 32) << 8) | ipv4[3];
    }
}
