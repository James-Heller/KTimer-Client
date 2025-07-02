package space.jamestang.ktimer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import space.jamestang.ktimer.core.CallbackHandler;
import space.jamestang.ktimer.message.*;
import space.jamestang.ktimer.message.enums.AckStatus;
import space.jamestang.ktimer.message.enums.MessageType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class KTimerClient {

    @NonNull
    private final Function<KTimerMessage, byte[]> messageEncoder;
    @NonNull
    private final Function<byte[], KTimerMessage> messageDecoder;
    @NonNull
    private final String host;
    @NonNull
    private final Integer port;
    @NonNull
    private final String clientId;
    @NonNull
    private final String instanceId;
    @NonNull
    private final String serviceName;

    @Setter
    private String version = "1.0.0"; // Default version, can be overridden

    private final Map<Class<?>, CallbackHandler<?>> callbackHandlers = new ConcurrentHashMap<>();

    private DataOutputStream out;
    private DataInputStream in;
    private volatile boolean running = false;
    private ExecutorService handlerThreadPool = Executors.newFixedThreadPool(8);


    private void connectAndRegister() throws IOException{
        try(Socket socket = new Socket(host, port)) {
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            running = true;

            var metaData = new ClientMetadata(
                    socket.getLocalAddress().getHostName(),
                    socket.getLocalAddress().getHostAddress(),
                    socket.getLocalPort(),
                    System.getenv("KTIMER_ENVIRONMENT") != null ? System.getenv("KTIMER_ENVIRONMENT") : "default"
            );

            var registerData = MessageBuilder.INSTANCE.createClientRegister(clientId, instanceId, serviceName, version, metaData);

            byte[] encodedMessage = messageEncoder.apply(registerData);

            sendMessage(encodedMessage);

            var response = receiveMessage();
            if (response.getType() != MessageType.ACK){
                throw new IOException(response.getType().toString());
            }
            AckData ackData = (AckData) response.getData();
            if (ackData.getStatus() != AckStatus.SUCCESS){
                throw new IOException("Failed to register client: " + ackData.getMessage());
            }


        }
    }

    public void start() throws IOException {

        if (callbackHandlers.isEmpty()){
            log.warn("No callback handlers registered. Please register at least one handler before starting the client.");
        }

        connectAndRegister();
        startMessageReadLoop();
    }

    /**
     * Registers a callback handler for a specific message type.
     *
     * @param messageType the class of the message type
     * @param handler the callback handler to register
     * @param <T> the type of the message
     */
    public <T> void registerCallbackHandler(@NonNull Class<T> messageType, @NonNull CallbackHandler<T> handler) {
        callbackHandlers.put(messageType, handler);
    }


    /**
     * Unregisters a callback handler for a specific payload type.
     * @param payloadType the class of the payload type to unregister
     */
    public void unregisterHandler(Class<?> payloadType) {
        callbackHandlers.remove(payloadType);
    }

    private void startMessageReadLoop() throws IOException{
        Thread messageReadThread = new Thread(() -> {
            while (running) {
                try {
                    var data = receiveMessage();
                    dispatchMessage(data);
                } catch (Exception e) {
                    var errMsg = MessageBuilder.INSTANCE.createError(clientId, "", e.getLocalizedMessage(), e.toString(), null, null);
                    try {
                        sendMessage(messageEncoder.apply(errMsg));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }


    private void dispatchMessage(KTimerMessage msg){
        TimerCallbackData callbackData = (TimerCallbackData) msg.getData();


        Class<?> payloadType = null;
        CallbackHandler<?> handler = null;

        for (Class<?> clazz : callbackHandlers.keySet()) {
            if (clazz.isInstance(callbackData)) {
                payloadType = clazz;
                handler = callbackHandlers.get(clazz);
                break;
            }
        }

        if (payloadType == null || handler == null) {
            log.error("No handler registered for payload type: {}", callbackData.getClass().getName());
            return;
        }

        CallbackHandler<?> finalHandler = handler;
        Class<?> finalPayloadType = payloadType;
        handlerThreadPool.submit(() -> {
            try {
                //noinspection unchecked
                ((CallbackHandler<Object>) finalHandler).onCallback(callbackData, callbackData);
            } catch (Exception e) {
                log.error("Error handling callback for payload type: {}", finalPayloadType.getName(), e);
            }
        });
    }



    private void sendMessage(@NonNull byte[] payload) throws IOException {
        if (out == null) {
            throw new IllegalStateException("Output stream is not initialized. Please connect first.");
        }
        out.writeInt(payload.length);
        out.write(payload);
        out.flush();
    }

    private KTimerMessage receiveMessage() throws IOException {
        if (in == null) {
            throw new IllegalStateException("Input stream is not initialized. Please connect first.");
        }
        int length = in.readInt();
        byte[] data = new byte[length];
        in.readFully(data);
        return messageDecoder.apply(data);
    }
}
