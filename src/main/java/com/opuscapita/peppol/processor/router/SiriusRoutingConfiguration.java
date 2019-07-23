package com.opuscapita.peppol.processor.router;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class SiriusRoutingConfiguration {

    @Value("${routing.sirius-config:''}")
    private String siriusConfig;

    public boolean isSiriusReceiver(String receiver) {
        return this.siriusConfig.contains(receiver);
    }
}
