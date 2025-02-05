package orders;

import client.ScooterServiceClient;
import com.github.javafaker.Faker;
import io.restassured.response.ValidatableResponse;
import model.Courier;
import model.Credentials;
import model.Order;
import model.Orders;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;

public class GetListOfOrdersTest {

    private ScooterServiceClient client;
    private Courier courier;
    private Credentials credentials;
    private Faker faker;

    private String courierId;
    private List<String> ordersIdList;

    private static final String BASE_URI = "https://qa-scooter.praktikum-services.ru/";

    @Before
    public void prepareData() {
        client = new ScooterServiceClient(BASE_URI);
        faker = new Faker();
        courier = new Courier(faker.name().username(), faker.bothify("#?#?#?#"), faker.name().firstName());
        credentials = Credentials.fromCourier(courier);
        ordersIdList = new ArrayList<>();
    }

    @Test
    public void checkListOfOrdersInBodyResponse() {
        //Создали курьера и сохранили его ID
        client.createCourier(courier);
        courierId = client.loginCourier(credentials).extract().jsonPath().getString("id");
        System.out.println(courierId);

        //Создали 3 заказа и сохранили их ID
        for (int i = 0; i < 3; i++) {
            Order order = newOrder();
            ValidatableResponse responseCreateOrder = client.createOrder(order);
            String track = responseCreateOrder.extract().jsonPath().getString("track");
            ValidatableResponse responseGetOrderByTrack = client.getOrderByTrack(track);
            String id = responseGetOrderByTrack.extract().jsonPath().getString("order.id");
            ordersIdList.add(id);
        }

        //Берем созданные заказы в работу курьером
        for (String orderId: ordersIdList) {
            client.takeOrderToWork(orderId, courierId);
        }

        Orders orders = client.getListOfOrders(courierId).extract().body().as(Orders.class);
        //Проверяем, что размер листа с заказами непустой
        MatcherAssert.assertThat(orders.getOrders().size(), not(0));
        //Проверяем, что поля id, courierId, Track объекта Order не пустые
        for (Order order: orders.getOrders()) {
            MatcherAssert.assertThat(order.getId(), notNullValue());
            MatcherAssert.assertThat(order.getCourierId(), notNullValue());
            MatcherAssert.assertThat(order.getTrack(), notNullValue());
        }

    }

    public static List<String> randomColor() {
        String[] mainColor = {"BLACK", "GRAY"};
        Random random = new Random();
        return List.of(mainColor[random.nextInt(2)]);
    }
    public  Order newOrder() {
        SimpleDateFormat SDFormat = new SimpleDateFormat("yyyy-MM-dd");
        return new Order(faker.name().firstName(),
                faker.name().lastName(),
                faker.address().fullAddress(),
                faker.number().numberBetween(1, 10),
                faker.phoneNumber().phoneNumber(),
                faker.number().numberBetween(1, 10),
                SDFormat.format(faker.date().future(10, TimeUnit.DAYS)),
                faker.bothify("#?#?#?#"),
                randomColor());
    }

    @After
    public void deleteTestData() {
        client.deleteCourier(courierId);
        for (String orderId: ordersIdList) {
            client.finishOrder(orderId);
        }
    }
}
