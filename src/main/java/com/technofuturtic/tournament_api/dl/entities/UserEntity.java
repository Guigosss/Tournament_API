package com.technofuturtic.tournament_api.dl.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_")
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = {"password"}) @ToString(exclude =  {"password"})
public class UserEntity extends BaseEntity {

    @Getter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    @Column(unique = true, nullable = false)
    private String username;

    @Getter @Setter
    @Column(unique = true, nullable = false)
    private String email;

    @Getter @Setter
    @Column(nullable = false)
    private String password;

    @Getter @Setter
    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE}
    )
    private RoleEntity role;

    public UserEntity(String username) {
        this();
        this.username = username;
    }

    public UserEntity(String username, String email, String password) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public UserEntity(String username,String email, String password, RoleEntity role) {
        this(username,email, password);
        this.role = role;
    }
}
