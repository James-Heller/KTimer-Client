package space.jamestang.ktimer.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import space.jamestang.ktimer.client.KTimerClient;
import space.jamestang.ktimer.client.datatype.KTimerMessage;

import java.io.IOException;
import java.util.function.Function;

public class Test{
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();

        Function<KTimerMessage, byte[]> encoder = (KTimerMessage message) -> {
            try {
                return mapper.writeValueAsBytes(message);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };

        Function<byte[], KTimerMessage> decoder = (byte[] bytes) -> {
            try {
                return mapper.readValue(bytes, KTimerMessage.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        var client = new KTimerClient("localhost", 4396, encoder, decoder);

        client.connect();
    }
}