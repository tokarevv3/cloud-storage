package ru.tokarev.cloudstorage.database.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String login;
    private String password;

    private String username;

    private Role role;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "bucketId")
    private Bucket bucket;

}
