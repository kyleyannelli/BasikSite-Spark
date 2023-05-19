package models;

import jakarta.persistence.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Set;

@Entity
@Table( name = "users" )
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column( name = "username" )
    private String username;

    @Column( name = "tag_id" )
    private String tagId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<AuthToken> authTokens;

    @Column( name = "password" )
    private String password;

    public Long getId() {
        return id;
    }

    public boolean validAuthTokenForUser(String authTokenStr) {
        for(AuthToken authToken : authTokens) {
            if(authToken.getValue().equals(authTokenStr)) {
                return true;
            }
        }
        return false;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String username() {
        return username;
    }

    public void createPassword(String passwordRaw) {
        password = BCrypt.hashpw(passwordRaw, BCrypt.gensalt());;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = String.format("%0" + 4 + "d", tagId);
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public boolean isCorrectPassword(String password) {
        return BCrypt.checkpw(password, this.password);
    }
}
