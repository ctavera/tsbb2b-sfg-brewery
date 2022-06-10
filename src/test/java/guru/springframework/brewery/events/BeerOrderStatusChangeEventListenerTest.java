package guru.springframework.brewery.events;

import com.github.jenspiegsa.wiremockextension.Managed;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import guru.springframework.brewery.domain.BeerOrder;
import guru.springframework.brewery.domain.OrderStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({WireMockExtension.class})
class BeerOrderStatusChangeEventListenerTest {

    @Managed
    WireMockServer wireMockServer = with(wireMockConfig().dynamicPort());
    BeerOrderStatusChangeEventListener beerOrderStatusChangeEventListener;

    @BeforeEach
    void setUp() {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        beerOrderStatusChangeEventListener = new BeerOrderStatusChangeEventListener(restTemplateBuilder);
    }

    @Test
    void listen() {

        wireMockServer.stubFor(post("/update").willReturn(ok()));

        BeerOrder beerOrder = BeerOrder.builder()
                .orderStatus(OrderStatusEnum.READY)
                .orderStatusCallbackUrl("http://localhost:" + wireMockServer.port() + "/update")
                .createdDate(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        BeerOrderStatusChangeEvent beerOrderStatusChangeEvent = new BeerOrderStatusChangeEvent(beerOrder, OrderStatusEnum.NEW);

        beerOrderStatusChangeEventListener.listen(beerOrderStatusChangeEvent);

        verify(1, postRequestedFor(urlEqualTo("/update")));
    }
}