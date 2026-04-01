package com.yellowinsurance.claims.model.dto;

// ISSUE: No input validation
// ISSUE: SSN accepted as plain text input
public class CustomerDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String ssn;
    private String dateOfBirth;
    private String street;
    private String city;
    private String state;
    private String zip;
    private String password;

    public CustomerDTO() {}

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSsn() { return ssn; }
    public void setSsn(String ssn) { this.ssn = ssn; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
