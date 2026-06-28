package sn.samabank.customer.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateCustomerRequest {

    @Size(min = 2, max = 100)
    private String firstName;

    @Size(min = 2, max = 100)
    private String lastName;

    @Pattern(regexp = "^[+]?[0-9]{8,15}$",
            message = "Format téléphone invalide")
    private String phone;

    @Size(max = 500)
    private String address;

    public String getFirstName()       { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }
    public String getLastName()        { return lastName; }
    public void setLastName(String v)  { this.lastName = v; }
    public String getPhone()           { return phone; }
    public void setPhone(String v)     { this.phone = v; }
    public String getAddress()         { return address; }
    public void setAddress(String v)   { this.address = v; }
}
