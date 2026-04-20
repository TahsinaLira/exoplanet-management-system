package com.exo.dao;

import com.exo.model.Paper;
import java.util.List;

public interface PaperDAO {

    List<Paper> findByPlanet(int planetId) throws Exception;

    List<Paper> findAll() throws Exception;

    void addPaperForPlanet(int planetId,
                           String title,
                           String authors,
                           Integer year,
                           String fileName,
                           byte[] fileData,
                           String abstractText,
                           Integer uploadedBy) throws Exception;
    void deletePaper(int paperID) throws Exception;
    
    Paper findFileById(int paperId) throws Exception;

    
}