
import space.jamestang.ktimer.KTimerClient;
import space.jamestang.ktimer.heartbeat.HeartbeatCallbackHandler;
import space.jamestang.ktimer.message.HeartbeatData;
import space.jamestang.ktimer.message.enums.TimerPriority;

import java.io.IOException;

public class Main {


    public static void main(String[] args) throws IOException {

        KTimerClient client = getKTimerClient();
        client.registerCallbackHandler(HeartbeatData.class, new HeartbeatCallbackHandler());
        client.registerCallbackHandler(SomeData.class, new SomeDataCallbackHandler());
        client.startAsync();

        var payload = new SomeData("10086", "test", "hello world", 4396, false);
        client.scheduleTask("OID1", payload, 10000L, TimerPriority.NORMAL, null);
    }

    private static KTimerClient getKTimerClient() {
        return new KTimerClient("localhost", 8080, "T1", "001", "Test");
    }
}
