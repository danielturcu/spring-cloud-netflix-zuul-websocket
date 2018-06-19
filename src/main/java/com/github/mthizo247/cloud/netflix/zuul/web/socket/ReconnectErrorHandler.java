package com.github.mthizo247.cloud.netflix.zuul.web.socket;

import com.github.mthizo247.cloud.netflix.zuul.web.util.ErrorAnalyzer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.messaging.simp.stomp.ConnectionLostException;

import java.io.IOException;

/**
 * Created by ronald22 on 06/04/2017.
 */
public class ReconnectErrorHandler implements ProxyWebSocketErrorHandler {
    private int reconnectRetries;
    private long reconnectInterval;
    private final Log logger = LogFactory
            .getLog(ReconnectErrorHandler.class);
    private ErrorAnalyzer errorAnalyzer;

    public ReconnectErrorHandler(ErrorAnalyzer errorAnalyzer, int reconnectRetries, long reconnectInterval) {
        this.errorAnalyzer = errorAnalyzer;
        this.reconnectRetries = reconnectRetries;
        this.reconnectInterval = reconnectInterval;
    }

    @Override
    public void handleError(Throwable t) {
        if (logger.isErrorEnabled()) {
            logger.error("Proxy web socket error occurred.", t);
        }

        if (!(t instanceof ProxySessionException))
            return;

        ProxySessionException exception = (ProxySessionException) t;

        if (shouldAttemptToReconnect(exception)) {
            executeReconnectThread(exception.getConnectionManager());
        }
    }

    private boolean shouldAttemptToReconnect(ProxySessionException proxyException) {
        if (!isConnectionLost(proxyException))
            return false;

        if (!proxyException.getConnectionManager().isConnectedToUserAgent())
            return false;

        return true;
    }

    private boolean isConnectionLost(ProxySessionException proxyException) {
        Throwable cause = proxyException.getCause();
        Throwable[] causeChain = errorAnalyzer.determineCauseChain(cause);
        Throwable throwable = errorAnalyzer
                .getFirstThrowableOfType(ConnectionLostException.class, causeChain);

        if (throwable != null)
            return true;

        throwable = errorAnalyzer.getFirstThrowableOfType(IOException.class,
                causeChain);

        return throwable != null
                && throwable.getMessage().toLowerCase().contains(" refused ");
    }

    private void executeReconnectThread(
            final ProxyWebSocketConnectionManager clientConnectionManager) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                clientConnectionManager.reconnect(reconnectInterval, reconnectRetries);
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
