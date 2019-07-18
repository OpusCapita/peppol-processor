package com.opuscapita.peppol.processor.router;

import com.opuscapita.peppol.commons.container.state.Route;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "peppol.routing-config")
public class RoutingConfiguration {

    private List<Route> routes = new ArrayList<>();

    public List<Route> getRoutes() {
        return routes;
    }

    public Route getRoute(String destination) {
        return routes.stream().filter(r -> r.getDestination().equals(destination)).findFirst().orElse(null);
    }

}
