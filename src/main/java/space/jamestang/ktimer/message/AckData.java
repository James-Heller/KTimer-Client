package space.jamestang.ktimer.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import space.jamestang.ktimer.message.enums.AckStatus;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class AckData extends MessageData{
    private String originalMessageId;
    private AckStatus status;
    private Integer code;
    private String message;
    private Map<String, Object> details;
}
