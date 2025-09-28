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
 * MC646 – Atividade 3 (Parte 4)
 *
 * O que estes testes fazem:
 *  - Casos VÁLIDOS: conferem que não há erros de validação e chamam productService.save() (repositório é mock).
 *  - Casos INVÁLIDOS: conferem que existe erro de validação no campo certo.
 *
 * Nota sobre datas:
 *  - A planilha considera "dateModified == dateAdded" como inválido.
 *  - No código atual não existe regra para isso. Então aqui esse caso passa como VÁLIDO
 *    (e isso é mencionado no relatório).
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
        p.setTitle("ABC"); // 3 caracteres (mínimo)
        p.setKeywords(null); // opcional
        p.setDescription("D".repeat(50)); // se tiver, precisa ter >= 50
        p.setRating(1); // 1..10
        p.setPrice(new BigDecimal("1.00")); // 1..9999
        p.setQuantityInStock(0); // >= 0
        p.setStatus(ProductStatus.IN_STOCK); // valor permitido
        p.setWeight(0.0d); // >= 0
        p.setDimensions("10x10x10 cm"); // <= 50 caracteres
        p.setDateAdded(Instant.now()); // obrigatório
        p.setDateModified(null); // opcional
        return p;
    }

    /** Espera que NÃO haja erros de validação. */
    private void assertNoViolations(Product p) {
        Set<ConstraintViolation<Product>> v = validator.validate(p);
        if (!v.isEmpty()) {
            v.forEach(cv -> System.out.println("Violação: " + cv.getPropertyPath() + " -> " + cv.getMessage()));
        }
        assertTrue(v.isEmpty(), "Esperava 0 violações, mas houve: " + v);
    }

    /** Espera que haja pelo menos um erro de validação no campo informado. */
    private void assertHasViolationOn(Product p, String property) {
        Set<ConstraintViolation<Product>> v = validator.validate(p);
        boolean has = v.stream().anyMatch(cv -> cv.getPropertyPath().toString().equals(property));
        if (!has) {
            v.forEach(cv ->
                System.out.println("Violação encontrada (não na propriedade alvo): " + cv.getPropertyPath() + " -> " + cv.getMessage())
            );
        }
        assertTrue(has, "Esperava violação em '" + property + "', mas foram: " + v);
    }

    // ---------------------------- Casos VÁLIDOS ----------------------------

    @Test
    @DisplayName("Caso 01: limites inferiores válidos (title=3, price=1, qty=0, status válido, dateAdded presente) -> save ok")
    void caso01_validLowerBounds() {
        Product p = baseValid();
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertNoViolations(p);
        Product saved = productService.save(p);
        assertNotNull(saved);
        verify(productRepository).save(p);
    }

    @Test
    @DisplayName(
        "Caso 02: limites superiores válidos (title=100, keywords=200, desc=50, rating=10, price=9999, qty>0, status=PREORDER, weight=0, dim=50)"
    )
    void caso02_validUpperBounds() {
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

    // ---------------------------- Título ----------------------------

    @Test
    @DisplayName("Caso 03: título vazio -> inválido")
    void caso03_titleEmpty_invalid() {
        Product p = baseValid();
        p.setTitle("");
        assertHasViolationOn(p, "title");
    }

    @Test
    @DisplayName("Caso 04: título com 2 caracteres -> inválido (min=3)")
    void caso04_titleTooShort_invalid() {
        Product p = baseValid();
        p.setTitle("AB");
        assertHasViolationOn(p, "title");
    }

    @Test
    @DisplayName("Caso 05: título com 101 caracteres -> inválido (max=100)")
    void caso05_titleTooLong_invalid() {
        Product p = baseValid();
        p.setTitle("X".repeat(101));
        assertHasViolationOn(p, "title");
    }

    // ---------------------------- Palavras-chave ----------------------------

    @Test
    @DisplayName("Caso 06: keywords ausente -> válido")
    void caso06_keywordsAbsent_valid() {
        Product p = baseValid();
        p.setKeywords(null);
        assertNoViolations(p);
    }

    @Test
    @DisplayName("Caso 07: keywords com 200 chars -> válido")
    void caso07_keywords200_valid() {
        Product p = baseValid();
        p.setKeywords("K".repeat(200));
        assertNoViolations(p);
    }

    @Test
    @DisplayName("Caso 08: keywords com 201 chars -> inválido (max=200)")
    void caso08_keywords201_invalid() {
        Product p = baseValid();
        p.setKeywords("K".repeat(201));
        assertHasViolationOn(p, "keywords");
    }

    // ---------------------------- Descrição ----------------------------

    @Test
    @DisplayName("Caso 09: descrição ausente -> válido")
    void caso09_descriptionAbsent_valid() {
        Product p = baseValid();
        p.setDescription(null);
        assertNoViolations(p);
    }

    @Test
    @DisplayName("Caso 10: descrição com 49 chars -> inválido (min=50 se presente)")
    void caso10_description49_invalid() {
        Product p = baseValid();
        p.setDescription("D".repeat(49));
        assertHasViolationOn(p, "description");
    }

    @Test
    @DisplayName("Caso 11: descrição com 50 chars -> válido")
    void caso11_description50_valid() {
        Product p = baseValid();
        p.setDescription("D".repeat(50));
        assertNoViolations(p);
    }

    // ---------------------------- Avaliação ----------------------------

    @Test
    @DisplayName("Caso 12: rating ausente -> válido")
    void caso12_ratingAbsent_valid() {
        Product p = baseValid();
        p.setRating(null);
        assertNoViolations(p);
    }

    @Test
    @DisplayName("Caso 13: rating=0 -> inválido (min=1)")
    void caso13_rating0_invalid() {
        Product p = baseValid();
        p.setRating(0);
        assertHasViolationOn(p, "rating");
    }

    @Test
    @DisplayName("Caso 14: rating=11 -> inválido (max=10)")
    void caso14_rating11_invalid() {
        Product p = baseValid();
        p.setRating(11);
        assertHasViolationOn(p, "rating");
    }

    @Test
    @DisplayName("Caso 15: rating=5 -> válido (inteiro dentro de 1..10)")
    void caso15_rating5_valid() {
        Product p = baseValid();
        p.setRating(5);
        assertNoViolations(p);
    }

    // ---------------------------- Preço ----------------------------

    @Test
    @DisplayName("Caso 16: price=0.99 -> inválido (min=1.00)")
    void caso16_priceBelow_invalid() {
        Product p = baseValid();
        p.setPrice(new BigDecimal("0.99"));
        assertHasViolationOn(p, "price");
    }

    @Test
    @DisplayName("Caso 17: price=1.00 -> válido")
    void caso17_price1_valid() {
        Product p = baseValid();
        p.setPrice(new BigDecimal("1.00"));
        assertNoViolations(p);
    }

    @Test
    @DisplayName("Caso 18: price=9999 -> válido")
    void caso18_price9999_valid() {
        Product p = baseValid();
        p.setPrice(new BigDecimal("9999"));
        assertNoViolations(p);
    }

    @Test
    @DisplayName("Caso 19: price=10000 -> inválido (max=9999)")
    void caso19_price10000_invalid() {
        Product p = baseValid();
        p.setPrice(new BigDecimal("10000"));
        assertHasViolationOn(p, "price");
    }

    // ---------------------------- Quantidade ----------------------------

    @Test
    @DisplayName("Caso 20: quantityInStock=-1 -> inválido (>=0)")
    void caso20_qtyNegative_invalid() {
        Product p = baseValid();
        p.setQuantityInStock(-1);
        assertHasViolationOn(p, "quantityInStock");
    }

    @Test
    @DisplayName("Caso 21: quantityInStock=0 -> válido")
    void caso21_qty0_valid() {
        Product p = baseValid();
        p.setQuantityInStock(0);
        assertNoViolations(p);
    }

    @Test
    @DisplayName("Caso 22: quantityInStock=1 -> válido")
    void caso22_qty1_valid() {
        Product p = baseValid();
        p.setQuantityInStock(1);
        assertNoViolations(p);
    }

    // ---------------------------- Status ----------------------------

    @Test
    @DisplayName("Caso 23: status nulo -> inválido (obrigatório)")
    void caso23_statusNull_invalid() {
        Product p = baseValid();
        p.setStatus(null);
        assertHasViolationOn(p, "status");
    }

    @Test
    @DisplayName("Caso 24: status válidos (IN_STOCK, OUT_OF_STOCK, DISCONTINUED, PREORDER) -> válido")
    void caso24_statusValid_valid() {
        for (ProductStatus st : new ProductStatus[] {
            ProductStatus.IN_STOCK,
            ProductStatus.OUT_OF_STOCK,
            ProductStatus.DISCONTINUED,
            ProductStatus.PREORDER,
        }) {
            Product p = baseValid();
            p.setStatus(st);
            assertNoViolations(p);
        }
    }

    // ---------------------------- Peso ----------------------------

    @Test
    @DisplayName("Caso 25: weight=-0.01 -> inválido (>=0)")
    void caso25_weightNegative_invalid() {
        Product p = baseValid();
        p.setWeight(-0.01d);
        assertHasViolationOn(p, "weight");
    }

    @Test
    @DisplayName("Caso 26: weight=0 -> válido")
    void caso26_weight0_valid() {
        Product p = baseValid();
        p.setWeight(0.0d);
        assertNoViolations(p);
    }

    // ---------------------------- Dimensões ----------------------------

    @Test
    @DisplayName("Caso 27: dimensions com 51 chars -> inválido (max=50)")
    void caso27_dimensions51_invalid() {
        Product p = baseValid();
        p.setDimensions("D".repeat(51));
        assertHasViolationOn(p, "dimensions");
    }

    @Test
    @DisplayName("Caso 28: dimensions com 50 chars -> válido")
    void caso28_dimensions50_valid() {
        Product p = baseValid();
        p.setDimensions("D".repeat(50));
        assertNoViolations(p);
    }

    // ---------------------------- Datas ----------------------------

    @Test
    @DisplayName("Caso 29: dateAdded nula -> inválido (obrigatória)")
    void caso29_dateAddedNull_invalid() {
        Product p = baseValid();
        p.setDateAdded(null);
        assertHasViolationOn(p, "dateAdded");
    }

    @Test
    @DisplayName("Caso 30: dateModified ausente -> válido")
    void caso30_dateModifiedAbsent_valid() {
        Product p = baseValid();
        p.setDateModified(null);
        assertNoViolations(p);
    }

    @Test
    @DisplayName("Caso 31: dateModified == dateAdded -> permitido (domínio não valida)")
    void caso31_dateModifiedEquals_valid() {
        Product p = baseValid();
        Instant t = Instant.now();
        p.setDateAdded(t);
        p.setDateModified(t);
        // Não há regra no código que exija dateModified > dateAdded.
        assertNoViolations(p);
    }
}
