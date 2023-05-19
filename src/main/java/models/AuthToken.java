package models;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table( name = "users_auth_tokens" )
public class AuthToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
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

    public AuthToken(String value, User user){
        this.value = value;
        this.user = user;
    }

    public AuthToken() {

    }

    public User getUser() {
        return user;
    }

    public String getValue() {
        return value;
    }

    public boolean isActive() {
        return expiryInSeconds > new Date().toInstant().getEpochSecond() && isActive;
    }

    public void setExpiryInSeconds(long epochSeconds) {
        this.expiryInSeconds = epochSeconds;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
