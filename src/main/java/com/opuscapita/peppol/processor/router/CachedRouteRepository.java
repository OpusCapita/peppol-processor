package com.opuscapita.peppol.processor.router;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CachedRouteRepository extends CrudRepository<CachedRoute, String> {
}