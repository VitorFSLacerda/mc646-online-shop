package myapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import myapp.domain.Product;
import myapp.domain.enumeration.ProductStatus;
import myapp.repository.ProductRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * MC646 – Atividade 3
 *
 * Estratégia:
 *  - VÁLIDOS: checam Bean Validation (0 violações) e, nos casos 01 e 02,
 *    exercitam productService.save() (repositório mockado).
 *  - INVÁLIDOS: checam que há violação na propriedade correta.
 *
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    /** Produto-base VÁLIDO usado como ponto de partida nos testes. */
    private Product baseValid() {
        Product p = new Product();
        p.setTitle("ABC"); // 3 chars (limite inferior)
        p.setKeywords(null); // opcional
        p.setDescription("D".repeat(50)); // se presente, >= 50
        p.setRating(1); // 1..10
        p.setPrice(new BigDecimal("1.00")); // 1..9999
        p.setQuantityInStock(0); // >= 0
        p.setStatus(ProductStatus.IN_STOCK);
        p.setWeight(0.0d); // >= 0
        p.setDimensions("10x10x10 cm"); // <= 50
        p.setDateAdded(Instant.now()); // obrigatório
        p.setDateModified(null); // opcional
        return p;
    }

    /** Espera que NÃO haja erros de validação. */
    private void assertNoViolations(Product p) {
        Set<ConstraintViolation<Product>> v = validator.validate(p);
        assertTrue(v.isEmpty(), "Esperava 0 violações, mas houve: " + v);
    }

    /** Espera que haja pelo menos um erro de validação no campo informado. */
    private void assertHasViolationOn(Product p, String property) {
        Set<ConstraintViolation<Product>> v = validator.validate(p);
        boolean has = v.stream().anyMatch(cv -> cv.getPropertyPath().toString().equals(property));
        assertTrue(has, "Esperava violação em '" + property + "', mas foram: " + v);
    }

    // =====================================================================
    // CASO TESTE 01 — Limites inferiores VÁLIDOS
    // =====================================================================

    @Test
    @DisplayName("Caso teste 01: limites inferiores válidos → save() ok")
    void casoteste01a_validLowerBounds_saveOk() {
        Product p = baseValid();
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertNoViolations(p);
        assertNotNull(productService.save(p));
        verify(productRepository).save(p);
    }

    @Test
    @DisplayName("Caso teste 01 (compl.): price=1.00 válido")
    void casoteste01b_price1_valid() {
        Product p = baseValid();
        p.setPrice(new BigDecimal("1.00"));
        assertNoViolations(p);
    }

    @Test
    @DisplayName("Caso teste 01 (compl.): quantityInStock=0 válido")
    void casoteste01c_qty0_valid() {
        Product p = baseValid();
        p.setQuantityInStock(0);
        assertNoViolations(p);
    }

    @Test
    @DisplayName("Caso teste 01 (compl.): weight=0.0 válido")
    void casoteste01d_weight0_valid() {
        Product p = baseValid();
        p.setWeight(0.0d);
        assertNoViolations(p);
    }

    // =====================================================================
    // CASO TESTE 02 — Limites superiores VÁLIDOS
    // =====================================================================

    @Test
    @DisplayName("Caso teste 02: limites superiores válidos → save() ok")
    void casoteste02a_validUpperBounds_saveOk() {
        Product p = baseValid();
        p.setTitle("X".repeat(100));
        p.setKeywords("K".repeat(200));
        p.setDescription("D".repeat(50));
        p.setRating(10);
        p.setPrice(new BigDecimal("9999"));
        p.setQuantityInStock(1);
        p.setStatus(ProductStatus.PREORDER);
        p.setWeight(0.0d);
        p.setDimensions("D".repeat(50));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertNoViolations(p);
        assertNotNull(productService.save(p));
    }

    @Test
    @DisplayName("Caso teste 02 (compl.): description=50 válido")
    void casoteste02b_description50_valid() {
        Product p = baseValid();
        p.setDescription("D".repeat(50));
        assertNoViolations(p);
    }

    @Test
    @DisplayName("Caso teste 02 (compl.): price=9999 válido")
    void casoteste02c_price9999_valid() {
        Product p = baseValid();
        p.setPrice(new BigDecimal("9999"));
        assertNoViolations(p);
    }

    @Test
    @DisplayName("Caso teste 02 (compl.): dimensions=50 válido")
    void casoteste02d_dimensions50_valid() {
        Product p = baseValid();
        p.setDimensions("D".repeat(50));
        assertNoViolations(p);
    }

    // =====================================================================
    // CASO TESTE 03 — Campos opcionais nulos VÁLIDOS
    // =====================================================================

    @Test
    @DisplayName("Caso teste 03: keywords=null válido")
    void casoteste03a_keywordsNull_valid() {
        Product p = baseValid();
        p.setKeywords(null);
        assertNoViolations(p);
    }

    @Test
    @DisplayName("Caso teste 03: description=null válido")
    void casoteste03b_descriptionNull_valid() {
        Product p = baseValid();
        p.setDescription(null);
        assertNoViolations(p);
    }

    @Test
    @DisplayName("Caso teste 03: weight=null válido")
    void casoteste03c_weightNull_valid() {
        Product p = baseValid();
        p.setWeight(null);
        assertNoViolations(p);
    }

    @Test
    @DisplayName("Caso teste 03: dimensions=null válido")
    void casoteste03d_dimensionsNull_valid() {
        Product p = baseValid();
        p.setDimensions(null);
        assertNoViolations(p);
    }

    // =====================================================================
    // CASO TESTE 04 — Título < 3 (INVÁLIDO)
    // =====================================================================

    @Test
    @DisplayName("Caso teste 04: title com 2 chars inválido")
    void casoteste04_titleTooShort_invalid() {
        Product p = baseValid();
        p.setTitle("AB");
        assertHasViolationOn(p, "title");
    }

    // =====================================================================
    // CASO TESTE 05 — Título > 100 (INVÁLIDO)
    // =====================================================================

    @Test
    @DisplayName("Caso teste 05: title com 101 chars inválido")
    void casoteste05_titleTooLong_invalid() {
        Product p = baseValid();
        p.setTitle("X".repeat(101));
        assertHasViolationOn(p, "title");
    }

    // =====================================================================
    // CASO TESTE 06 — Título nulo (INVÁLIDO)
    // =====================================================================

    @Test
    @DisplayName("Caso teste 06: title=null inválido")
    void casoteste06_titleNull_invalid() {
        Product p = baseValid();
        p.setTitle(null);
        assertHasViolationOn(p, "title");
    }

    // =====================================================================
    // CASO TESTE 07 — Palavras-chave > 200 (INVÁLIDO)
    // =====================================================================

    @Test
    @DisplayName("Caso teste 07: keywords com 201 chars inválido")
    void casoteste07_keywordsTooLong_invalid() {
        Product p = baseValid();
        p.setKeywords("K".repeat(201));
        assertHasViolationOn(p, "keywords");
    }

    // =====================================================================
    // CASO TESTE 08 — Descrição < 50 (INVÁLIDO)
    // =====================================================================

    @Test
    @DisplayName("Caso teste 08: description com 49 chars inválido")
    void casoteste08_descriptionTooShort_invalid() {
        Product p = baseValid();
        p.setDescription("D".repeat(49));
        assertHasViolationOn(p, "description");
    }

    // =====================================================================
    // CASO TESTE 09 — Avaliação < 1 (INVÁLIDO)
    // =====================================================================

    @Test
    @DisplayName("Caso teste 9: rating=0 inválido")
    void casoteste10_ratingBelow_invalid() {
        Product p = baseValid();
        p.setRating(0);
        assertHasViolationOn(p, "rating");
    }

    // =====================================================================
    // CASO TESTE 10 — Avaliação > 10 (INVÁLIDO)
    // =====================================================================

    @Test
    @DisplayName("Caso teste 10: rating=11 inválido")
    void casoteste11_ratingAbove_invalid() {
        Product p = baseValid();
        p.setRating(11);
        assertHasViolationOn(p, "rating");
    }

    // =====================================================================
    // CASO TESTE 11 — Preço < 1 (INVÁLIDO)
    // =====================================================================

    @Test
    @DisplayName("Caso teste 11: price=0.99 inválido")
    void casoteste13_priceBelow_invalid() {
        Product p = baseValid();
        p.setPrice(new BigDecimal("0.99"));
        assertHasViolationOn(p, "price");
    }

    // =====================================================================
    // CASO TESTE 12 — Preço > 9999 (INVÁLIDO)
    // =====================================================================

    @Test
    @DisplayName("Caso teste 12: price=10000 inválido")
    void casoteste14_priceAbove_invalid() {
        Product p = baseValid();
        p.setPrice(new BigDecimal("10000"));
        assertHasViolationOn(p, "price");
    }

    // =====================================================================
    // CASO TESTE 13 — Preço nulo (INVÁLIDO)
    // =====================================================================

    @Test
    @DisplayName("Caso teste 13: price=null inválido")
    void casoteste15_priceNull_invalid() {
        Product p = baseValid();
        p.setPrice(null);
        assertHasViolationOn(p, "price");
    }

    // =====================================================================
    // CASO TESTE 14 — Quantidade < 0 (INVÁLIDO)
    // =====================================================================

    @Test
    @DisplayName("Caso teste 14: quantityInStock=-1 inválido")
    void casoteste17_quantityNegative_invalid() {
        Product p = baseValid();
        p.setQuantityInStock(-1);
        assertHasViolationOn(p, "quantityInStock");
    }

    // =====================================================================
    // CASO TESTE 15 — Status nulo (INVÁLIDO)
    // =====================================================================

    @Test
    @DisplayName("Caso teste 15: status=null inválido")
    void casoteste19_statusNull_invalid() {
        Product p = baseValid();
        p.setStatus(null);
        assertHasViolationOn(p, "status");
    }

    // =====================================================================
    // CASO TESTE 16–19 — Status válidos (VÁLIDOS)
    // =====================================================================

    @Test
    @DisplayName("Caso teste 16: status=IN_STOCK válido")
    void casoteste21_statusInStock_valid() {
        Product p = baseValid();
        p.setStatus(ProductStatus.IN_STOCK);
        assertNoViolations(p);
    }

    @Test
    @DisplayName("Caso teste 17: status=OUT_OF_STOCK válido")
    void casoteste22_statusOutOfStock_valid() {
        Product p = baseValid();
        p.setStatus(ProductStatus.OUT_OF_STOCK);
        assertNoViolations(p);
    }

    @Test
    @DisplayName("Caso teste 18: status=PREORDER válido")
    void casoteste23_statusPreorder_valid() {
        Product p = baseValid();
        p.setStatus(ProductStatus.PREORDER);
        assertNoViolations(p);
    }

    @Test
    @DisplayName("Caso teste 19: status=DISCONTINUED válido")
    void casoteste24_statusDiscontinued_valid() {
        Product p = baseValid();
        p.setStatus(ProductStatus.DISCONTINUED);
        assertNoViolations(p);
    }

    // =====================================================================
    // CASO TESTE 20 — Peso < 0 (INVÁLIDO)
    // =====================================================================

    @Test
    @DisplayName("Caso teste 20: weight=-0.01 inválido")
    void casoteste26_weightNegative_invalid() {
        Product p = baseValid();
        p.setWeight(-0.01d);
        assertHasViolationOn(p, "weight");
    }

    // =====================================================================
    // CASO TESTE 21 — Dimensões > 50 (INVÁLIDO)
    // =====================================================================

    @Test
    @DisplayName("Caso teste 21: dimensions com 51 chars inválido")
    void casoteste27_dimensionsTooLong_invalid() {
        Product p = baseValid();
        p.setDimensions("D".repeat(51));
        assertHasViolationOn(p, "dimensions");
    }

    // =====================================================================
    // CASO TESTE 22 — Data de adição nula (INVÁLIDO)
    // =====================================================================

    @Test
    @DisplayName("Caso teste 22: dateAdded=null inválido")
    void casoteste28_dateAddedNull_invalid() {
        Product p = baseValid();
        p.setDateAdded(null);
        assertHasViolationOn(p, "dateAdded");
    }
}
