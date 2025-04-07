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
    private Folder parentId;

    @OneToMany(mappedBy = "parentId")
    private List<Folder> childId = new ArrayList<>();

    @ManyToOne
    @Nullable
    private Bucket bucketId;

}
