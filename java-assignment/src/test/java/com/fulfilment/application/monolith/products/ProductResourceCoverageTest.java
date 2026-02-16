package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@DisplayName("ProductResource Coverage Tests")
public class ProductResourceCoverageTest {

  private static final String PRODUCT_PATH = "product";

  @Test
  @DisplayName("Should retrieve all products successfully")
  void testGetAllProductsSuccess() {
    given()
        .when()
        .get(PRODUCT_PATH)
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(0))
        .contentType(containsString("application/json"));
  }

  @Test
  @DisplayName("Should return 404 for non-existent product")
  void testGetSingleProductNotFound() {
    given()
        .when()
        .get(PRODUCT_PATH + "/999999")
        .then()
        .statusCode(404)
        .body("error", containsString("does not exist"));
  }

  @Test
  @DisplayName("Should create product with valid data")
  void testCreateProductSuccess() {
    Product newProduct = new Product();
    newProduct.name = uniqueName("Test Product");
    newProduct.description = "A test product";
    newProduct.price = BigDecimal.valueOf(99.99);
    newProduct.stock = 50;

    given()
        .contentType("application/json")
        .body(newProduct)
        .when()
        .post(PRODUCT_PATH)
        .then()
        .statusCode(201)
        .body("id", notNullValue())
        .body("name", equalTo(newProduct.name));
  }

  @Test
  @DisplayName("Should return 422 when creating with pre-set ID")
  void testCreateProductWithPresetId() {
    Product newProduct = new Product();
    newProduct.id = 999L;
    newProduct.name = uniqueName("Invalid Product");

    given()
        .contentType("application/json")
        .body(newProduct)
        .when()
        .post(PRODUCT_PATH)
        .then()
        .statusCode(422)
        .body("error", containsString("Id was invalidly set"));
  }

  @Test
  @DisplayName("Should return 500 when body is null")
  void testCreateProductNullBody() {
    given()
        .contentType("application/json")
        .body("null")
        .when()
        .post(PRODUCT_PATH)
        .then()
        .statusCode(500);
  }

  @Test
  @DisplayName("Should return 500 when name exceeds DB column length")
  void testCreateProductLongName() {
    Product newProduct = new Product();
    newProduct.name = "A".repeat(255);

    given()
        .contentType("application/json")
        .body(newProduct)
        .when()
        .post(PRODUCT_PATH)
        .then()
        .statusCode(500);
  }

  @Test
  @DisplayName("Should update product with valid data")
  void testUpdateProductSuccess() {
    Long productId = createProductAndReturnId(uniqueName("Product To Update"), null, null, 0);

    Product updatedProduct = new Product();
    updatedProduct.name = uniqueName("Updated Product");
    updatedProduct.description = "Updated description";
    updatedProduct.price = BigDecimal.valueOf(149.99);
    updatedProduct.stock = 75;

    given()
        .contentType("application/json")
        .body(updatedProduct)
        .when()
        .put(PRODUCT_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .body("id", equalTo(productId.intValue()))
        .body("name", equalTo(updatedProduct.name))
        .body("price", equalTo(149.99F));
  }

  @Test
  @DisplayName("Should return 404 when updating non-existent product")
  void testUpdateProductNotFound() {
    Product updatedProduct = new Product();
    updatedProduct.name = uniqueName("Updated Name");

    given()
        .contentType("application/json")
        .body(updatedProduct)
        .when()
        .put(PRODUCT_PATH + "/999999")
        .then()
        .statusCode(404)
        .body("error", containsString("does not exist"));
  }

  @Test
  @DisplayName("Should return 422 when updating with null name")
  void testUpdateProductNullName() {
    Long productId = createProductAndReturnId(uniqueName("Product Null Name"), null, null, 0);

    Product updatedProduct = new Product();
    updatedProduct.name = null;

    given()
        .contentType("application/json")
        .body(updatedProduct)
        .when()
        .put(PRODUCT_PATH + "/" + productId)
        .then()
        .statusCode(422)
        .body("error", containsString("Name was not set"));
  }

  @Test
  @DisplayName("Should update product partial fields")
  void testUpdateProductPartialFields() {
    Long productId = createProductAndReturnId(uniqueName("Product Partial"), null, BigDecimal.TEN, 10);

    Product updatedProduct = new Product();
    updatedProduct.name = uniqueName("Product With Updated Fields");
    updatedProduct.description = "New description";
    updatedProduct.price = BigDecimal.valueOf(199.99);
    updatedProduct.stock = 100;

    given()
        .contentType("application/json")
        .body(updatedProduct)
        .when()
        .put(PRODUCT_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .body("price", equalTo(199.99F));
  }

  @Test
  @DisplayName("Should update product to zero price")
  void testUpdateProductToZeroPrice() {
    Long productId = createProductAndReturnId(uniqueName("Product Zero Price"), null, BigDecimal.TEN, 10);

    Product updatedProduct = new Product();
    updatedProduct.name = uniqueName("Free Product Update");
    updatedProduct.price = BigDecimal.ZERO;
    updatedProduct.stock = 10;

    given()
        .contentType("application/json")
        .body(updatedProduct)
        .when()
        .put(PRODUCT_PATH + "/" + productId)
        .then()
        .statusCode(200)
      .body("price", equalTo(0));
  }

  @Test
  @DisplayName("Should update product stock to zero")
  void testUpdateProductToZeroStock() {
    Long productId = createProductAndReturnId(uniqueName("Product Zero Stock"), null, BigDecimal.TEN, 5);

    Product updatedProduct = new Product();
    updatedProduct.name = uniqueName("Out of Stock Update");
    updatedProduct.price = BigDecimal.TEN;
    updatedProduct.stock = 0;

    given()
        .contentType("application/json")
        .body(updatedProduct)
        .when()
        .put(PRODUCT_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .body("stock", equalTo(0));
  }

  @Test
  @DisplayName("Should delete product and return 204")
  void testDeleteProductNoContent() {
    Long productId = createProductAndReturnId(uniqueName("Product To Delete"), null, null, 0);

    given()
        .when()
        .delete(PRODUCT_PATH + "/" + productId)
        .then()
        .statusCode(204);
  }

  @Test
  @DisplayName("Should return 404 when deleting non-existent product")
  void testDeleteProductNotFound() {
    given()
        .when()
        .delete(PRODUCT_PATH + "/999999")
        .then()
        .statusCode(404)
        .body("error", containsString("does not exist"));
  }

  @Test
  @DisplayName("Should delete product then return 404 on GET")
  void testDeleteProductThenVerifyNotFound() {
    Long productId = createProductAndReturnId(uniqueName("Product To Delete Verify"), null, null, 0);

    given()
        .when()
        .delete(PRODUCT_PATH + "/" + productId)
        .then()
        .statusCode(204);

    given()
        .when()
        .get(PRODUCT_PATH + "/" + productId)
        .then()
        .statusCode(404);
  }

  @Test
  @DisplayName("Should return JSON error payload")
  void testErrorResponseIsJson() {
    given()
        .when()
        .get(PRODUCT_PATH + "/999999")
        .then()
        .statusCode(404)
        .contentType(containsString("application/json"))
        .body("exceptionType", notNullValue())
        .body("code", equalTo(404))
        .body("error", notNullValue());
  }

  @Test
  @DisplayName("Should handle create then retrieve")
  void testCreateThenRetrieve() {
    String name = uniqueName("Create Then Retrieve Product");
    Long productId = createProductAndReturnId(name, "Test product", BigDecimal.valueOf(75.00), 0);

    given()
        .when()
        .get(PRODUCT_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .body("name", equalTo(name));
  }

  @Test
  @DisplayName("Should handle create, update, then retrieve")
  void testCreateUpdateThenRetrieve() {
    Long productId = createProductAndReturnId(uniqueName("Original Name"), null, BigDecimal.valueOf(50.00), 0);

    String updatedName = uniqueName("Updated Name");
    Product updatedProduct = new Product();
    updatedProduct.name = updatedName;
    updatedProduct.price = BigDecimal.valueOf(100.00);
    updatedProduct.stock = 0;

    given()
        .contentType("application/json")
        .body(updatedProduct)
        .when()
        .put(PRODUCT_PATH + "/" + productId)
        .then()
        .statusCode(200);

    given()
        .when()
        .get(PRODUCT_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .body("name", equalTo(updatedName))
        .body("price", equalTo(100.00F));
  }

  @Test
  @DisplayName("Should list products after creation")
  void testListProductsAfterCreation() {
    createProductAndReturnId(uniqueName("Listable Product"), null, null, 0);

    given()
        .when()
        .get(PRODUCT_PATH)
        .then()
        .statusCode(200)
        .body("size()", greaterThan(0));
  }

  private Long createProductAndReturnId(String name, String description, BigDecimal price, Integer stock) {
    Product newProduct = new Product();
    newProduct.name = name;
    newProduct.description = description;
    newProduct.price = price;
    newProduct.stock = stock == null ? 0 : stock;

    Number productIdValue = given()
        .contentType("application/json")
        .body(newProduct)
        .when()
        .post(PRODUCT_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    return productIdValue.longValue();
  }

  private String uniqueName(String base) {
    String suffix = String.valueOf(System.nanoTime());
    String candidate = base + "-" + suffix;
    return candidate.length() > 40 ? candidate.substring(0, 40) : candidate;
  }
}
