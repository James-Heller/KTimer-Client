package space.jamestang.ktimer.client;

import space.jamestang.ktimer.client.datatype.KTimerMessage;

public abstract class KTimerCallBackHandler {

    void onReceiveHeartbeat(KTimerMessage message) {
//        TODO not implemented yet
    }

    public abstract KTimerMessage onTaskTrigger(KTimerMessage message);

    public void onException(KTimerMessage message) {

        throw new RuntimeException(message.toString());
    }

}
