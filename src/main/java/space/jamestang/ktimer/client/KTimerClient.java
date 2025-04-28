package space.jamestang.ktimer.client;

import lombok.extern.slf4j.Slf4j;
import space.jamestang.ktimer.client.datatype.KTimerMessage;
import space.jamestang.ktimer.client.datatype.MessageType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

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
    private ExecutorService executor;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private Supplier<KTimerMessage> responseSupplier;


    public KTimerClient(String host, int port, Function<KTimerMessage, byte[]> msgToJsonBytes, Function<byte[], KTimerMessage> jsonBytesToMsg) {
        this.host = host;
        this.port = port;
        this.messageEncoder = msgToJsonBytes;
        this.messageDecoder = jsonBytesToMsg;
    }



    public void connect() {
        try {
            connection = new Socket(host, port);
            input = new DataInputStream(connection.getInputStream());
            output = new DataOutputStream(connection.getOutputStream());

            if (clientId == null){
                try {
                    clientId = Files.readString(Path.of("clientId.txt"));
                }catch (IOException e){
                    registry();
                }
            }
        } catch (IOException e) {
            if (log.isDebugEnabled()){
                e.printStackTrace();
            }else{
                log.error("Failed to connect to server: {}", e.getMessage());
            }
        }
    }

    private void registry(){

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
                Path clientIdFile = Path.of("clientId.txt");
                if (!Files.exists(clientIdFile)){
                    Files.createFile(clientIdFile);
                    Files.writeString(Path.of("clientId.txt"), clientId);
                }

                log.info("Client registered successfully with ID: {}", clientId);
            } else {
                log.error("Failed to register client: {}", response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public DataInputStream getInput() {
        if (this.input == null) {
            throw new IllegalStateException("The input stream has not been initialized yet.");
        }
        return input;
    }

    public DataOutputStream getOutput() {
        if (this.output == null) {
            throw new IllegalStateException("The output stream has not been initialized yet.");
        }
        return output;
    }
}
