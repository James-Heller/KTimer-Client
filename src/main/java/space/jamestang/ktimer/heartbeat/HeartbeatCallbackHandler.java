package space.jamestang.ktimer.heartbeat;

import lombok.extern.slf4j.Slf4j;
import space.jamestang.ktimer.core.CallbackHandler;
import space.jamestang.ktimer.message.HeartbeatData;
import space.jamestang.ktimer.message.TimerCallbackData;

@Slf4j
public class HeartbeatCallbackHandler implements CallbackHandler<HeartbeatData> {

    @Override
    public void onCallback(HeartbeatData payload, TimerCallbackData raw) {
        log.trace("Heartbeat callback received: {}", payload);
    }
}