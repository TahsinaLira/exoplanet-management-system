package com.exo.model;

public class PlanetSummary {

    private int planetId;
    private String planet;
    private String hostStar;
    private Integer discYear;
    private String methodName;
    private String facilityName;
    private Double distanceParsec;
    private Double orbitalPeriodDays;
    private Double massEarth;
  
    
    public double getMassEarth() {
        return massEarth;
    }
    public void setMassEarth(double massEarth) {
        this.massEarth = massEarth;
    }
    // --- Getters and Setters ---
    public int getPlanetId() {
        return planetId;
    }
    public void setPlanetId(int planetId) {
        this.planetId = planetId;
    }

    public String getPlanet() {
        return planet;
    }
    public void setPlanet(String planet) {
        this.planet = planet;
    }

    public String getHostStar() {
        return hostStar;
    }
    public void setHostStar(String hostStar) {
        this.hostStar = hostStar;
    }

    public Integer getDiscYear() {
        return discYear;
    }
    public void setDiscYear(Integer discYear) {
        this.discYear = discYear;
    }

    public String getMethodName() {
        return methodName;
    }
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getFacilityName() {
        return facilityName;
    }
    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public Double getDistanceParsec() {
        return distanceParsec;
    }
    public void setDistanceParsec(Double distanceParsec) {
        this.distanceParsec = distanceParsec;
    }

    public Double getOrbitalPeriodDays() {
        return orbitalPeriodDays;
    }
    public void setOrbitalPeriodDays(Double orbitalPeriodDays) {
        this.orbitalPeriodDays = orbitalPeriodDays;
    }
    private String imagePath;
public String getImagePath() { return imagePath; }
public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}