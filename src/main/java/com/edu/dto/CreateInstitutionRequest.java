package com.edu.dto;

import java.util.Objects;

public class CreateInstitutionRequest {
    private String name;
    private String code;
    private String website;
    private String officialEmail;
    private String city;
    private String state;
    private String country;

    public CreateInstitutionRequest() {}

    public CreateInstitutionRequest(String name, String code, String website, String officialEmail, String city, String state, String country) {
        this.name = name;
        this.code = code;
        this.website = website;
        this.officialEmail = officialEmail;
        this.city = city;
        this.state = state;
        this.country = country;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getOfficialEmail() {
        return officialEmail;
    }

    public void setOfficialEmail(String officialEmail) {
        this.officialEmail = officialEmail;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateInstitutionRequest that = (CreateInstitutionRequest) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(code, that.code) &&
               Objects.equals(website, that.website) &&
               Objects.equals(officialEmail, that.officialEmail) &&
               Objects.equals(city, that.city) &&
               Objects.equals(state, that.state) &&
               Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, code, website, officialEmail, city, state, country);
    }

    @Override
    public String toString() {
        return "CreateInstitutionRequest{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", website='" + website + '\'' +
                ", officialEmail='" + officialEmail + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
