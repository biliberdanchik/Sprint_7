package courier;

import client.ScooterServiceClient;
import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.Courier;
import model.Credentials;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;

public class CreateCourierTest {

    private ScooterServiceClient client;
    private Courier courier;
    private static final String BASE_URI = "https://qa-scooter.praktikum-services.ru/";
    private Credentials credentials;
    private Faker faker;
    ValidatableResponse response;
    private boolean dataCleanupFlag;

    @Before
    public void prepareData() {
        client = new ScooterServiceClient(BASE_URI);
        faker = new Faker();
    }

    @Test
    @DisplayName("Успешное создание нового курьера")
    public void createCourierSuccessful() {
        courier = new Courier(faker.name().username(), faker.bothify("#?#?#?#"), faker.name().firstName());
        response = client.createCourier(courier);
        decisionCleanupTestData(response);
        response.assertThat()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Test
    @DisplayName("Проверка на создание дублирующего курьера")
    public void createCourierDoubleUnsuccessful() {
        courier = new Courier(faker.name().username(), faker.bothify("#?#?#?#"), faker.name().firstName());
        response = client.createCourier(courier);
        decisionCleanupTestData(response);
        response = client.createCourier(courier);
        response.assertThat()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Test
    @DisplayName("В запросе на создание курьера отсутствует логин")
    public void createCourierWithoutLoginUnsuccessful() {
        courier = new Courier(null, faker.bothify("#?#?#?#"), faker.name().firstName());
        response = client.createCourier(courier);
        decisionCleanupTestData(response);
        response.assertThat()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("В запросе на создание курьера отсутствует пароль")
    public void createCourierWithoutPassUnsuccessful() {
        courier = new Courier(faker.name().username(), null, faker.name().firstName());
        response = client.createCourier(courier);
        decisionCleanupTestData(response);
        response.assertThat()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("В запросе на создание курьера отсутствует имя")
    public void createCourierWithoutNameUnsuccessful() {
        courier = new Courier(faker.name().username(), faker.bothify("#?#?#?#"), null);
        response = client.createCourier(courier);
        decisionCleanupTestData(response);
        response.assertThat()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @After
    public void deleteCourierAfterTest() {
        if (dataCleanupFlag) {
            credentials = Credentials.fromCourier(courier);
            String id = client.loginCourier(credentials).extract().jsonPath().getString("id");
            client.deleteCourier(id);
        }
    }

    public void decisionCleanupTestData(ValidatableResponse response) {
        int responseCode = response.extract().statusCode();
        if (responseCode == 201) {
            dataCleanupFlag = true;
        }
    }

}

