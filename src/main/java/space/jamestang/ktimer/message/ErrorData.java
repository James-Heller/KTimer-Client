package space.jamestang.ktimer.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public final class ErrorData extends MessageData{
    private String originalMessageId;
    private final String errorCode;
    private final String errorMessage;
    private Map<String, Object> details;
    private List<String> suggestions;
}
