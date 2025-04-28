package space.jamestang.ktimer.client.datatype;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KTimerMessage {
    private String clientId;
    private MessageType type;
    private String taskId;
    private KTimerTaskContext context;

    public static KTimerMessage createClientRegisterMessage() {
        return new KTimerMessage(null, MessageType.CLIENT_REGISTER, "REGISTRY", null);
    }
    public static KTimerMessage createScheduleTaskMessage(String clientId, String taskId, KTimerTaskContext context) {
        if (clientId == null || clientId.isEmpty()){
            throw new IllegalArgumentException("clientId cannot be null or empty");
        }
        return new KTimerMessage(clientId, MessageType.SCHEDULE_TASK, taskId, context);
    }


}
