package models;

import jakarta.persistence.*;

@Entity
@Table( name = "users_auth_tokens" )
public class AuthToken {
    @Id
    @GeneratedValue
    private long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column( name = "value" )
    private String value;
    @Column( name = "is_active" )
    private boolean isActive;
    @Column( name = "expiry_in_seconds" )
    private long expiryInSeconds;

    public User getUser() {
        return user;
    }
}
