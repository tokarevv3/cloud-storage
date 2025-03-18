package ru.tokarev.cloudstorage.database.repositorty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tokarev.cloudstorage.database.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
