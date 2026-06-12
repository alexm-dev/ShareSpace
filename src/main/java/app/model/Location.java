package app.model;

/**
 * Location class representing the locations table in the database.
 * This class represents a physical location of an asset.
 */
public class Location {
    private int id;
    private String city;
    private String postalCode;
    private String district;
    private String streetAddress;
    private String country;

    public Location(int id, String city, String postalCode, String district, String streetAddress, String country) {
        this.id = id;
        this.city = city;
        this.postalCode = postalCode;
        this.district = district;
        this.streetAddress = streetAddress;
        this.country = country;
    }

    public Location(String city, String postalCode, String district, String streetAddress, String country) {
        this.city = city;
        this.postalCode = postalCode;
        this.district = district;
        this.streetAddress = streetAddress;
        this.country = country;
    }

    // Getters
    public int getId() { return id; }
    public String getCity() { return city; }
    public String getPostalCode() { return postalCode; }
    public String getDistrict() { return district; }
    public String getStreetAddress() { return streetAddress; }
    public String getCountry() { return country; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setCity(String city) { this.city = city; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public void setDistrict(String district) { this.district = district; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }
    public void setCountry(String country) { this.country = country; }

    /**
     * Returns a copy of this location without the street address, for showing
     * an approximate location before a booking is confirmed.
     *
     * @return a copy with the street address removed
     */
    public Location withoutStreet() {
        return new Location(id, city, postalCode, district, null, country);
    }
}
