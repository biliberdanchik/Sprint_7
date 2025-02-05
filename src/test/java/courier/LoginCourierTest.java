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
import static org.hamcrest.CoreMatchers.notNullValue;

public class LoginCourierTest {

    private ScooterServiceClient client;
    private Courier courier;
    private Credentials credentials;
    private Faker faker;
    private boolean dataCleanupFlag;
    ValidatableResponse responseCreateCourier;
    ValidatableResponse responseLoginCourier;
    private String courierId;

    private static final String BASE_URI = "https://qa-scooter.praktikum-services.ru/";

    @Before
    public void prepareData() {
        client = new ScooterServiceClient(BASE_URI);
        faker = new Faker();
    }

    @Test
    @DisplayName("Успешная авторизация курьера")
    public void loginCourierSuccessful() {
        courier = new Courier(faker.name().username(), faker.bothify("#?#?#?#"), faker.name().firstName());
        credentials = Credentials.fromCourier(courier);
        responseCreateCourier = client.createCourier(courier);
        decisionCleanupTestData(responseCreateCourier);
        responseLoginCourier = client.loginCourier(credentials);
        responseLoginCourier.assertThat()
                .statusCode(200)
                .body("id", notNullValue());
        courierId = responseLoginCourier.extract().jsonPath().getString("id");
    }

    @Test
    @DisplayName("Авторизация без указания логина")
    public void loginCourierWithoutLoginUnsuccessful() {
        courier = new Courier(null, faker.bothify("#?#?#?#"), faker.name().firstName());
        credentials = Credentials.fromCourier(courier);
        client.loginCourier(credentials).assertThat()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Авторизация без указания пароля")
    public void loginCourierWithoutPassUnsuccessful() {
        courier = new Courier(faker.name().username(), null, faker.name().firstName());
        credentials = Credentials.fromCourier(courier);
        client.loginCourier(credentials).assertThat()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Авторизация c некорректным паролем")
    public void loginCourierPassIncorrectUnsuccessful() {
        courier = new Courier(faker.name().username(), faker.bothify("#?#?#?#"), faker.name().firstName());
        credentials = Credentials.fromCourier(courier);
        responseCreateCourier = client.createCourier(courier);
        decisionCleanupTestData(responseCreateCourier);
        //Вызываем авторизацию для получения id курьера пока учетные данные валидные
        responseLoginCourier = client.loginCourier(credentials);
        courierId = responseLoginCourier.extract().jsonPath().getString("id");
        //Изменяем пароль в учетных данных
        courier.setPassword(faker.bothify("#?#?#?#?#"));
        credentials = Credentials.fromCourier(courier);
        //Вызываем авторизацию с невалидным паролем
        responseLoginCourier = client.loginCourier(credentials);
        responseLoginCourier.assertThat()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));

    }

    @Test
    @DisplayName("Авторизация несуществующего курьера")
    public void loginNonexistentCourierUnsuccessful() {
        credentials = new Credentials(faker.name().username(), faker.bothify("#?#?#?#"));
        client.loginCourier(credentials).assertThat()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @After
    public void deleteCourierAfterTest() {
        if (dataCleanupFlag) {
            client.deleteCourier(courierId);
        }
    }

    public void decisionCleanupTestData(ValidatableResponse response) {
        int responseCode = response.extract().statusCode();
        if (responseCode == 201) {
            dataCleanupFlag = true;
        }
    }
}
