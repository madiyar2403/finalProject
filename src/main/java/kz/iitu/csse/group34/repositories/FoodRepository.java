package kz.iitu.csse.group34.repositories;

import kz.iitu.csse.group34.entities.Food;
import kz.iitu.csse.group34.entities.Restaurants;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long> {

    List<Food> findAll();
    Optional<Food> findById(Long id);

}