package models;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table( name = "users" )
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @Column( name = "username" )
    private String username;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<AuthToken> authTokens;

    public Long getId() {
        return id;
    }
}
