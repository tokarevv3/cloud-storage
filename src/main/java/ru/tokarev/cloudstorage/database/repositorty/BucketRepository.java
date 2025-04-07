package ru.tokarev.cloudstorage.database.repositorty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.Folder;

public interface BucketRepository extends JpaRepository<Bucket, Long> {

//    @Query
//    Folder getRootFolderByBucketId(Long id);

    Bucket findByName(String name);

}
