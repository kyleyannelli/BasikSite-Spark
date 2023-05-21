package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import org.mindrot.jbcrypt.BCrypt;
import repositories.HibernateUserRepository;

import javax.servlet.http.HttpServletResponse;
import java.util.BitSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static spark.Spark.halt;

@Entity
@Table( name = "users" )
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( name = "id" )
    @JsonProperty("id")
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
        if(tagId < 0 || tagId > 9999) throw new RuntimeException("Tag ID must be a valid integer between [0, 9999]!");
        this.tagId = String.format("%0" + 4 + "d", tagId);
    }

    public void setTagId(String tagId) {
        if(tagId.length() != 4 || !tagId.chars().allMatch(Character::isDigit)) {
            throw new RuntimeException("Tag ID must be a valid number between [0, 9999]. Numbers less than four digits must be padded!");
        }
        this.tagId = tagId;
    }

    public boolean isCorrectPassword(String password) {
        return BCrypt.checkpw(password, this.password);
    }

    public static User validateUser(HibernateUserRepository hibernateUserRepository, String username, String tag, String password) {
        User user = hibernateUserRepository.findByUsernameAndTag(username, tag);
        if (!user.isCorrectPassword(password)) {
            halt(401);
        }
        return user;
    }

    public static int generateTag(HibernateUserRepository hibernateUserRepository, String  username) {
        int tagId, total = 0;
        // 9999 possible outcomes, BitSet is probably the fastest. Very marginal performance improvement, but it adds for a lot of requests.
        BitSet foundTags = new BitSet(9999);
        // Current max shared usernames are 100.
        if(hibernateUserRepository.getCountByUsername(username) >= 100) {
            halt(HttpServletResponse.SC_CONFLICT, "Please pick a different username");
        }
        do {
            // ThreadLocalRandom is now preferred over Random random = new... etc
            tagId = ThreadLocalRandom.current().nextInt(0, 10000);
            total++;

            // if we have already seen this tag do not bother with the db query
            if(foundTags.get(tagId)) {
                continue;
            }
            if (!hibernateUserRepository.doesUsernameAndTagExist(username, String.format("%0" + 4 + "d", tagId))) {
                break;
            }
            foundTags.set(tagId);
        } while (total <= 100);
        // It is possible that in the worst possible case 100 tags could be generated which already exist.
        //  However, that is very unlikely and worth the performance.
        if(total > 100) {
            halt(HttpServletResponse.SC_CONFLICT, "Please pick a different username");
        }
        return tagId;
    }

    @JsonProperty("username")
    public String getUsernameWithTag() {
        return username + "#" + tagId;
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.writeValueAsString(this);
    }
}
