package io.github.funcfoo.id;

import java.util.Objects;

public class JakeIdBuilder {
    private Integer machineId;
    private Long startTime;

    private Integer bitLengthOfTime;
    private Integer bitLengthOfMachineId;
    private Integer bitLengthOfSequence;
    private Integer sequenceLifeCycle;

    public Integer getMachineId() {
        return machineId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Integer getBitLengthOfTime() {
        return bitLengthOfTime;
    }

    public Integer getBitLengthOfMachineId() {
        return bitLengthOfMachineId;
    }

    public Integer getBitLengthOfSequence() {
        return bitLengthOfSequence;
    }

    public Integer getSequenceLifeCycle() {
        return sequenceLifeCycle;
    }

    private JakeIdBuilder() {}

    public static JakeIdBuilder create() {
        return new JakeIdBuilder();
    }

    public JakeIdBuilder machineId(Integer machineId) {
        this.machineId = machineId;
        return this;
    }

    public JakeIdBuilder startTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }

    public JakeIdBuilder bitLengthOfTime(Integer bitLengthOfTime) {
        this.bitLengthOfTime = bitLengthOfTime;
        return this;
    }

    public JakeIdBuilder bitLengthOfMachineId(Integer bitLengthOfMachineId) {
        this.bitLengthOfMachineId = bitLengthOfMachineId;
        return this;
    }

    public JakeIdBuilder bitLengthOfSequence(Integer bitLengthOfSequence) {
        this.bitLengthOfSequence = bitLengthOfSequence;
        return this;
    }

    public JakeIdBuilder sequenceLifeCycle(Integer sequenceLifeCycle) {
        this.sequenceLifeCycle = sequenceLifeCycle;
        return this;
    }

    /**
     *  bitLengthOfSequence = 9, 512 per 1millis, 512000 per second
     *  sequenceLifeCycle = 1millis
     *  bitLengthOfMachineId = 16, (?).(?).(0-31).(0-255)(0.0.11111.11111111) 0 - 8191
     *  bitLengthOfTime = 38, 68 year
     */
    public JakeIdBuilder modelS() {
        this.bitLengthOfSequence = 9;
        this.sequenceLifeCycle = 1;
        this.bitLengthOfMachineId = 13;
        this.bitLengthOfTime = 41;
        return this;
    }

    /**
     *  bitLengthOfSequence = 9, 512 per 10 millis, 51200 per second
     *  sequenceLifeCycle = 10millis
     *  bitLengthOfMachineId = 16, (?).(?).(0-31).(0-255)(0.0.11111111.11111111) 0 - 65535
     *  bitLengthOfTime = 38, 86 year
     */
    public JakeIdBuilder modelL() {
        this.bitLengthOfSequence = 9;
        this.sequenceLifeCycle = 10;
        this.bitLengthOfMachineId = 16;
        this.bitLengthOfTime = 38;
        return this;
    }

    public JakeId build() {
        if(machineId == null) {
            this.machineId = JakeUtils.defaultMachineId();
        }
        if(startTime == null) {
            this.startTime = JakeUtils.defaultStartTime();
        }
        Objects.requireNonNull(bitLengthOfTime);
        Objects.requireNonNull(bitLengthOfMachineId);
        Objects.requireNonNull(bitLengthOfSequence);
        Objects.requireNonNull(sequenceLifeCycle);
        return new JakeIdImpl(this.startTime, this.bitLengthOfTime, this.bitLengthOfMachineId,
                this.bitLengthOfSequence, this.sequenceLifeCycle, this.machineId);
    }
}
