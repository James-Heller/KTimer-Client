package space.jamestang.ktimer.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import space.jamestang.ktimer.message.enums.MessageType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KTimerMessage {
    private  String version = "1.0";
    private  MessageType type;
    private  String messageId;
    private  String clientId;
    private  Long timestamp;
    private  MessageData data;
}
