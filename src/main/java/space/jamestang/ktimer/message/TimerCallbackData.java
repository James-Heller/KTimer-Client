package space.jamestang.ktimer.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public final class TimerCallbackData extends MessageData {
    private final String timerId;
    private final Long originalTimestamp;
    private final Long executeTimestamp;
    private final Integer attempt = 1;
    private final Object payload;
}
