package models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;

@Entity
@Table( name = "presets")
public class Preset {
    @Id
    @GeneratedValue
    @JsonSerialize
    private Long id;

    @ManyToOne
    @JoinColumn( name = "user_id" )
    @JsonSerialize
    private User user;

    @Column( name = "name" )
    @JsonSerialize
    private String name;

    @Column( name = "description" )
    @JsonSerialize
    private String description;

    @Column( name = "direct_value" )
    private String directValue;

    @Column( name = "is_public" )
    @JsonSerialize
    private Boolean isPublic;

    public Preset() {

    }

    public Preset(String directValue) {
        this.directValue = directValue;
    }

    public Preset(String directValue, User user) {
        this.directValue = directValue;
        this.user = user;
    }

    public String getDirectValue() {
        return directValue;
    }

    public Long getId() {
        return id;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.writeValueAsString(this);
    }
}
