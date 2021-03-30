package com.udacity.vehicles.api;


import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udacity.vehicles.VehiclesApiApplication;
import com.udacity.vehicles.client.prices.Price;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.manufacturer.Manufacturer;
import com.udacity.vehicles.domain.manufacturer.ManufacturerRepository;
import com.udacity.vehicles.service.CarNotFoundException;
import com.udacity.vehicles.service.CarService;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;

import com.udacity.vehicles.service.MapService;
import com.udacity.vehicles.service.PriceService;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.logging.Log;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implements a REST-based controller for the Vehicles API.
 */
@RestController
@RequestMapping("/cars")
class CarController {

    private final CarService carService;
    private final CarResourceAssembler assembler;
    private final PriceService priceService;
    private final MapService mapService;

    CarController(CarService carService, CarResourceAssembler assembler, PriceService priceService, MapService mapService) {
        this.carService = carService;
        this.assembler = assembler;
        this.priceService = priceService;
        this.mapService = mapService;
    }
/*
//for testing purpose
    @GetMapping("/test")
    JSONObject test() {
        WebClient client = VehiclesApiApplication.webClientPricing("http://localhost:8761");
//        creating Price object to send to microservice
        Price priceObj = new Price();
        priceObj.setPrice(BigDecimal.valueOf(224000.23));
        priceObj.setCurrency("INR");
        priceObj.setVehicleId(23L);

//        calling microservice to do testing
        Mono<String> priceServiceResult = client.get()
                .uri("/prices")
//                .body(Mono.just(priceObj),Price.class)
                .retrieve()
                .bodyToMono(String.class);

        Object obj= JSONValue.parse(priceServiceResult.block());
        JSONObject jsonObject = (JSONObject) obj;
//        System.out.println(jsonObject);
        System.out.println("Exiting..");
        return jsonObject;
    }*/
    /**
     * Creates a list to store any vehicles.
     * @return list of vehicles
     */

    @GetMapping
    Resources<Resource<Car>> list() {
//        fetching price from pricing service and append those values to corresponding car objects
//        assigned null values if there is any issues while fetching record from pricing service
        List<Car> listOfCar = carService.list();
        for(int i =0;i<listOfCar.size();i++){
            try{
                String fetchedPriceFromPriceService = priceService.getPriceById(listOfCar.get(i).getId());
                listOfCar.get(i).setPrice(fetchedPriceFromPriceService);
                listOfCar.get(i).setLocation(mapService.getLocationData());
            }
           catch (Exception e){
                System.out.println("Some issues while fetching price for given id, assigning null "+listOfCar.get(i).getId());
                listOfCar.get(i).setPrice(null);
           }
        }
        List<Resource<Car>> resources = listOfCar.stream().map(assembler::toResource)
                .collect(Collectors.toList());
        return new Resources<>(resources,
                linkTo(methodOn(CarController.class).list()).withSelfRel());
    }

    /**
     * Gets information of a specific car by ID.
     * @param id the id number of the given vehicle
     * @return all information for the requested vehicle
     */
    @GetMapping("/{id}")
    Resource<Car> get(@PathVariable Long id) {
        /**
         * TODO: Use the `findById` method from the Car Service to get car information.
         * TODO: Use the `assembler` on that car and return the resulting output.
         *   Update the first line as part of the above implementing.
         */
        Car carObjectReturnedFromService = carService.findById(id);
/*
Triggered Mircroservice to fetch the details of price for corresponding vehicle id
and set to currently recieved car object and return as response
 */
        String price = priceService.getPriceById(id);
        carObjectReturnedFromService.setPrice(price);
        carObjectReturnedFromService.setLocation(mapService.getLocationData());
        return assembler.toResource(carObjectReturnedFromService);
    }

    /**
     * Posts information to create a new vehicle in the system.
     * @param car A new vehicle to add to the system.
     * @return response that the new vehicle was added to the system
     * @throws URISyntaxException if the request contains invalid fields or syntax
     */
    @PostMapping
//    ResponseEntity<URI> post(@Valid @RequestBody Car car) throws URISyntaxException {
    Resource<Car> post(@Valid @RequestBody Car car) throws URISyntaxException {
//      save car details to JPA
        Car savedCarOBJ = carService.save(car);
//        System.out.println("ID is "+savedCarOBJ.getId());
//        Save the vehicle pricing details to price microservice
        priceService.postPriceById(savedCarOBJ.getId(),car.getPrice());
//        return response in assembler format
        savedCarOBJ.setPrice(car.getPrice());
        savedCarOBJ.setLocation(mapService.getLocationData());
//        savedCarOBJ;
        return assembler.toResource(savedCarOBJ);
        /**
         * TODO: Use the `save` method from the Car Service to save the input car.
         * TODO: Use the `assembler` on that saved car and return as part of the response.
         *   Update the first line as part of the above implementing.
         */
//        Resource<Car> resource = assembler.toResource(new Car());
//        return ResponseEntity.created(new URI(resource.getId().expand().getHref())).body(resource);
    }



    /**
     * Updates the information of a vehicle in the system.
     * @param id The ID number for which to update vehicle information.
     * @param car The updated information about the related vehicle.
     * @return response that the vehicle was updated in the system
     */
    @PutMapping("/{id}")
    ResponseEntity<?> put(@PathVariable Long id, @Valid @RequestBody Car car) {
        /**
         * TODO: Set the id of the input car object to the `id` input.
         * TODO: Save the car using the `save` method from the Car service
         * TODO: Use the `assembler` on that updated car and return as part of the response.
         *   Update the first line as part of the above implementing.
         */
        if (carService.findById(id).getId() == id){
            carService.save(car);
            priceService.postPriceById(id,car.getPrice());
            car.setLocation(mapService.getLocationData());
            Resource<Car> resource = assembler.toResource(car);
            return ResponseEntity.ok(resource);
        }
        else{
            throw new CarNotFoundException();
        }

    }

    /**
     * Removes a vehicle from the system.
     * @param id The ID number of the vehicle to remove.
     * @return response that the related vehicle is no longer in the system
     */
    @DeleteMapping("/{id}")
    JSONObject delete(@PathVariable Long id) {
        /**
         * TODO: Use the Car Service to delete the requested vehicle.
         */
        carService.delete(id);
        priceService.deletePriceById(id);
//        Custom JSONObject for Response
        Car deletedEntity = carService.findById(id);
        JSONObject deleteMessageObj = new JSONObject();
        deleteMessageObj.appendField("message","Deleted Successfully");
        deleteMessageObj.appendField("data", deletedEntity);
//        return ResponseEntity.noContent().build();
        return deleteMessageObj;
    }
}
