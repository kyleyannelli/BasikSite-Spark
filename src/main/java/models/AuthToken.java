package models;

import jakarta.persistence.*;
import repositories.HibernateAuthTokenRepository;

import java.math.BigInteger;
import java.security.SecureRandom;
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
    @Column( name = "is_active", columnDefinition = "bit")
    private boolean isActive;
    @Column( name = "expiry_in_seconds" )
    private long expiryInSeconds;

    @Column( name = "user_agent" )
    private String userAgent;

    public AuthToken(String value, User user){
        this.value = value;
        this.user = user;
    }

    public AuthToken(User user) {
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

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * This returns the literal value of boolean isActive.
     *  This does NOT check the expiry date, thus this method usage is discouraged.
     * @deprecated use {@link #isActive()} instead.
     */
    @Deprecated
    public boolean getIsActive() {
        return isActive;
    }

    public void setUserAgent(String userAgent) {
        if(userAgent.length() > 255) this.userAgent = userAgent.substring(0, 255);
        else this.userAgent = userAgent;
    }

    public boolean isSameUserAgent(String userAgent) {
        if(userAgent.length() > 255) userAgent = userAgent.substring(0, 255);
        return this.userAgent.equals(userAgent);
    }

    public long getId() {
        return id;
    }

    public void generate(HibernateAuthTokenRepository hibernateAuthTokenRepository) {
        SecureRandom random = new SecureRandom();
        String authTokenValue = new BigInteger(512, random).toString(32);
        while (hibernateAuthTokenRepository.findByValue(authTokenValue).isPresent()) {
            authTokenValue = new BigInteger(512, random).toString(32);
        }
        this.value = authTokenValue;
        this.isActive = true;
        this.expiryInSeconds = new Date().toInstant().getEpochSecond() + (86400 * 90);
    }

    public void invalidate(HibernateAuthTokenRepository hibernateAuthTokenRepository) {
        this.isActive = false;
        hibernateAuthTokenRepository.save(this);
    }
}
