package space.jamestang.ktimer.client;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import space.jamestang.ktimer.client.datatype.KTimerMessage;
import space.jamestang.ktimer.client.datatype.MessageType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@Slf4j
public class KTimerClient {
    private final String host;
    private final int port;
    private String clientId;
    private Socket connection;
    private DataInputStream input;
    private DataOutputStream output;
    private final Function<KTimerMessage, byte[]> messageEncoder;
    private final Function<byte[], KTimerMessage> messageDecoder;
    private final ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    // -- SETTER --
    // 当接收到服务器消息时，将调用该方法处理消息
    @Setter
    private KTimerCallBackHandler handler;

    public KTimerClient(String host, int port, Function<KTimerMessage, byte[]> msgToJsonBytes, Function<byte[], KTimerMessage> jsonBytesToMsg) {
        this.host = host;
        this.port = port;
        this.messageEncoder = msgToJsonBytes;
        this.messageDecoder = jsonBytesToMsg;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void connect() {
        try {
            connection = new Socket(host, port);
            input = new DataInputStream(connection.getInputStream());
            output = new DataOutputStream(connection.getOutputStream());

            Path clientIdPath = Path.of("clientId.txt");
            if (clientId == null) {
                if (Files.exists(clientIdPath)) {
                    clientId = Files.readString(clientIdPath);
                } else {
                    registry();
                }
            }
            startMessageListener();
        } catch (IOException e) {
            log.error("Failed to connect to server: {}", e.getMessage(), e);
        }
    }

    public void sendTask(KTimerMessage message) {
        if (message.getClientId() == null) {
            message.setClientId(clientId);
        }
        byte[] payload = messageEncoder.apply(message);
        try {
            output.writeInt(payload.length);
            output.write(payload);
            output.flush();
        } catch (IOException e) {
            log.error("Failed to send task: {}", e.getMessage(), e);
        }
    }

    private void registry() {
        var msg = KTimerMessage.createClientRegisterMessage();
        var payload = messageEncoder.apply(msg);
        try {
            output.writeInt(payload.length);
            output.write(payload);
            output.flush();

            int length = input.readInt();
            byte[] buffer = new byte[length];
            input.readFully(buffer);
            var response = messageDecoder.apply(buffer);
            if (response.getType() == MessageType.TASK_RECEIVED) {
                clientId = response.getClientId();
                // 统一使用 writeString 写入（支持文件不存在时自动创建）
                Files.writeString(Path.of("clientId.txt"), clientId, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                log.info("Client registered successfully with ID: {}", clientId);
            } else {
                log.error("Failed to register client: {}", response);
            }
        } catch (IOException e) {
            throw new RuntimeException("Registry error", e);
        }
    }

    private void startMessageListener() {
        running.set(true);
        executor.submit(() -> {
            try {
                while (running.get() && !connection.isClosed()) {
                    int length = input.readInt();
                    byte[] buffer = new byte[length];
                    input.readFully(buffer);

                    KTimerMessage payload = messageDecoder.apply(buffer);
                    log.info("Message received: {}", payload);
                    if (handler != null) {
                        switch (payload.getType()) {
                            case HEARTBEAT -> handler.onReceiveHeartbeat(payload);
                            case TASK_TRIGGER -> {
                                KTimerMessage response = handler.onTaskTrigger(payload);
                                if (response != null) {
                                    byte[] responseBytes = messageEncoder.apply(response);
                                    output.writeInt(responseBytes.length);
                                    output.write(responseBytes);
                                    output.flush();
                                }
                            }
                            case ERROR -> handler.onException(payload);
                            default -> log.warn("Unhandled message type: {}", payload.getType());
                        }
                    } else {
                        log.warn("No message handler set, ignoring message: {}", payload);
                    }
                }
            } catch (IOException e) {
                if (running.get()) {
                    log.error("Error while reading message: {}", e.getMessage(), e);
                }
            } finally {
                if (running.get()) {
                    log.error("Connection to server lost, attempting to reconnect...");
                    reconnect();
                }
            }
        });
    }

    private void reconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            connect();
        } catch (IOException e) {
            log.error("Failed to reconnect to server: {}", e.getMessage(), e);
        }
    }
}