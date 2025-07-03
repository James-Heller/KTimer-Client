package space.jamestang.ktimer.message;


import lombok.NonNull;
import space.jamestang.ktimer.message.enums.AckStatus;
import space.jamestang.ktimer.message.enums.MessageType;
import space.jamestang.ktimer.message.enums.TimerPriority;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class MessageBuilder {

    public static MessageBuilder INSTANCE = new MessageBuilder();

    private MessageBuilder() {
    }

    private String generateMessageId() {
        var timestamp = System.currentTimeMillis();
        var radom = (int) (Math.random() * 10000);
        return String.format("msg_%d_%04d", timestamp, radom);
    }


    public KTimerMessage createClientRegister(@NonNull String clientId,
                                              @NonNull String instanceId,
                                              @NonNull String serviceName,
                                              @NonNull String version,
                                              @NonNull ClientMetadata metadata) {
        return new KTimerMessage(
                version,
                MessageType.CLIENT_REGISTER,
                generateMessageId(),
                clientId,
                System.currentTimeMillis(),
                new ClientRegisterData(
                        instanceId,
                        serviceName,
                        version,
                        new ArrayList<>(),
                        metadata
                )

        );

    }

    public KTimerMessage createTimerRegister(@NonNull String clientId,
                                             @NonNull String timerId,
                                             @NonNull Long delayMillis,
                                             @NonNull Object payload,
                                             @NonNull String classInfo,
                                             TimerPriority priority,
                                             Map<String, String> tags) {
        var data = new TimerRegisterData(
                timerId,
                delayMillis,
                payload
        );
        data.setPriority(priority);
        data.setTags(tags);
        data.setClassInfo(classInfo);

        return new KTimerMessage(
                "1.0",
                MessageType.TIMER_REGISTER,
                generateMessageId(),
                clientId,
                System.currentTimeMillis(),
                data
        );
    }


    public KTimerMessage createTimerCancel(@NonNull String clientId,
                                           @NonNull String timerId,
                                           String reason) {
        var data = new TimerCancelData(timerId);
        data.setReason(reason);
        return new KTimerMessage(
                "1.0",
                MessageType.TIMER_CANCEL,
                generateMessageId(),
                clientId,
                System.currentTimeMillis(),
                data
        );
    }

    public KTimerMessage createHeartbeat(
            @NonNull String clientId,
            @NonNull String instanceId,
            @NonNull String serviceName,
            @NonNull String version,
            @NonNull ClientMetadata metadata
    ) {
        return new KTimerMessage(
                "1.0",
                MessageType.HEARTBEAT,
                generateMessageId(),
                clientId,
                System.currentTimeMillis(),
                new HeartbeatData("healthy",
                        0,
                        0L,
                        0L)
        );
    }

    /**
     * Create an acknowledgment message for a timer message.
     *
     * @param clientId          client ID of the sender
     * @param originalMessageId ID of the original message being acknowledged
     * @param status            acknowledgment status, e.g., SUCCESS, FAILURE. default is SUCCESS
     * @param message           optional message providing additional context for the acknowledgment. default is Success
     * @param details           optional details providing additional context for the acknowledgment. default is empty map
     * @return KTimerMessage representing the acknowledgment
     */
    public KTimerMessage createAck(@NonNull String clientId,
                                   @NonNull String originalMessageId,
                                   AckStatus status, String message,
                                   Map<String, Object> details) {
        var data = new AckData();
        data.setOriginalMessageId(originalMessageId);
        data.setStatus(status != null ? status : AckStatus.SUCCESS);
        data.setMessage(message != null ? message : "Success");
        data.setDetails(details != null ? details : Map.of());

        return new KTimerMessage(
                "1.0",
                MessageType.ACK,
                generateMessageId(),
                clientId,
                System.currentTimeMillis(),
                data
        );
    }

    public KTimerMessage createError(@NonNull String clientId, String originalMessageId, String errCode, String errMessage,
                                     Map<String, Object> details, List<String> suggestions) {
        var data = new ErrorData(errCode, errMessage);
        data.setOriginalMessageId(originalMessageId);
        data.setDetails(details);
        data.setSuggestions(suggestions);

        return new KTimerMessage(
                "1.0",
                MessageType.ERROR,
                generateMessageId(),
                clientId,
                System.currentTimeMillis(),
                data
        );
    }


}
