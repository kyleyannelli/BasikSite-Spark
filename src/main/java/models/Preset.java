package models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table( name = "presets")
public class Preset {
    @Id
    @GeneratedValue
    private Long id;

    public Long getId() {
        return id;
    }
}
