# spring-cloud-netflix-zuul-websocket
A simple library to enable Zuul reverse proxy web socket support in spring applications.
The library has rate limit support using the https://github.com/marcosbarbero/spring-cloud-zuul-ratelimit library.
The rate limit support is optional and can be disabled.

## USAGE

spring-cloud-netflix-zuul-websocket is available from **Maven Central**

```xml
<dependency>
  <groupId>com.github.mthizo247</groupId>
  <artifactId>spring-cloud-netflix-zuul-websocket</artifactId>
  <version>1.0.8-RELEASE</version>
</dependency>
```

### Who is this for?

This is for anyone using **Spring Netflix Zuul** to proxy requests to back-ends which supports web sockets but the proxy layer does not.
**Netflix Zuul** does not natively support web sockets.

### How do I use this?

Enable it like so:

```java
@SpringBootApplication
@EnableZuulWebSocket
@EnableWebSocketMessageBroker
public class ProxyApplication {
	public static void main(String[] args) {
		SpringApplication.run(ProxyApplication.class, args);
	}
}
```

Then in your spring application properties(e.g application.yml)

```
server:
  port: 7078

zuul:
   routes:
    hello:
      path: /**
      url: http://localhost:7079
      customSensitiveHeaders: true
  ratelimit:
    key-prefix: your-prefix
    enabled: true
    repository: REDIS
    behind-proxy: true
    default-policy-list: #optional - will apply unless specific policy exists
      - limit: 10 #optional - request number limit per refresh interval window
        quota: 1000 #optional - request time limit per refresh interval window (in seconds)
        refresh-interval: 60 #default value (in seconds)
        type: #optional
          - user
          - origin
          - url
    policy-list:
      hello:
        - limit: 10 #optional - request number limit per refresh interval window
          quota: 1000 #optional - request time limit per refresh interval window (in seconds)
          refresh-interval: 60 #default value (in seconds)
          type: #optional
            - user
            - origin
            - url
        - type: #optional value for each type
            - user=anonymous
            - origin=somemachine.com
            - url=/api #url prefix
            - role=user
   ws:
      brokerages:
        hello:
          end-points: /ws
          brokers:  /topic
          destination-prefixes: /app
      reconnectRetries: 2
      reconnectInterval: 10000
```

With this you should have web sockets to your back-end service working correctly.

**Checkout** this [demo](https://github.com/mthizo247/zuul-websocket-support-demo)
