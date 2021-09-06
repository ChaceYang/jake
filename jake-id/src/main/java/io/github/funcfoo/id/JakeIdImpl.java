package io.github.funcfoo.id;

import java.util.concurrent.locks.LockSupport;

public class JakeIdImpl implements JakeId {
    protected static final long ONE_MILLIS_NANOS = 1000000;
    protected static final long HALF_MILLIS_NANOS = ONE_MILLIS_NANOS / 2;

    private long customLastTime = 0;
    private int sequence = 0;

    private final long customStartTime;
    private final int bitLengthSequenceAndMachineId;
    private final int sequenceLifeCycle;
    private final int machineId;
    private final long waitNanos;
    private final int maxSequence;
    private final long maxTime;
    private final long customOneSecond;

    protected JakeIdImpl(
            long startTime, int bitLengthOfTime, int bitLengthOfMachineId,
            int bitLengthOfSequence, int sequenceLifeCycle, int machineId) {
        if(sequenceLifeCycle > 1000 || sequenceLifeCycle < 1) {
            throw new IllegalArgumentException("SequenceLifeCycle must be less than or equal 1000 and greater than 0");
        }
        long customStartTimeStamp = customTimeStamp(startTime, sequenceLifeCycle);
        long customCurrentTimeStamp = customTimeStamp(currentTimeMillis(), sequenceLifeCycle);
        int bitLengthMS = bitLengthOfMachineId + bitLengthOfSequence;
        if(customStartTimeStamp < 0
                || customStartTimeStamp > customCurrentTimeStamp) {
            throw new IllegalArgumentException("StartTime must be less than currentTimeMillis and greater than 0");
        }
        long startStamp = customCurrentTimeStamp - customStartTimeStamp;
        if(startStamp << bitLengthMS >> bitLengthMS != startStamp) {
            throw new IllegalArgumentException("Time is out");
        }
        if(bitLengthOfTime + bitLengthOfMachineId + bitLengthOfSequence != 63) {
            throw new IllegalArgumentException("Bit length must be equal 63");
        }
        int maxMachineId = ~(-1 << bitLengthOfMachineId);
        if (machineId < 0 || machineId > maxMachineId) {
            throw new IllegalArgumentException("Machine id must be greater than 0 and less than " + maxMachineId);
        }
        this.customStartTime = customStartTimeStamp;
        this.bitLengthSequenceAndMachineId = bitLengthMS;
        this.machineId = machineId << bitLengthOfSequence;
        this.maxSequence = ~(-1 << bitLengthOfSequence);
        this.maxTime = ~(-1L << bitLengthOfTime);
        this.sequenceLifeCycle = sequenceLifeCycle;
        this.waitNanos = waitNanos(sequenceLifeCycle);
        this.customOneSecond = customTimeStamp(1000, this.sequenceLifeCycle);
    }

    /**
     * get currentTimeMillis
     * overwrite on test
     * @return currentTimeMillis
     */
    protected long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    protected long currentCustomTimeStamp() {
        return customTimeStamp(currentTimeMillis(), this.sequenceLifeCycle);
    }

    /**
     * @return time/sequenceLifeCycle
     */
    protected static long customTimeStamp(long time, int sequenceLifeCycle) {
        if(sequenceLifeCycle == 1) {
            return time;
        }
        return time / sequenceLifeCycle;
    }

    protected static long waitNanos(int sequenceLifeCycle) {
        return HALF_MILLIS_NANOS * sequenceLifeCycle + 1;
    }

    private long nextTime(long now) {
        long time = now;
        // max wait time: 1 second
        while (this.customLastTime >= time
                && this.customLastTime - time <= customOneSecond) {
            LockSupport.parkNanos(this.waitNanos);
            time = currentCustomTimeStamp();
        }
        if (this.customLastTime - time > customOneSecond) {
            throw new JakeIdException("CurrentTimeMillis greater than lastTime 1000! Please check timestamp!");
        }
        return time;
    }

    private long id() {
        return ((this.customLastTime - this.customStartTime) & this.maxTime) << this.bitLengthSequenceAndMachineId
                | this.machineId
                | this.sequence;
    }

    public synchronized long nextId() {
        long currentCustomTimestamp = currentCustomTimeStamp();
        if (this.customLastTime == currentCustomTimestamp) {
            this.sequence = (this.sequence + 1) & this.maxSequence;
            // no sequence
            if (this.sequence == 0) {
                currentCustomTimestamp = nextTime(currentCustomTimestamp);
            }
        } else {
            this.sequence = 0;
            // timestamp rollback
            if (this.customLastTime > currentCustomTimestamp) {
                currentCustomTimestamp = nextTime(currentCustomTimestamp);
            }
        }
        this.customLastTime = currentCustomTimestamp;
        return id();
    }
}
