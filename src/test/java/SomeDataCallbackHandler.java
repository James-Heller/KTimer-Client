import lombok.extern.slf4j.Slf4j;
import space.jamestang.ktimer.core.CallbackHandler;
import space.jamestang.ktimer.message.TimerCallbackData;

@Slf4j
public class SomeDataCallbackHandler implements CallbackHandler<SomeData> {
    @Override
    public void onCallback(SomeData payload, TimerCallbackData raw) {
        log.info("回调数据: {}", payload);

    }
}
