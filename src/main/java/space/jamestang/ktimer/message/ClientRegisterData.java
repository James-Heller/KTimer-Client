package space.jamestang.ktimer.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public final class ClientRegisterData extends MessageData {
    private  String instanceId;
    private  String serviceName;
    private  String version;
    private  List<String> capabilities;
    private  ClientMetadata metadata;
}
