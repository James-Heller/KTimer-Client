package space.jamestang.ktimer.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public final class TimerCallbackData extends MessageData {
    private String timerId;
    private Long originalTimestamp;
    private Long executeTimestamp;
    private Integer attempt = 1;
    private Object payload;
    private String classInfo;
}
