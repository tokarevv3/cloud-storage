package ru.tokarev.cloudstorage.database.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "buckets")
@Data
@ToString(exclude = "folders")
@EqualsAndHashCode(of = "name")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bucket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long size;

    @OneToOne
    @JoinColumn(name="user_id")
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "bucketId")
    private List<Folder> folders = new ArrayList<>();

    @OneToOne
    @JoinColumn(name="folder_id")
    private Folder rootFolderId;
}
