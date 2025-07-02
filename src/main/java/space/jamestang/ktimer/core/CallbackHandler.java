package space.jamestang.ktimer.core;

import space.jamestang.ktimer.message.TimerCallbackData;

public interface CallbackHandler<T> {

    /**
     * Handles the callback with the given payload and raw data.
     * @param payload the data you sent to timer before.
     * @param raw the raw data received from the timer. In case you need to implement your own logic based on the raw data.
     */
    void onCallback(T payload, TimerCallbackData raw);
}
