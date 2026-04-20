package com.exo.model;

import java.sql.Timestamp;

public class Paper {

    private Integer paperId;
    private String title;
    private String authors;
    private Integer year;

    private String abstractText;

    private String fileName;     // matches column file_name
    private byte[] fileData;     // matches column file_data

    private Integer uploadedBy;
    private Timestamp uploadedAt;

    // extra for admin / UI
    private Integer planetId;
    private String planetName;

    // ----------- getters / setters -----------

    public Integer getPaperId() {
        return paperId;
    }

    public void setPaperId(Integer paperId) {
        this.paperId = paperId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {   // ✅ fixed
        this.fileName = fileName;
    }

    public byte[] getFileData() {               // ✅ new
        return fileData;
    }

    public void setFileData(byte[] fileData) {  // ✅ new
        this.fileData = fileData;
    }

    public Integer getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(Integer uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Timestamp getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Timestamp uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Integer getPlanetId() {
        return planetId;
    }

    public void setPlanetId(Integer planetId) {
        this.planetId = planetId;
    }

    public String getPlanetName() {
        return planetName;
    }

    public void setPlanetName(String planetName) {
        this.planetName = planetName;
    }
}