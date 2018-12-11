/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.mthizo247.cloud.netflix.zuul.web.socket;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.github.mthizo247.cloud.netflix.zuul.web.proxytarget.ProxyTargetResolver;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.Rate;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitKeyGenerator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimiter;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties.Policy.MatchType;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.util.Assert;
import org.springframework.util.ErrorHandler;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * A {@link WebSocketHandlerDecorator} that adds web socket support to zuul reverse proxy.
 *
 * @author Ronald Mthombeni
 * @author Salman Noor
 */
public class ProxyWebSocketHandler extends WebSocketHandlerDecorator {

    private final Logger logger = LoggerFactory.getLogger(ProxyWebSocketHandler.class);
    private final WebSocketHttpHeadersCallback headersCallback;
    private final SimpMessagingTemplate messagingTemplate;
    private final ProxyTargetResolver proxyTargetResolver;
    private final ZuulWebSocketProperties zuulWebSocketProperties;
    private final WebSocketStompClient stompClient;
    private final RateLimitProperties properties;
    private final RouteLocator routeLocator;
    private final RateLimitUtils rateLimitUtils;
    private final RateLimiter rateLimiter;
    private final RateLimitKeyGenerator rateLimitKeyGenerator;
    private final Map<WebSocketSession, ProxyWebSocketConnectionManager> managers = new ConcurrentHashMap<>();
    private ErrorHandler errorHandler;

    public ProxyWebSocketHandler(WebSocketHandler delegate, //NOSONAR
        WebSocketStompClient stompClient,
        WebSocketHttpHeadersCallback headersCallback,
        SimpMessagingTemplate messagingTemplate,
        ProxyTargetResolver proxyTargetResolver,
        ZuulWebSocketProperties zuulWebSocketProperties,
        RateLimitProperties properties, RouteLocator routeLocator,
        RateLimitUtils rateLimitUtils, RateLimiter rateLimiter,
        RateLimitKeyGenerator rateLimitKeyGenerator) {
        super(delegate);
        this.stompClient = stompClient;
        this.headersCallback = headersCallback;
        this.messagingTemplate = messagingTemplate;
        this.proxyTargetResolver = proxyTargetResolver;
        this.zuulWebSocketProperties = zuulWebSocketProperties;
        this.properties = properties;
        this.routeLocator = routeLocator;
        this.rateLimitUtils = rateLimitUtils;
        this.rateLimiter = rateLimiter;
        this.rateLimitKeyGenerator = rateLimitKeyGenerator;
    }

    public void errorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    private String getWebSocketServerPath(ZuulWebSocketProperties.WsBrokerage wsBrokerage,
        URI uri) {
        String path = uri.toString();
        if (path.contains(":")) {
            path = UriComponentsBuilder.fromUriString(path).build().getPath();
        }

        for (String endPoint : wsBrokerage.getEndPoints()) {
            if (PatternMatchUtils.simpleMatch(toPattern(endPoint), path + "/")) {
                return endPoint;
            }
        }

        return null;
    }

    private ZuulWebSocketProperties.WsBrokerage getWebSocketBrokarage(URI uri) {
        String path = uri.toString();
        if (path.contains(":")) {
            path = UriComponentsBuilder.fromUriString(path).build().getPath();
        }

        for (Map.Entry<String, ZuulWebSocketProperties.WsBrokerage> entry : zuulWebSocketProperties
            .getBrokerages().entrySet()) {
            ZuulWebSocketProperties.WsBrokerage wsBrokerage = entry.getValue();
            if (wsBrokerage.isEnabled()) {
                for (String endPoint : wsBrokerage.getEndPoints()) {
                    if (PatternMatchUtils.simpleMatch(toPattern(endPoint), path + "/")) {
                        return wsBrokerage;
                    }
                }
            }
        }

        return null;
    }

    private String toPattern(String path) {
        path = path.startsWith("/") ? "**" + path : "**/" + path;
        return path.endsWith("/") ? path + "**" : path + "/**";
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus)
        throws Exception {
        disconnectFromProxiedTarget(session);
        super.afterConnectionClosed(session, closeStatus);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message)
        throws Exception {
        super.handleMessage(session, message);
        handleMessageFromClient(session, message);
    }

    private void handleMessageFromClient(WebSocketSession session,
        WebSocketMessage<?> message) {
        boolean handled = false;
        WebSocketMessageAccessor accessor = WebSocketMessageAccessor.create(message);
        if (StompCommand.SEND.toString().equalsIgnoreCase(accessor.getCommand())) {
            handled = true;
            logger.info("Handling SEND event for session with id '{}'", session.getId());
            sendMessageToProxiedTarget(session, accessor);
        }

        if (StompCommand.SUBSCRIBE.toString().equalsIgnoreCase(accessor.getCommand())) {
            handled = true;
            logger.info("Handling SUBSCRIBE event for session with id '{}'", session.getId());
            subscribeToProxiedTarget(session, accessor);
        }

        if (StompCommand.UNSUBSCRIBE.toString().equalsIgnoreCase(accessor.getCommand())) {
            handled = true;
            logger.info("Handling UNSUBSCRIBE event for session with id '{}'", session.getId());
            unsubscribeFromProxiedTarget(session, accessor);
        }

        if (StompCommand.CONNECT.toString().equalsIgnoreCase(accessor.getCommand())) {
            handled = true;
            logger.info("Handling CONNECT event for session with id '{}'", session.getId());
            connectToProxiedTarget(session);
        }

        if (!handled && logger.isDebugEnabled()) {
            logger.debug("STOMP COMMAND '{}' was not explicitly handled", accessor.getCommand());
        }
    }

    private List<Policy> policy(Route route, HttpServletRequest request) {
        String routeId = Optional.ofNullable(route).map(Route::getId).orElse(null);
        return properties.getPolicies(routeId).stream()
            .filter(policy -> applyPolicy(request, route, policy))
            .collect(Collectors.toList());
    }

    private boolean applyPolicy(HttpServletRequest request, Route route, Policy policy) {
        List<MatchType> types = policy.getType();
        return types.isEmpty() || types.stream().allMatch(type -> type.apply(request, route, rateLimitUtils));
    }

    private Route route(WebSocketSession session) {
        return routeLocator.getMatchingRoute(session.getUri().getPath());
    }

    private void connectToProxiedTarget(WebSocketSession session) {

        try {
            checkRateLimitReached(session);
        } catch (RateLimitReachedException e) {
            logger.debug("RateLimit reached for session '{}'", session, e);
            disconnectFromProxiedTarget(session);
            closeSession(session);
            return;
        }

        URI sessionUri = session.getUri();
        ZuulWebSocketProperties.WsBrokerage wsBrokerage = getWebSocketBrokarage(
            sessionUri);

        Assert.notNull(wsBrokerage, "wsBrokerage must not be null");

        String path = getWebSocketServerPath(wsBrokerage, sessionUri);
        Assert.notNull(path, "Web socket uri path must be null");

        URI routeTarget = proxyTargetResolver.resolveTarget(wsBrokerage);

        Assert.notNull(routeTarget, "routeTarget must not be null");

        String uri = ServletUriComponentsBuilder
            .fromUri(routeTarget)
            .path(path)
            .replaceQuery(sessionUri.getQuery())
            .toUriString();

        ProxyWebSocketConnectionManager connectionManager = new ProxyWebSocketConnectionManager(
            proxyTargetResolver,
            wsBrokerage,
            path,
            sessionUri.getQuery(),
            messagingTemplate, stompClient, session, headersCallback, uri);
        connectionManager.errorHandler(this.errorHandler);
        managers.put(session, connectionManager);
        connectionManager.start();
    }

    private void checkRateLimitReached(WebSocketSession session) {
        if (properties == null || !properties.isEnabled()) {
            return;
        }
        HttpServletRequest request = HttpServletRequestBuilder.request(session.getRemoteAddress(), session.getHandshakeHeaders());
        final Route route = route(session);
            policy(route, request).forEach(policy -> {
                final String key = rateLimitKeyGenerator.key(request, route, policy);
                final Rate rate = rateLimiter.consume(policy, key, null);
                final String httpHeaderKey = key.replaceAll("[^A-Za-z0-9-.]", "_").replaceAll("__", "_");

                final Long limit = policy.getLimit();
                final Long remaining = rate.getRemaining();
                if (limit != null) {
                    logger.info("Limit for '{}' = '{}'", httpHeaderKey, String.valueOf(limit));
                    logger.info("Remaining Limit for '{}' = '{}'", httpHeaderKey, String.valueOf(Math.max(remaining, 0)));
                }

                final Long quota = policy.getQuota();
                final Long remainingQuota = rate.getRemainingQuota();
                if (quota != null) {
                    logger.info("Quota for '{}' = '{}'", httpHeaderKey, String.valueOf(quota));
                    logger.info("Reaming Quota for '{}' = '{}'", httpHeaderKey, String.valueOf(MILLISECONDS.toSeconds(Math.max(remainingQuota, 0))));
                }

                logger.info("Reset for '{}' = '{}'", httpHeaderKey, String.valueOf(rate.getReset()));

                if ((limit != null && remaining < 0) || (quota != null && remainingQuota < 0)) {
                    throw new RateLimitReachedException("Rate Limit Exceeded for session with id: " + session.getId());
                }
            });
    }

    private void disconnectFromProxiedTarget(WebSocketSession session) {
        disconnectProxyManager(managers.remove(session));
    }

    private void disconnectProxyManager(ProxyWebSocketConnectionManager proxyManager) {
        if (proxyManager != null) {
            try {
                proxyManager.disconnect();
            } catch (Exception ignored) {
                // nothing
            }
        }
    }

    private void unsubscribeFromProxiedTarget(WebSocketSession session, WebSocketMessageAccessor accessor) {
        ProxyWebSocketConnectionManager manager = managers.get(session);
        if (manager != null) {
            manager.unsubscribe(accessor.getDestination());
        } else {
            closeSession(session);
        }
    }

    private void sendMessageToProxiedTarget(WebSocketSession session, WebSocketMessageAccessor accessor) {
        ProxyWebSocketConnectionManager manager = managers.get(session);
        if (manager != null) {
            manager.sendMessage(accessor.getDestination(), accessor.getPayload());
        } else {
            closeSession(session);
        }
    }

    private void subscribeToProxiedTarget(WebSocketSession session, WebSocketMessageAccessor accessor) {
        ProxyWebSocketConnectionManager manager = managers.get(session);
        if (manager != null) {
            manager.subscribe(accessor.getDestination());
        } else {
            closeSession(session);
        }
    }

    private void closeSession(WebSocketSession session) {
        if (session.isOpen()) {
            try {
                logger.debug("Closing session with id '{}'", session.getId());
                session.close(CloseStatus.SERVER_ERROR);
            } catch (Exception ignored) {
                logger.error("Unable to close session with id '{}' due to error", session.getId(), ignored);
            }
        }
    }
}
