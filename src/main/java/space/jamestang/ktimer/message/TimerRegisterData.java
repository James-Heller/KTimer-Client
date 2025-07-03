package space.jamestang.ktimer.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import space.jamestang.ktimer.message.enums.TimerPriority;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public final class TimerRegisterData extends MessageData{
    private final String timerId;
    private final long delayMillis;
    private Long executeAt;
    private final long repeatInterval = 0L;
    private final int maxRetries = 3;
    @Setter
    private TimerPriority priority = TimerPriority.NORMAL;
    private final Object payload;
    private String classInfo;
    private Map<String, String> tags = null;
}
