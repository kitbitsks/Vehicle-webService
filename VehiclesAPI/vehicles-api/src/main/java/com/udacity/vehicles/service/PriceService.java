package com.udacity.vehicles.service;

import com.udacity.vehicles.client.prices.Price;
import com.udacity.vehicles.domain.car.Car;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class PriceService {
    private WebClient pricing;

    public PriceService(WebClient pricing) {
        this.pricing = pricing;
    }

    public String getPriceById(long id){
//   Request triggered
        Mono<String> priceServiceResult = pricing
                .get()
                .uri("/prices/"+id)
                .retrieve()
                .bodyToMono(String.class);
//    Response received and returned
        Object obj= JSONValue.parse(priceServiceResult.block());
        JSONObject jsonObject = (JSONObject) obj;
        return String.valueOf(jsonObject.get("price"));
    }

    public JSONObject postPriceById(long id, String price){
        //Object created to be inserted as in PriceMicroService DB
        Price priceObj = new Price();
        priceObj.setPrice(new BigDecimal(price));
        priceObj.setCurrency("INR");
        priceObj.setVehicleId(id);

//   Request triggered
        Mono<String> priceServiceResult = pricing
                .post()
                .uri("/prices")
                .body(Mono.just(priceObj),Price.class)
                .retrieve()
                .bodyToMono(String.class);

//    Response received and returned
        Object obj= JSONValue.parse(priceServiceResult.block());
        JSONObject jsonObject = (JSONObject) obj;
        return jsonObject;
    }

    public JSONObject deletePriceById(long id){
//   Request triggered
        Mono<String> priceServiceResult = pricing
                .delete()
                .uri("/prices/"+id)
                .retrieve()
                .bodyToMono(String.class);
//    Response received and returned
        Object obj= JSONValue.parse(priceServiceResult.block());
        JSONObject jsonObject = (JSONObject) obj;
        return jsonObject;
    }
}
