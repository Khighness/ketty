package top.parak.ketty.concurrent;

import java.io.IOException;
import java.util.EventListener;
import java.util.concurrent.Future;

/**
 * Listen to the result of a {@link Future}.
 *
 * @author Khighness
 * @since 2023-12-15
 */
public interface GenericListener<P extends Promise<?>> extends EventListener {

    /**
     * Invoked when the operation associated with the {@link Future} has been completed.
     *
     * @param promise the source {@link Promise} which called this callback
     */
    void operationComplete(P promise);

}
