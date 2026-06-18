package com.apitest.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User — request/response model for the /users endpoint.
 * {@code @JsonInclude(NON_NULL)} ensures null fields are omitted
 * in serialisation (useful for "missing field" negative tests).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("username")
    private String username;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("website")
    private String website;

    @JsonProperty("address")
    private Address address;

    @JsonProperty("company")
    private Company company;

    // ── Constructors ─────────────────────────────────────────────────────────

    public User() {}

    /** Minimal constructor for positive-path POST/PUT tests. */
    public User(String name, String email, String username) {
        this.name     = name;
        this.email    = email;
        this.username = username;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Integer getId()       { return id; }
    public void    setId(Integer id) { this.id = id; }

    public String getName()      { return name; }
    public void   setName(String name) { this.name = name; }

    public String getEmail()     { return email; }
    public void   setEmail(String email) { this.email = email; }

    public String getUsername()  { return username; }
    public void   setUsername(String username) { this.username = username; }

    public String getPhone()     { return phone; }
    public void   setPhone(String phone) { this.phone = phone; }

    public String getWebsite()   { return website; }
    public void   setWebsite(String website) { this.website = website; }

    public Address getAddress()  { return address; }
    public void    setAddress(Address address) { this.address = address; }

    public Company getCompany()  { return company; }
    public void    setCompany(Company company) { this.company = company; }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }

    // ── Nested models ─────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Address {
        private String street;
        private String suite;
        private String city;
        private String zipcode;

        public String getStreet()  { return street; }
        public void   setStreet(String street) { this.street = street; }
        public String getSuite()   { return suite; }
        public void   setSuite(String suite)   { this.suite = suite; }
        public String getCity()    { return city; }
        public void   setCity(String city)     { this.city = city; }
        public String getZipcode() { return zipcode; }
        public void   setZipcode(String zipcode) { this.zipcode = zipcode; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Company {
        private String name;
        private String catchPhrase;
        private String bs;

        public String getName()        { return name; }
        public void   setName(String name) { this.name = name; }
        public String getCatchPhrase() { return catchPhrase; }
        public void   setCatchPhrase(String catchPhrase) { this.catchPhrase = catchPhrase; }
        public String getBs()          { return bs; }
        public void   setBs(String bs) { this.bs = bs; }
    }
}
