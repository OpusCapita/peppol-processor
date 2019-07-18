package com.opuscapita.peppol.processor.router;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@RefreshScope
public class SiriusRoutingConfiguration {

    @Value("${routing.sirius-config:''}")
    private String rawConfig;

    private Set<String> receivers;

    public SiriusRoutingConfiguration() {
        this.receivers = new HashSet<>();
    }

    @PostConstruct
    public void extractReceivers() {
        String[] a = rawConfig.split(",\n");
        this.receivers.addAll(Arrays.asList(a));
    }

    public boolean isSiriusReceiver(String receiver) {
        return this.receivers.contains(receiver);
    }
}
