package store.product;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Product create(Product product) {
        if (null == product.name()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Name is mandatory!"
            );
        }
        // clean special caracters
        product.name(product.name().trim());

        if (null == product.price()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Price is mandatory!"
            );
        }

        if (null == product.unit()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Unit is mandatory!"
            );
        }

        if (productRepository.findByName(product.name()) != null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Name already have been registered!"
            );

        return productRepository.save(
            new ProductModel(product)
        ).to();
    }

    public List<Product> findAll() {
        return StreamSupport.stream(
            productRepository.findAll().spliterator(), false)
            .map(ProductModel::to)
            .toList();
    }

    public Product findById(String id) {
        return productRepository.findById(id).map(ProductModel::to).orElse(null);
    }

    public List<Product> findByName(String name) {
        return StreamSupport.stream(
            productRepository.findByNameContaining(name).spliterator(), false)
            .map(ProductModel::to)
            .toList();
    }

    public void delete(String id) {
        productRepository.delete(new ProductModel().id(id));
    }
}