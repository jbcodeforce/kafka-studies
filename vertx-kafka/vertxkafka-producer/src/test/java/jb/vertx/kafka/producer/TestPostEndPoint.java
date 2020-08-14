package jb.vertx.kafka.producer;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
@QuarkusTest
public class TestPostEndPoint {
    
    @Test
    public void testGetOrder(){
        given()
        .when().delete("/orders/1")
        .then()
        .statusCode(200)
        .body(
            containsString("\"id\": \"1\""));
    }
    @Test
    public void testPostOrder(){
        given()
                .when()
                .body("{\"deliveryDate\": \"2020-08-13\",\"deliveryLocation\": \"Milano/Italy\",                   \"priority\": 0,\"quantity\": 10 }")
                .contentType("application/json")
                .post("/orders")
                .then()
                .statusCode(201)
                .body(
                        containsString("\"id\":"),
                        containsString("\"deliveryLocation\":\"Milano/Italy\""));
        ;
    }
}