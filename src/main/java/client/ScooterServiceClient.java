package client;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import model.Courier;
import model.Credentials;
import model.Order;

import static io.restassured.RestAssured.given;

public class ScooterServiceClient {

    private final String baseURI;

    public ScooterServiceClient(String baseURI) {
        this.baseURI = baseURI;
    }

    @Step("Клиент – создание курьера")
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

    @Step("Клиент – авторизация курьера")
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

    @Step("Клиент – удаление курьера")
    public void deleteCourier (String id) {
        if (id == null) {
            System.out.println("Не получен идентификатор курьера для удаления");
        } else {
            given()
                    .log()
                    .all()
                    .baseUri(baseURI)
                    //.queryParam("id", id)
                    .delete("/api/v1/courier/" + id)
                    .then()
                    .log()
                    .all();
        }
    }

    @Step("Клиент – создание заказа")
    public ValidatableResponse createOrder(Order order) {
        return given()
                .log()
                .all()
                .baseUri(baseURI)
                .header("Content-Type", "application/json")
                .body(order)
                .post("/api/v1/orders")
                .then()
                .log()
                .all();
    }

    @Step("Клиент – отмена заказа")
    public void cancelOrder(String track) {
        given()
                .log()
                .all()
                .baseUri(baseURI)
                .queryParam("track", track)
                .put("/api/v1/orders/cancel")
                .then()
                .log()
                .all();
    }

    @Step("Клиент – получение заказа по трек-номеру")
    public ValidatableResponse getOrderByTrack(String track) {
        return given()
                .log()
                .all()
                .baseUri(baseURI)
                .queryParam("t", track)
                .get("/api/v1/orders/track")
                .then()
                .log()
                .all();
    }

    @Step("Клиент – назначение заказа курьеру")
    public void takeOrderToWork(String orderID, String courierId) {
        given()
                .log()
                .all()
                .baseUri(baseURI)
                .queryParam("courierId", courierId)
                .put("/api/v1/orders/accept/" + orderID)
                .then()
                .log()
                .all();
    }

    @Step("Клиент – получение списка заказов курьера")
    public ValidatableResponse getListOfOrders(String courierId) {
        return given()
                .log()
                .all()
                .baseUri(baseURI)
                .queryParam("courierId", courierId)
                .get("/api/v1/orders")
                .then()
                .log()
                .all();
    }

    @Step("Клиент – завершение заказа")
    public void finishOrder(String orderId) {
        given()
                .log()
                .all()
                .baseUri(baseURI)
                .put("/api/v1/orders/finish/" + orderId)
                .then()
                .log()
                .all();
    }
}
