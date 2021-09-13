package io.github.funcfoo.id;

import java.util.concurrent.locks.LockSupport;

public class JakeIdImpl implements JakeId {
    protected static final long ONE_MILLIS_NANOS = 1000000;
    protected static final long HALF_MILLIS_NANOS = ONE_MILLIS_NANOS / 2;

    private long customLastTime = 0;
    private long sequence = 0;

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
        checkConstructorArgs(startTime, bitLengthOfTime, bitLengthOfMachineId,
                bitLengthOfSequence, sequenceLifeCycle, machineId);
        this.customStartTime = customTimeStamp(startTime, sequenceLifeCycle);
        this.bitLengthSequenceAndMachineId = bitLengthOfMachineId + bitLengthOfSequence;
        this.machineId = machineId << bitLengthOfSequence;
        this.maxSequence = ~(-1 << bitLengthOfSequence);
        this.maxTime = ~(-1L << bitLengthOfTime);
        this.sequenceLifeCycle = sequenceLifeCycle;
        this.waitNanos = waitNanos(sequenceLifeCycle);
        this.customOneSecond = customTimeStamp(1000, this.sequenceLifeCycle);
    }

    private void checkConstructorArgs(
            long startTime, int bitLengthOfTime, int bitLengthOfMachineId,
            int bitLengthOfSequence, int sequenceLifeCycle, int machineId) {
        long currentTimeMillis = currentTimeMillis();
        checkPositiveNum(startTime, "startTime");
        checkPositiveNum(bitLengthOfTime, "bitLengthOfTime");
        checkPositiveNum(bitLengthOfMachineId, "bitLengthOfMachineId");
        checkPositiveNum(bitLengthOfSequence, "bitLengthOfSequence");
        checkPositiveNum(sequenceLifeCycle, "sequenceLifeCycle");
        checkPositiveNum(machineId,"machineId");
        if(sequenceLifeCycle > 1000) {
            throw new IllegalArgumentException("The sequenceLifeCycle must be less than or equal 1000");
        }
        if(startTime > currentTimeMillis) {
            throw new IllegalArgumentException("The startTime must be less than currentTimeMillis");
        }
        if(bitLengthOfTime + bitLengthOfMachineId + bitLengthOfSequence != 63) {
            throw new IllegalArgumentException("Bit length must be equal 63");
        }
        int maxMachineId = ~(-1 << bitLengthOfMachineId);
        if (machineId > maxMachineId) {
            throw new IllegalArgumentException("the machineId must be greater than 0 and less than " + maxMachineId);
        }
        int bitLengthMS = bitLengthOfMachineId + bitLengthOfSequence;
        long startStamp = customTimeStamp(currentTimeMillis, sequenceLifeCycle)
                - customTimeStamp(startTime, sequenceLifeCycle);
        if(startStamp << bitLengthMS >> bitLengthMS != startStamp) {
            throw new IllegalArgumentException("Time is out, Please check The startTime");
        }
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

    private long nextTime(long now) {
        long time = now;
        // max wait time: 1 second
        while (this.customLastTime >= time
                && this.customLastTime - time <= customOneSecond) {
            LockSupport.parkNanos(this.waitNanos);
            time = currentCustomTimeStamp();
        }
        if (this.customLastTime - time > customOneSecond) {
            throw new JakeIdException("The currentTimeMillis greater than lastTime one second! Please check the system timestamp!");
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

    protected static void checkPositiveNum(long num, String name) {
        if(num < 1) {
            throw new IllegalArgumentException("The " + name +" must be greater than 0");
        }
    }
}
