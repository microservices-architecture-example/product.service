package store.product;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class ProductResource implements ProductController {

    @Autowired
    private ProductService productService;

    @Override
    public ResponseEntity<ProductOut> create(ProductIn in) {
        // parser ProductIn to Product
        Product product = ProductParser.to(in);

        Product saved = productService.create(product);

        // parser Product to ProductOut and build to
        // HATEAOS standard
        return ResponseEntity
            .created(
                ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(saved.id())
                    .toUri()
            ).body(ProductParser.to(saved));
    }

    @Override
    public ResponseEntity<ProductOut> findById(String id) {
        return ResponseEntity
            .ok(ProductParser.to(productService.findById(id)));
    }

    @Override
    public ResponseEntity<List<ProductOut>> findAll() {
        return ResponseEntity
            .ok()
            .body(ProductParser.to(productService.findAll()));
    }

    @Override
    public ResponseEntity<List<ProductOut>> findByName(String name) {
        return ResponseEntity
            .ok()
            .body(ProductParser.to(productService.findByName(name)));
    }

    @Override
    public ResponseEntity<Void> delete(String id) {
        return ResponseEntity
            .noContent()
            .build();
    }
    
}