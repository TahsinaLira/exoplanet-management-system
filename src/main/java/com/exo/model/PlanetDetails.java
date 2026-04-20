package com.exo.model;

public class PlanetDetails {

    private Integer planetId;
    private String planet;
    private String hostStar;

    private Double mass;
    private Double radius;
    private Double temp;

    private Integer discYear;
    private String method;
    private String facility;

    private Double insolation;
    private Boolean controversial;

    private Double period;
    private Double semimajorAxis;
    private Double eccentricity;

    private Double distance;
    private String spectralType;
    private Double starTemp;

    // --------- Getters & Setters ----------

    public Integer getPlanetId() { return planetId; }
    public void setPlanetId(Integer planetId) { this.planetId = planetId; }

    public String getPlanet() { return planet; }
    public void setPlanet(String planet) { this.planet = planet; }

    public String getHostStar() { return hostStar; }
    public void setHostStar(String hostStar) { this.hostStar = hostStar; }

    public Double getMass() { return mass; }
    public void setMass(Double mass) { this.mass = mass; }

    public Double getRadius() { return radius; }
    public void setRadius(Double radius) { this.radius = radius; }

    public Double getTemp() { return temp; }
    public void setTemp(Double temp) { this.temp = temp; }

    public Integer getDiscYear() { return discYear; }
    public void setDiscYear(Integer discYear) { this.discYear = discYear; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getFacility() { return facility; }
    public void setFacility(String facility) { this.facility = facility; }

    public Double getInsolation() { return insolation; }
    public void setInsolation(Double insolation) { this.insolation = insolation; }

    public Boolean getControversial() { return controversial; }
    public void setControversial(Boolean controversial) { this.controversial = controversial; }

    public Double getPeriod() { return period; }
    public void setPeriod(Double period) { this.period = period; }

    public Double getSemimajorAxis() { return semimajorAxis; }
    public void setSemimajorAxis(Double semimajorAxis) { this.semimajorAxis = semimajorAxis; }

    public Double getEccentricity() { return eccentricity; }
    public void setEccentricity(Double eccentricity) { this.eccentricity = eccentricity; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public String getSpectralType() { return spectralType; }
    public void setSpectralType(String spectralType) { this.spectralType = spectralType; }

    public Double getStarTemp() { return starTemp; }
    public void setStarTemp(Double starTemp) { this.starTemp = starTemp; }

}