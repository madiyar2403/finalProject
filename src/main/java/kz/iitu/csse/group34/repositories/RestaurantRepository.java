package kz.iitu.csse.group34.repositories;

import kz.iitu.csse.group34.entities.Restaurants;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurants, Long> {

    List<Restaurants> findAll();
    Optional<Restaurants> findById(Long id);

}