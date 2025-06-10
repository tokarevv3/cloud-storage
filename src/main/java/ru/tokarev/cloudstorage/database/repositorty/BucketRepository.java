package ru.tokarev.cloudstorage.database.repositorty;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.Folder;

public interface BucketRepository extends JpaRepository<Bucket, Long> {

    boolean existsByRootFolder(Folder rootFolder);
}
