package space.jamestang.ktimer.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public final class ClientMetadata extends MessageData {
    private final String hostname;
    private final String ip;
    private final Integer port;
    private final String environment;
}
