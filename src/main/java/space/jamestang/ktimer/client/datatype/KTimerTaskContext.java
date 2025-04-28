package space.jamestang.ktimer.client.datatype;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KTimerTaskContext {
    private Object ctx;
    private Long delay;
}
