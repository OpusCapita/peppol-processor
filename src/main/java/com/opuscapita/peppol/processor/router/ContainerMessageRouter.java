package com.opuscapita.peppol.processor.router;

import com.opuscapita.peppol.commons.auth.AuthorizationService;
import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.state.Route;
import com.opuscapita.peppol.commons.container.state.Source;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Component
public class ContainerMessageRouter {

    private final static Logger logger = LoggerFactory.getLogger(ContainerMessageRouter.class);

    private final RestTemplate restTemplate;
    private final AuthorizationService authService;
    private final CachedRouteRepository routeRepository;

    @Autowired
    public ContainerMessageRouter(AuthorizationService authService, RestTemplateBuilder restTemplateBuilder, CachedRouteRepository routeRepository) {
        this.authService = authService;
        this.restTemplate = restTemplateBuilder.build();
        this.routeRepository = routeRepository;
    }

    public Route loadRoute(@NotNull ContainerMessage cm) {
        if (cm.isOutbound()) {
            return new Route(Source.NETWORK);
        }

        String receiver = cm.getCustomerId();
        Optional<CachedRoute> cachedRoute = routeRepository.findById(receiver);
        if (cachedRoute.isPresent()) {
            return new Route(cachedRoute.get().getDestination());
        }

        Source destination = queryBusinessPlatform(cm.getCustomerId());
        routeRepository.save(new CachedRoute(receiver, destination));
        return new Route(destination);
    }

    private Source queryBusinessPlatform(String customerId) {
        String endpoint = getEndpoint(customerId);
        logger.debug("Sending get-route request to endpoint: " + endpoint + " for customer: " + customerId);

        HttpHeaders headers = new HttpHeaders();
        authService.setAuthorizationHeader(headers);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        try {
            ResponseEntity<Source> result = restTemplate.exchange(endpoint, HttpMethod.GET, entity, Source.class);
            Source route = result.getBody();
            logger.info("Fetched route as " + route + " for customer: " + customerId);
            return route;
        } catch (Exception e) {
            logger.error("Error occurred while trying to query the ROUTE for file: " + customerId, e);
            return Source.A2A;
        }
    }

    private String getEndpoint(String customerId) {
        String[] result = customerId.split(":");
        return UriComponentsBuilder
                .fromUriString("http://peppol-smp")
                .port(3045)
                .path("/api/get-business-platform/" + result[0] + "/" + result[1])
                .toUriString();
    }
}
