package com.opuscapita.peppol.processor.router;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.state.Route;
import com.opuscapita.peppol.commons.container.state.Source;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContainerMessageRouter {

    private final static Logger logger = LoggerFactory.getLogger(ContainerMessageRouter.class);

    private final RoutingConfiguration routingConfiguration;
    private final SiriusRoutingConfiguration siriusRoutingConfiguration;

    @Autowired
    public ContainerMessageRouter(RoutingConfiguration routingConfiguration, SiriusRoutingConfiguration siriusRoutingConfiguration) {
        this.routingConfiguration = routingConfiguration;
        this.siriusRoutingConfiguration = siriusRoutingConfiguration;
    }

    public Route loadRoute(@NotNull ContainerMessage cm) {
        Source source = cm.getSource();

        for (Route route : routingConfiguration.getRoutes()) {
            if (route.getSource().equals(source.name().toLowerCase())) {
                if (route.getDestination().equals("sirius")) {
                    if (siriusRoutingConfiguration.isSiriusReceiver(cm.getCustomerId())) {
                        logger.debug("Route selected by receiver for the file: " + cm.getFileName());
                        return new Route(route);
                    }
                } else if (route.getMask() != null) {
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

        cm.getHistory().addError("Cannot define route for file coming from " + source);
        return null;
    }
}
