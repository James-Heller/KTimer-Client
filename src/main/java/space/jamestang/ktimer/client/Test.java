package space.jamestang.ktimer.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.jamestang.ktimer.client.datatype.KTimerMessage;
import space.jamestang.ktimer.client.datatype.KTimerTaskContext;
import space.jamestang.ktimer.client.datatype.MessageType;

import java.io.IOException;
import java.util.function.Function;

public class Test{
    private static final Logger log = LoggerFactory.getLogger(Test.class);

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
        client.setHandler(new KTimerCallBackHandler() {
            @Override
            public KTimerMessage onTaskTrigger(KTimerMessage message) {
                System.out.println("Task triggered: " + message);
                return new KTimerMessage(message.getClientId(), MessageType.TASK_RECEIVED, message.getTaskId(), null);
            }
        });

        client.connect();

        KTimerMessage cancelOrder = new KTimerMessage(null, MessageType.SCHEDULE_TASK, "AllTaken-1123", new KTimerTaskContext("",1L));
        client.sendTask(cancelOrder);
        log.info("task added");
    }
}