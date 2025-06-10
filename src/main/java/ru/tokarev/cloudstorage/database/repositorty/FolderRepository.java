package ru.tokarev.cloudstorage.database.repositorty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.tokarev.cloudstorage.database.entity.Bucket;
import ru.tokarev.cloudstorage.database.entity.Folder;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

     Optional<Folder> getFolderById(long id);

     @Query("select f from Folder f where f.parent = :parent")
     List<Folder> getAllFoldersByParentId(@Param("parent") Folder parent);

     Folder getFolderByNameAndPathAndBucketId(String name, String path, Bucket bucketId);

     Folder getFolderByNameAndBucketId(String folderName, Bucket bucket);

     List<Folder> findByBucketId(Bucket bucketId);
}
