package com.github.mthizo247.cloud.netflix.zuul.web.socket;

public class RateLimitReachedException extends RuntimeException{

    public RateLimitReachedException(String message) {
        super(message);
    }
}
