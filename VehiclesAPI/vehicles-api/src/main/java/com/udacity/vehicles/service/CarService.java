package com.udacity.vehicles.service;

import com.udacity.vehicles.VehiclesApiApplication;
import com.udacity.vehicles.client.prices.Price;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import java.util.List;
import java.util.Optional;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.hibernate.service.spi.InjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    private final CarRepository repository;
    private  WebClient pricing;
    private final WebClient maps;
    private final PriceService priceService;

    public CarService(CarRepository repository, WebClient maps, WebClient pricing, PriceService priceService) {
        /**
         * TODO: Add the Maps and Pricing Web Clients you create
         *   in `VehiclesApiApplication` as arguments and set them here.
         */
        this.repository = repository;
        this.maps = maps;
        this.pricing = pricing;
        this.priceService = priceService;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return repository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        /**
         * TODO: Find the car by ID from the `repository` if it exists.
         *   If it does not exist, throw a CarNotFoundException
         *   Remove the below code as part of your implementation.
         */
        Optional<Car> returningObj = repository.findById(id);
        return returningObj.stream().findFirst().orElseThrow(CarNotFoundException::new);
        /**
         * TODO: Use the Pricing Web client you create in `VehiclesApiApplication`
         *   to get the price based on the `id` input'
         * TODO: Set the price of the car
         * Note: The car class file uses @transient, meaning you will need to call
         *   the pricing service each time to get the price.
         */
        //Pricing webservice call
//        WebClient client = ("http://localhost:8761")
//        creating Price object to send to microservice
//        Price priceObj = new Price();
//        priceObj.setPrice(BigDecimal.valueOf(224000.23));
//        priceObj.setCurrency("INR");
//        priceObj.setVehicleId(23L);

//        calling microservice to do testing
//        Mono<String> priceServiceResult = pricing.get().uri("/prices").retrieve().bodyToMono(String.class);
//        Object obj= JSONValue.parse(priceServiceResult.block());
//        JSONObject jsonObject = (JSONObject) obj;
//        System.out.println("Result = "+jsonObject);
//        priceService.result();
        /**
         * TODO: Use the Maps Web client you create in `VehiclesApiApplication`
         *   to get the address for the vehicle. You should access the location
         *   from the car object and feed it to the Maps service.
         * TODO: Set the location of the vehicle, including the address information
         * Note: The Location class file also uses @transient for the address,
         * meaning the Maps service needs to be called each time for the address.
         */


//        return car;
//        return returningObj.stream().findFirst().orElseThrow(CarNotFoundException::new);
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
//        System.out.println(repository.findById(car.getId()));
//        if (car.getId() != null) {
            if (repository.findById(car.getId()).isPresent() != false)
            {
                return repository.findById(car.getId())
                        .map(carToBeUpdated -> {
                            carToBeUpdated.setModifiedAt(car.getModifiedAt());
                            carToBeUpdated.setCreatedAt(car.getCreatedAt());
                            carToBeUpdated.setCondition(car.getCondition());
                            carToBeUpdated.setPrice(car.getPrice());
                            carToBeUpdated.setDetails(car.getDetails());
                            carToBeUpdated.setLocation(car.getLocation());
                            System.out.println("Data udated successfully !");
                            return repository.save(carToBeUpdated);

                        }).orElseThrow(CarNotFoundException::new);
            }
            else{
                System.out.println("executed save command");
                return repository.save(car);
            }
//        }
//        return car;
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        /**
         * TODO: Find the car by ID from the `repository` if it exists.
         *   If it does not exist, throw a CarNotFoundException
         */
        if (repository.findById(id).isPresent() == true){
            repository.deleteById(id);
            System.out.println("car_details with id "+id+" deleted successfully !");
        }
        else{
            System.out.println("car_details with id"+id+" not found !");
            throw new CarNotFoundException();
        }

        /**
         * TODO: Delete the car from the repository.
         */


    }
}
