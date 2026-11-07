package model;

/**
 * A registered account. A PARENT account owns one Student record;
 * the ADMIN account has no student.
 */
public class User {
    public static final String ROLE_ADMIN  = "ADMIN";
    public static final String ROLE_PARENT = "PARENT";

    private final String username;
    private final String fullName;
    private final String email;
    private final String role;
    private final String salt;          // Base64
    private final String passwordHash;  // Base64 (PBKDF2)
    private final Student student;      // null for ADMIN

    public User(String username, String fullName, String email, String role,
                String salt, String passwordHash, Student student) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.salt = salt;
        this.passwordHash = passwordHash;
        this.student = student;
    }

    public String getUsername()     { return username; }
    public String getFullName()     { return fullName; }
    public String getEmail()        { return email; }
    public String getRole()         { return role; }
    public String getSalt()         { return salt; }
    public String getPasswordHash() { return passwordHash; }
    public Student getStudent()     { return student; }
    public boolean isAdmin()        { return ROLE_ADMIN.equals(role); }
        /** Returns a copy of this account with an updated student record (User is immutable). */
    public User withStudent(Student updated) {
        return new User(username, fullName, email, role, salt, passwordHash, updated);
    }

}
