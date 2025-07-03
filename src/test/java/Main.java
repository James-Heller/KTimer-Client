
import space.jamestang.ktimer.KTimerClient;
import space.jamestang.ktimer.message.enums.TimerPriority;

import java.io.IOException;

public class Main {


    public static void main(String[] args) throws IOException {

        //Just construct a KTimerClient instance
        KTimerClient client = new KTimerClient("localhost", 8080, "OnlineShop", "001", "Test");

//        Register a callback handler for SomeData
        client.registerCallbackHandler(SomeData.class, new SomeDataCallbackHandler());

        // start the client
        client.startAsync();

        // make a few tasks and wait
        var payload = new SomeData("10086", "test", "hello world", 4396, false);
        client.scheduleTask("OID1", payload, 10000L, TimerPriority.NORMAL, null);
        client.scheduleTask("OID2", payload, 10000L, TimerPriority.NORMAL, null);
        client.scheduleTask("OID3", payload, 10000L, TimerPriority.NORMAL, null);
    }


}
