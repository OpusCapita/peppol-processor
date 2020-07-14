package com.opuscapita.peppol.processor.controller;

import com.opuscapita.peppol.processor.router.CachedRouteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProcessorRestController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessorRestController.class);

    private final CachedRouteRepository routeRepository;

    public ProcessorRestController(CachedRouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @GetMapping("/clear-cache/{icd}/{identifier}")
    public void clearCache(@PathVariable String icd, @PathVariable String identifier) throws Exception {
        try {
            routeRepository.deleteById(icd + ":" + identifier);
        } catch (Exception e) {
            logger.error("Failed to clear the cache for: " + icd + ":" + identifier);
        }
    }
}
