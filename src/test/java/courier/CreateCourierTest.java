package courier;

import client.ScooterServiceClient;
import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import model.Courier;
import model.Credentials;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;

public class CreateCourierTest {

    private ScooterServiceClient client;
    private Courier courier;
    private String baseURI = "https://qa-scooter.praktikum-services.ru/";
    private Credentials credentials;

    @Before
    public void prepareData() {
        client = new ScooterServiceClient(baseURI);
    }

    @Test
    @DisplayName("Успешное создание нового курьера")
    public void createCourierSuccessful() {
        courier = new Courier("goga12345", "1234", "GOGA");
        client.createCourier(courier).assertThat().statusCode(201).body("ok", equalTo(true));
    }

    @Test
    @DisplayName("Проверка на создание дублирующего курьера")
    public void createCourierDoubleUnsuccessful() {
        courier = new Courier("goga12345", "1234", "GOGA");
        client.createCourier(courier);
        client.createCourier(courier).assertThat().statusCode(409).body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Test
    @DisplayName("В запросе на создание курьера отсутствует логин")
    public void createCourierWithoutLoginUnsuccessful() {
        courier = new Courier(null, "1234", "GOGA");
        client.createCourier(courier).assertThat().statusCode(400).body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("В запросе на создание курьера отсутствует пароль")
    public void createCourierWithoutPassUnsuccessful() {
        courier = new Courier("fsadf", null, "GOGA");
        client.createCourier(courier).assertThat().statusCode(400).body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("В запросе на создание курьера отсутствует имя")
    public void createCourierWithoutNameSuccessful() {
        courier = new Courier("fsadf", "1234", null);
        client.createCourier(courier).assertThat().statusCode(201).body("ok", equalTo(true));
    }

    @After
    public void deleteCourierAfterTest() {
        try {
            credentials = new Credentials(courier.getLogin(), courier.getPassword());
            client.deleteCourier(client.loginCourier(credentials).extract().jsonPath().getString("id"));
        } catch (Exception exception) {
            System.out.println("Курьер не был создан, или при удалении возникла ошибка");
        }


    }
}
