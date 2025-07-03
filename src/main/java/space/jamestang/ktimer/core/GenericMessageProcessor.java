package space.jamestang.ktimer.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import space.jamestang.ktimer.message.AckData;
import space.jamestang.ktimer.message.KTimerMessage;
import space.jamestang.ktimer.message.TimerCallbackData;
import space.jamestang.ktimer.message.enums.AckStatus;

import java.util.Map;
import java.util.concurrent.ExecutorService;

@Slf4j
@AllArgsConstructor
public class GenericMessageProcessor {

    private final Map<Class<?>, CallbackHandler<?>> callbackHandlers;
    private final ExecutorService handlerThreadPool;

    public void processMessage(KTimerMessage msg){

        switch (msg.getType()){
            case ACK -> handleACKMessage( (AckData) msg.getData());
            case TIMER_CALLBACK -> dispatchMessage(msg);
            default -> handleUnknowMessage(msg);
        }
    }

    private void handleACKMessage(AckData msg) {
        // Handle ACK message
        if (msg.getStatus() == AckStatus.SUCCESS) {
            log.trace("ACK received for message ID: {}", msg.getMessage());
        } else {
            log.warn("ACK failed for message ID: {}, Status: {}", msg.getStatus(), msg.getMessage());
        }
    }

    private void dispatchMessage(KTimerMessage msg){
        TimerCallbackData callbackData = (TimerCallbackData) msg.getData();


        Class<?> payloadType;
        try {
             payloadType = Class.forName(callbackData.getClassInfo());
        }catch (ClassNotFoundException e){
            log.error("Class not found for class info: {}", callbackData.getClassInfo(), e);
            return;
        }
        CallbackHandler<?> handler = callbackHandlers.get(payloadType);
        if (handler == null) {
            log.warn("No handler found for payload type: {}", payloadType.getName());
            return;
        }

        var payload = new ObjectMapper().convertValue(callbackData.getPayload(), payloadType);

        log.debug("Dispatching message with payload type: {}", payloadType.getName());

        handlerThreadPool.submit(() -> {
            try {
                //noinspection unchecked
                ((CallbackHandler<Object>)handler).onCallback(payload, callbackData);
            } catch (Exception e) {
                log.error("Error processing callback for payload type: {}", payloadType.getName(), e);
                // Optionally, you can send an error ACK back or handle it accordingly
            }
        });

    }

    private void handleUnknowMessage(KTimerMessage msg) {
        log.warn("Received unknown message type: {}", msg.getType());

    }
}
