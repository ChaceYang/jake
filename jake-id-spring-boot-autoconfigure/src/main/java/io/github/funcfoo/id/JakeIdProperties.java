package io.github.funcfoo.id;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Instant;

@ConfigurationProperties("id.jake")
public class JakeIdProperties {
    private Integer machineId;
    private Long startTime;

    public Integer getMachineId() {
        if(this.machineId == null) {
            return JakeUtils.defaultMachineId();
        }
        return machineId;
    }

    public void setMachineId(Integer machineId) {
        this.machineId = machineId;
    }

    public long getStartTime() {
        if(startTime == null) {
            return JakeUtils.defaultStartTime();
        }
        return startTime;
    }

    public void setStartTime(String startTime) {
        if(startTime == null) {
            return;
        }
        this.startTime = Instant.parse(startTime).toEpochMilli();
    }
}
