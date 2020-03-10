package com.shatrix.covid19tracker;

public class CountryLine {

    public String countryName, cases, recovered, deaths;
    public CountryLine(String countryName, String cases, String recovered, String deaths) {
        super();
        this.countryName = countryName;
        this.cases = cases;
        this.recovered = recovered;
        this.deaths = deaths;
    }
    public String getCountryName() {
        return countryName;
    }
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCases() {
        return cases;
    }
    public void setCases(String cases) {
        this.cases = cases;
    }

    public String getRecovered() {
        return recovered;
    }
    public void setRecovered(String recovered) {
        this.recovered = recovered;
    }

    public String getDeaths() {
        return deaths;
    }
    public void setDeaths(String deaths) {
        this.deaths = deaths;
    }
}
