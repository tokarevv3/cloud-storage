package ru.tokarev.cloudstorage.database.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "buckets")
@Data
@ToString(exclude = {"folders", "user", "rootFolder"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bucket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long size;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "bucket", fetch = FetchType.LAZY)
    private List<Folder> folders = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="folder_id")
    private Folder rootFolder;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bucket)) return false;
        Bucket bucket = (Bucket) o;
        return Objects.equals(id, bucket.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
