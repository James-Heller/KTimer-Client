package space.jamestang.ktimer;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import space.jamestang.ktimer.core.CallbackHandler;
import space.jamestang.ktimer.core.GenericMessageProcessor;
import space.jamestang.ktimer.message.*;
import space.jamestang.ktimer.message.enums.AckStatus;
import space.jamestang.ktimer.message.enums.MessageType;
import space.jamestang.ktimer.message.enums.TimerPriority;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

@Slf4j
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

    private Integer heartbeatInterval = 5000; // Default heartbeat interval in milliseconds

    private final CountDownLatch shutdownLatch = new CountDownLatch(1);

    @Setter
    private String version = "1.0.0"; // Default version, can be overridden

    private final Map<Class<?>, CallbackHandler<?>> callbackHandlers = new ConcurrentHashMap<>();
    Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private volatile boolean running = false;
    private volatile boolean connected = false;
    private final Object connectionLock = new Object();
    private final ExecutorService handlerThreadPool = Executors.newFixedThreadPool(8);

    private GenericMessageProcessor messageProcessor;

    public Boolean isRunning() {
        return running;
    }


    /**
     * Constructor for KTimerClient.
     *
     * @param messageEncoder serialization provider for KTimerMessage. from KTimerMessage to json bytes.
     * @param messageDecoder deserialization provider for KTimerMessage. from json bytes to KTimerMessage.
     * @param host           the host of the KTimer server
     * @param port           the port of the KTimer server
     * @param clientId       the unique identifier for the client
     * @param instanceId     the unique identifier for the client instance
     * @param serviceName    the name of the service this client is associated with
     */
    public KTimerClient(
            @NonNull Function<KTimerMessage, byte[]> messageEncoder,
            @NonNull Function<byte[], KTimerMessage> messageDecoder,
            @NonNull String host,
            @NonNull Integer port,
            @NonNull String clientId,
            @NonNull String instanceId,
            @NonNull String serviceName
    ) {
        this.messageEncoder = messageEncoder;
        this.messageDecoder = messageDecoder;
        this.host = host;
        this.port = port;
        this.clientId = clientId;
        this.instanceId = instanceId;
        this.serviceName = serviceName;

        String heartbeatIntervalEnv = System.getenv("KTIMER_HEARTBEAT_INTERVAL");
        if (heartbeatIntervalEnv != null) {
            try {
                this.heartbeatInterval = Integer.parseInt(heartbeatIntervalEnv);
            } catch (NumberFormatException e) {
                log.warn("Invalid heartbeat interval from environment variable, using default: {}", heartbeatInterval);
            }
        }
    }


    private void connectAndRegister() throws IOException {
        socket = new Socket(host, port);
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
        if (response.getType() != MessageType.ACK) {
            throw new IOException(response.getType().toString());
        }
        AckData ackData = (AckData) response.getData();
        if (ackData.getStatus() != AckStatus.SUCCESS) {
            throw new IOException("Failed to register client: " + ackData.getMessage());
        }
    }

    public void start() throws IOException {
        startAsync();
        try {
            shutdownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Starts the client asynchronously without blocking the current thread.
     * This allows you to call scheduleTask() after starting the client.
     */
    public void startAsync() throws IOException {
        if (callbackHandlers.isEmpty()) {
            log.warn("No callback handlers registered. Please register at least one handler before starting the client.");
        }

        messageProcessor = new GenericMessageProcessor(callbackHandlers, handlerThreadPool);

        synchronized (connectionLock) {
            connectAndRegister();
            connected = true;
        }
        startHeartbeatThread();
        startMessageReadLoop();
    }

    /**
     * Waits for the client to shut down.
     */
    public void awaitShutdown() throws InterruptedException {
        shutdownLatch.await();
    }

    /**
     * Registers a callback handler for a specific message type.
     *
     * @param messageType the class of the message type
     * @param handler     the callback handler to register
     * @param <T>         the type of the message
     */
    public <T> void registerCallbackHandler(@NonNull Class<T> messageType, @NonNull CallbackHandler<T> handler) {
        callbackHandlers.put(messageType, handler);
    }

    /**
     * Schedules a task to be executed after the specified delay.
     *
     * @param uniqueTaskID unique identifier for the task, must not be null or empty
     * @param payload task payload data, must not be null
     * @param delay delay in milliseconds before task execution, must be non-negative
     * @param priority task priority, can be null (defaults to NORMAL)
     * @param tags additional metadata tags, can be null
     * @throws IllegalStateException if client is not running or not connected
     * @throws IllegalArgumentException if parameters are invalid
     */
    public void scheduleTask(@NonNull String uniqueTaskID, @NonNull Object payload, @NonNull Long delay,
                           TimerPriority priority, Map<String, String> tags) {
        // Fast parameter validation without synchronization
        validateScheduleTaskParameters(uniqueTaskID, payload, delay);

        // Check connection state with minimal lock time
        ensureClientConnected();

        // Log task scheduling attempt
        if (log.isDebugEnabled()) {
            log.debug("Scheduling task: id={}, delay={}ms, priority={}, tags={}",
                     uniqueTaskID, delay, priority, tags);
        }

        // Create and send task message
        try {
            System.out.println(payload.getClass().getName());
            final var taskData = MessageBuilder.INSTANCE.createTimerRegister(
                clientId, uniqueTaskID, delay, payload, payload.getClass().getCanonicalName(), priority, tags);
            final byte[] encodedMessage = messageEncoder.apply(taskData);

            sendMessage(encodedMessage);

            log.debug("Task scheduled successfully: id={}", uniqueTaskID);
        } catch (IOException e) {
            log.error("Failed to schedule task [{}]: {}", uniqueTaskID, e.getMessage(), e);
            throw new RuntimeException("Failed to schedule task: " + uniqueTaskID, e);
        }
    }

    /**
     * Validates parameters for task scheduling.
     * Fast-fail validation without locks.
     */
    private void validateScheduleTaskParameters(String uniqueTaskID, Object payload, Long delay) {
        if (uniqueTaskID == null || uniqueTaskID.isEmpty()) {
            throw new IllegalArgumentException("uniqueTaskID must not be null or empty");
        }
        if (payload == null) {
            throw new IllegalArgumentException("payload must not be null");
        }
        if (delay == null || delay < 0) {
            throw new IllegalArgumentException("delay must not be null or negative, got: " + delay);
        }
    }

    /**
     * Ensures the client is connected and ready to schedule tasks.
     * Uses minimal synchronization for better performance.
     */
    private void ensureClientConnected() {
        // Quick check without lock for common case
        if (running && connected) {
            return;
        }

        // Double-checked locking pattern for thread safety
        synchronized (connectionLock) {
            if (!running || !connected) {
                throw new IllegalStateException(
                    String.format("Client is not ready (running=%s, connected=%s). Please start the client first.",
                                 running, connected));
            }
        }
    }

    /**
     * Unregisters a callback handler for a specific payload type.
     *
     * @param payloadType the class of the payload type to unregister
     */
    public void unregisterHandler(Class<?> payloadType) {
        callbackHandlers.remove(payloadType);
    }

    private void startMessageReadLoop() {
        Thread messageReadThread = new Thread(() -> {
            while (running) {
                try {
                    var data = receiveMessage();
                    messageProcessor.processMessage(data);
                } catch (Exception e) {
                    var errMsg = MessageBuilder.INSTANCE.createError(clientId, "", e.getLocalizedMessage(), e.toString(), null, null);
                    log.error("Error receiving message: {}", e.getMessage(), e);
                    try {
                        sendMessage(messageEncoder.apply(errMsg));
                    } catch (IOException ex) {
                        if (ex instanceof SocketException) {
                            log.error("Socket error occurred: {}. program will shutdown.", ex.getMessage());
                            running = false;
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        });
        messageReadThread.start();
    }

    private void startHeartbeatThread() {
        var scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            if (!running) {
                scheduler.shutdown();
                return;
            }
            try {
                var clientInfo = new ClientMetadata(
                        socket.getLocalAddress().getHostName(),
                        socket.getLocalAddress().getHostAddress(),
                        socket.getLocalPort(),
                        System.getenv("KTIMER_ENVIRONMENT") != null ? System.getenv("KTIMER_ENVIRONMENT") : "default"
                );
                var heartbeatData = MessageBuilder.INSTANCE.createHeartbeat(clientId, instanceId, serviceName, version, clientInfo);
                sendMessage(messageEncoder.apply(heartbeatData));
            } catch (IOException e) {
                log.error("Error sending heartbeat: {}", e.getMessage());
            }
        }, 0, heartbeatInterval, java.util.concurrent.TimeUnit.MILLISECONDS);
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
