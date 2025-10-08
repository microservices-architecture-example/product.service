package store.product;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends CrudRepository<ProductModel, String> {
    // find by name
    Optional<ProductModel> findByName(String name);
    
    // find by "like" name
    Iterable<ProductModel> findByNameContaining(String name);
}