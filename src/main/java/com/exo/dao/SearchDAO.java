package com.exo.dao;

import com.exo.model.PlanetSummary;
import java.util.List;

public interface SearchDAO {
    List<PlanetSummary> search(String q,
                               Integer yearFrom,
                               Integer yearTo,
                               String method,
                               String facility,
                               int limit,
                               int offset) throws Exception;

    int count(String q,
              Integer yearFrom,
              Integer yearTo,
              String method,
              String facility) throws Exception;
    
    PlanetSummary findById(int planetId) throws Exception;
}