
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import space.jamestang.ktimer.KTimerClient;
import space.jamestang.ktimer.message.KTimerMessage;

import java.io.IOException;
import java.util.function.Function;

public class Main {


    @SneakyThrows
    public static void main(String[] args) throws IOException {

        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();


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

        KTimerClient client = new KTimerClient(messageEncoder, messageDecoder, "localhost", 8080, "T1", "001", "Test");

        client.start();
    }
}
