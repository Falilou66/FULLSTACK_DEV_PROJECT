package sn.samabank.customer.dto;

public class CreateUserRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;

    public CreateUserRequest() {}

    public CreateUserRequest(String firstName, String lastName, String email, String password, String role) {
        this.firstName = firstName;
        this.lastName  = lastName;
        this.email     = email;
        this.password  = password;
        this.role      = role;
    }

    public String getFirstName()          { return firstName; }
    public void setFirstName(String v)    { this.firstName = v; }
    public String getLastName()           { return lastName; }
    public void setLastName(String v)     { this.lastName = v; }
    public String getEmail()              { return email; }
    public void setEmail(String v)        { this.email = v; }
    public String getPassword()           { return password; }
    public void setPassword(String v)     { this.password = v; }
    public String getRole()               { return role; }
    public void setRole(String v)         { this.role = v; }
}
