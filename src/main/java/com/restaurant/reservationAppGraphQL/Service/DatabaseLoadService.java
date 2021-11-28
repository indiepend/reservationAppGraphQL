package com.restaurant.reservationAppGraphQL.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.reservationAppGraphQL.Model.RestaurantTable;
import com.restaurant.reservationAppGraphQL.Repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DatabaseLoadService {

    private final RestaurantTableRepository restaurantTableRepository;
    private final ObjectMapper mapper;

    @Value("${application.restaurant.tables.json.filename}")
    private String file;

    @PostConstruct
    public void loadJsonToObjects() {
        List<RestaurantTable> tables = new ArrayList<>();
        try (InputStream inputStream = this.getClass().getResourceAsStream(file)){
            String jsonFile = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            tables = mapper.readValue(jsonFile, new TypeReference<>(){});
        }catch(IOException ex){
            log.warn("Can't load resource json file in method [DatabaseLoadService.loadJsonToObjects] " + ex.getMessage() + ex.getCause());
        }
        restaurantTableRepository.saveAllAndFlush(tables);
        log.info("Resource json file has been loaded");
    }
}