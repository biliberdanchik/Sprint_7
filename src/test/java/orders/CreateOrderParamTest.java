package orders;

import client.ScooterServiceClient;
import com.github.javafaker.Faker;
import io.restassured.response.ValidatableResponse;
import model.Order;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.notNullValue;

@RunWith(Parameterized.class)
public class CreateOrderParamTest {

    private ScooterServiceClient client;
    private Order order;
    private Faker faker;
    private ValidatableResponse response;
    private String track;

    private final List<String> color;

    private static final String BASE_URI = "https://qa-scooter.praktikum-services.ru/";

    public CreateOrderParamTest(List<String> color) {
        this.color = color;
    }

    @Parameterized.Parameters
    public static List<String>[] getColor() {
        return new List[] {
                List.of("BLACK"),
                List.of("GREY"),
                List.of("BLACK", "GRAY"),
                List.of()
        };
    }

    @Before
    public void prepareData() {
        client = new ScooterServiceClient(BASE_URI);
        faker = new Faker();
    }

    @Test
    public void checkCreateOrderWithDifferentColors() {
        SimpleDateFormat SDFormat = new SimpleDateFormat("yyyy-MM-dd");
        order = new Order(faker.name().firstName(),
                faker.name().lastName(),
                faker.address().fullAddress(),
                faker.number().numberBetween(1,10),
                faker.phoneNumber().phoneNumber(),
                faker.number().numberBetween(1,10),
                SDFormat.format(faker.date().future(10, TimeUnit.DAYS)),
                faker.bothify("#?#?#?#"),
                color);

        response = client.createOrder(order);
        response.assertThat().statusCode(201).body("track", notNullValue());
        track = response.extract().jsonPath().getString("track");
    }

    @After
    public void deleteOrderAfterTest() {
        client.cancelOrder(track);
    }


}
