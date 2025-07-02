package space.jamestang.ktimer.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public final class TimerCancelData extends MessageData{
    private final String timerId;
    private String reason;
    private final Boolean force = false;
}
