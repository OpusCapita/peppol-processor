package com.opuscapita.peppol.processor.router;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.state.Route;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContainerMessageRouter {

    private final static Logger logger = LoggerFactory.getLogger(ContainerMessageRouter.class);

    private final RoutingConfiguration routingConfiguration;

    @Autowired
    public ContainerMessageRouter(RoutingConfiguration routingConfiguration) {
        this.routingConfiguration = routingConfiguration;
    }

    public Route loadRoute(@NotNull ContainerMessage cm) {
        String source = cm.getSourceEndpoint().getName();

        for (Route route : routingConfiguration.getRoutes()) {
            if (route.getSource().equals(source)) {
                if (route.getMask() != null) {
                    if (cm.getMetadata().getRecipientId().matches(route.getMask())) {
                        logger.debug("Route selected by source and mask for the file: " + cm.getFileName());
                        return new Route(route);
                    }
                } else {
                    logger.debug("Route selected by source for the file: " + cm.getFileName());
                    return new Route(route);
                }
            }
        }

        cm.setProcessingException("Cannot define route for " + cm.toLog() + " originated from " + source);
        return null;
    }

}
