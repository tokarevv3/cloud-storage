package ru.tokarev.cloudstorage.database.repositorty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.tokarev.cloudstorage.database.entity.File;
import ru.tokarev.cloudstorage.database.entity.Folder;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

     Folder getFolderByPath(String path);

     Folder getFolderById(long id);

     @Query("select f from Folder f where f.parent = :parent")
     List<Folder> getAllFoldersByParentId(@Param("parent") Folder parent);

     @Query("select f from File f where f.folder = :parent")
     List<File> getAllFilesByParentId(@Param("parent") Folder parent);

}
