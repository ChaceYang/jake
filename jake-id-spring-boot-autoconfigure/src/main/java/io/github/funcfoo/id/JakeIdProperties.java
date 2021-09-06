package io.github.funcfoo.id;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Instant;

@ConfigurationProperties("id.jake")
public class JakeIdProperties {
    private static final String MODEL_L = "L";
    private static final String MODEL_S = "S";
    private static final String MODEL_CUSTOM = "CUSTOM";

    private Integer machineId;
    private Instant startTime;

    private String model = MODEL_S;

    private Integer bitLengthOfTime;
    private Integer bitLengthOfMachineId;
    private Integer bitLengthOfSequence;
    private Integer sequenceLifeCycle;

    public Integer getMachineId() {
        return machineId;
    }

    public void setMachineId(Integer machineId) {
        this.machineId = machineId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = Instant.parse(startTime);
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getBitLengthOfTime() {
        return bitLengthOfTime;
    }

    public void setBitLengthOfTime(Integer bitLengthOfTime) {
        this.bitLengthOfTime = bitLengthOfTime;
    }

    public Integer getBitLengthOfMachineId() {
        return bitLengthOfMachineId;
    }

    public void setBitLengthOfMachineId(Integer bitLengthOfMachineId) {
        this.bitLengthOfMachineId = bitLengthOfMachineId;
    }

    public Integer getBitLengthOfSequence() {
        return bitLengthOfSequence;
    }

    public void setBitLengthOfSequence(Integer bitLengthOfSequence) {
        this.bitLengthOfSequence = bitLengthOfSequence;
    }

    public Integer getSequenceLifeCycle() {
        return sequenceLifeCycle;
    }

    public void setSequenceLifeCycle(Integer sequenceLifeCycle) {
        this.sequenceLifeCycle = sequenceLifeCycle;
    }

    @Override
    public String toString() {
        return "JakeIdProperties{" +
                "machineId=" + machineId +
                ", startTime=" + startTime +
                ", model='" + model + '\'' +
                ", bitLengthOfTime=" + bitLengthOfTime +
                ", bitLengthOfMachineId=" + bitLengthOfMachineId +
                ", bitLengthOfSequence=" + bitLengthOfSequence +
                ", sequenceLifeCycle=" + sequenceLifeCycle +
                '}';
    }

    public JakeIdBuilder toBuilder() {
        JakeIdBuilder jakeIdBuilder = JakeIdBuilder.create()
                .machineId(this.machineId);
        if(this.startTime != null) {
            jakeIdBuilder.startTime(this.startTime.toEpochMilli());
        }
        if(MODEL_L.equalsIgnoreCase(this.model)) {
            return jakeIdBuilder.modelL();
        }
        if(MODEL_S.equalsIgnoreCase(this.model)) {
            return jakeIdBuilder.modelS();
        }
        if(MODEL_CUSTOM.equalsIgnoreCase(this.model)) {
            jakeIdBuilder.bitLengthOfMachineId(this.bitLengthOfMachineId)
                    .bitLengthOfSequence(this.bitLengthOfSequence)
                    .bitLengthOfTime(this.bitLengthOfTime)
                    .sequenceLifeCycle(this.sequenceLifeCycle);
            return jakeIdBuilder;
        }
        throw new IllegalArgumentException("id.jake.model error");
    }
}
