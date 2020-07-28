package com.opuscapita.peppol.processor.router;

import com.opuscapita.peppol.commons.container.state.Source;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash(value = "CachedRoute", timeToLive = 86400)
public class CachedRoute implements Serializable {

    private String id;
    private Source destination;

    public CachedRoute() {

    }

    public CachedRoute(String id, Source destination) {
        this.id = id;
        this.destination = destination;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Source getDestination() {
        return destination;
    }

    public void setDestination(Source destination) {
        this.destination = destination;
    }
}