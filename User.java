/**
 * User model class representing a user in the marketplace system.
 * Stores user information including credentials and role.
 */
public class User {
    private int userId;
    private String username;
    private String email;
    private String role;
    
    /**
     * Constructor for creating a User object
     * @param userId unique identifier for the user
     * @param username username for login
     * @param email user's email address
     * @param role user role (customer, seller, admin)
     */
    public User(int userId, String username, String email, String role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
    }
    
    /**
     * Constructor for creating a User without userId (for new users)
     * @param username username for login
     * @param email user's email address
     * @param role user role (customer, seller, admin)
     */
    public User(String username, String email, String role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }
    
    // Getters and Setters
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    @Override
    public String toString() {
        return String.format("User[id=%d, username=%s, email=%s, role=%s]", 
            userId, username, email, role);
    }
}
