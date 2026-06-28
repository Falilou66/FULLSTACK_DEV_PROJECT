package sn.samabank.customer.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class CreateCustomerRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100)
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100)
    private String lastName;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateOfBirth;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 100)
    private String password;

    @Pattern(regexp = "^[+]?[0-9]{8,15}$",
            message = "Format téléphone invalide")
    private String phone;

    @Size(max = 500)
    private String address;

    public String getFirstName()          { return firstName; }
    public void setFirstName(String v)    { this.firstName = v; }
    public String getLastName()           { return lastName; }
    public void setLastName(String v)     { this.lastName = v; }
    public LocalDate getDateOfBirth()     { return dateOfBirth; }
    public void setDateOfBirth(LocalDate v){ this.dateOfBirth = v; }
    public String getEmail()              { return email; }
    public void setEmail(String v)        { this.email = v; }
    public String getPassword()           { return password; }
    public void setPassword(String v)     { this.password = v; }
    public String getPhone()              { return phone; }
    public void setPhone(String v)        { this.phone = v; }
    public String getAddress()            { return address; }
    public void setAddress(String v)      { this.address = v; }
}
