package client;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import model.Courier;
import model.Credentials;

import static io.restassured.RestAssured.given;

public class ScooterServiceClient {

    private String baseURI;

    public ScooterServiceClient(String baseURI) {
        this.baseURI = baseURI;
    }

    public ValidatableResponse createCourier (Courier courier) {
        return given()
                .log()
                .all()
                .baseUri(baseURI)
                .header("Content-Type", "application/json")
                .body(courier)
                .post("/api/v1/courier")
                .then()
                .log()
                .all();
    }

    public ValidatableResponse loginCourier (Credentials credentials) {
        return given()
                .log()
                .all()
                .baseUri(baseURI)
                .header("Content-Type", "application/json")
                .body(credentials)
                .post("/api/v1/courier/login")
                .then()
                .log()
                .all();
    }

    public ValidatableResponse deleteCourier (String id) {
        return given()
                .log()
                .all()
                .baseUri(baseURI)
                //.queryParam("id", id)
                .delete("/api/v1/courier/"+ id)
                .then()
                .log()
                .all();
    }




}
