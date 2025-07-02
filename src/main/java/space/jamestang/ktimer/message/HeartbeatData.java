package space.jamestang.ktimer.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public final class HeartbeatData extends MessageData{
    private final String status;
    private final Integer activeTimers;
    private final Long processedCount;
    private final Long uptime;
    private SystemInfo systemInfo;
}

record SystemInfo(
        Double cpuUsage,
        Double memoryUsage,
        Double diskUsage
){}