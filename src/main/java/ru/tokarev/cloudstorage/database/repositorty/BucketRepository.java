package ru.tokarev.cloudstorage.database.repositorty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.Folder;
import ru.tokarev.cloudstorage.dto.BucketReadDto;

import java.util.Optional;

public interface BucketRepository extends JpaRepository<Bucket, Long> {

//    @Query
//    Folder getRootFolderByBucketId(Long id);

//    Optional<Bucket> findByName(String name);

    @Query("select b.size from Bucket b where b.id = :id")
    Long getSize(Long id);

    Optional<Bucket> findByName(String name);
}
