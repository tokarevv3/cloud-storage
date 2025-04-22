package ru.tokarev.cloudstorage.database.repositorty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.tokarev.cloudstorage.database.entity.File;
import ru.tokarev.cloudstorage.database.entity.Folder;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

//    List<Object> getAllByFolder(Folder folder);

    @Query("select f from File f where f.folder = :parent")
    List<File> getAllFilesByParentId(@Param("parent") Folder parent);

}
