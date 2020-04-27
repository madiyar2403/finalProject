package kz.iitu.csse.group34.repositories;

import kz.iitu.csse.group34.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAll();
    Optional<Category> findById(Long id);

}