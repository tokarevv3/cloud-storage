package ru.tokarev.cloudstorage.database.repositorty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.tokarev.cloudstorage.database.entity.File;
import ru.tokarev.cloudstorage.database.entity.Folder;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

     Folder getFolderByPath(String path);

     Folder getFolderById(long id);

//     @Query("select Folder f from Folder h where f.parent_id = :parent")
//     List<Folder> getAllFoldersByParentId(Folder parent);
//
//     @Query("select File f from Folder h where f.folder = :parent")
//     List<File> getAllFilesByParentId(Folder parent);
}
