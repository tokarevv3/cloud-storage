package ru.tokarev.cloudstorage.database.repositorty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.tokarev.cloudstorage.database.entity.File;
import ru.tokarev.cloudstorage.database.entity.Folder;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {


    @Query("select f from File f where f.folder = :parent")
    List<File> getAllFilesByParentId(@Param("parent") Folder parent);

    @Query("select f from File f where f.folder.bucketId.user.id = :userId and f.fileName like concat('%', :search, '%')")
    List<File> findByUserIdAndFileNameLike(Long userId, String search);
}
