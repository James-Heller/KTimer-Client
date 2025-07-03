
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import space.jamestang.ktimer.KTimerClient;
import space.jamestang.ktimer.heartbeat.HeartbeatCallbackHandler;
import space.jamestang.ktimer.message.HeartbeatData;
import space.jamestang.ktimer.message.KTimerMessage;
import space.jamestang.ktimer.message.enums.TimerPriority;

import java.io.IOException;
import java.util.function.Function;

public class Main {


    public static void main(String[] args) throws IOException {

        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();


        KTimerClient client = getKTimerClient(mapper);
        client.registerCallbackHandler(HeartbeatData.class, new HeartbeatCallbackHandler());
        client.registerCallbackHandler(SomeData.class, new SomeDataCallbackHandler());
        client.startAsync();

        var payload = new SomeData("10086", "test", "hello world", 4396, false);
        client.scheduleTask("OID1", payload, 10000L, TimerPriority.NORMAL, null);
    }

    private static KTimerClient getKTimerClient(ObjectMapper mapper) {
        Function<KTimerMessage, byte[]> messageEncoder = data -> {
            try {
                return mapper.writeValueAsBytes(data);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
        Function<byte[], KTimerMessage> messageDecoder = bytes -> {
            try {
                return mapper.readValue(bytes, KTimerMessage.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        return new KTimerClient(messageEncoder, messageDecoder, "localhost", 8080, "T1", "001", "Test");
    }
}
