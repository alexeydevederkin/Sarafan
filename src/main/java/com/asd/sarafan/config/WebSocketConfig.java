package com.asd.sarafan.config;

import com.asd.sarafan.domain.User;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue", "/user");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry config) {
        config.addEndpoint("/sarafan-websocket")
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request,
                                                      WebSocketHandler wsHandler,
                                                      Map<String, Object> attributes) {

                        // Making "username" for websockets = id of user
                        // by default username for websockets == principal.toString() == user.toString()
                        //   e.g. username = "User(id=100008869919267898123, name=a b)"

                        OAuth2Authentication oauth2 = (OAuth2Authentication) request.getPrincipal();

                        User user = (User) (oauth2 == null ? null : oauth2.getPrincipal());

                        final String userId = user == null ? "0" : user.getId();

                        return new Principal() {
                            @Override
                            public String getName() {
                                return userId;
                            }
                        };
                    }
                })
                .withSockJS();
    }
}
