package com.udacity.vehicles.service;

import com.udacity.vehicles.domain.Location;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class MapService {
    private WebClient maps;

    public MapService(WebClient maps) {
        this.maps = maps;
    }

    public JSONObject getMapServiceData(){
//   Request triggered
        Mono<String> mapServiceResult = maps
                .get()
                .uri("/maps?lat=0&lon=0")
                .retrieve()
                .bodyToMono(String.class);
//    Response received and returned
        Object obj= JSONValue.parse(mapServiceResult.block());
        JSONObject jsonObject = (JSONObject) obj;
        return jsonObject;
    }

    public Location getLocationData(){
        Location locationData = new Location();
        locationData.setAddress(String.valueOf(this.getMapServiceData().get("address")));
        locationData.setCity(String.valueOf(this.getMapServiceData().get("city")));
        locationData.setState(String.valueOf(this.getMapServiceData().get("state")));
        locationData.setZip(String.valueOf(this.getMapServiceData().get("zip")));
        return locationData;
    }
}
