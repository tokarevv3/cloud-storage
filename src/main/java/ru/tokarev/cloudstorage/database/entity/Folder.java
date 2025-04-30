package ru.tokarev.cloudstorage.database.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "folders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "child")
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String path;

    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @Nullable
    @JoinColumn(name="parent_id")
    private Folder parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Folder> child = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @Nullable
    private Bucket bucketId;

}
