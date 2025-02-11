package com.danieloliveira.starwarsplanetapi.domain;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

public class QueryBuilder {

    // o construtor dele deve ser privado, pois ela é uma classe estática
    // assim o jacoco não vai dizer que o construtor dela nunca é chamado
    private QueryBuilder() {
    }

    public static Example<Planet> makeQuery(Planet planet) {
        ExampleMatcher exampleMatcher = ExampleMatcher.matchingAll().withIgnoreCase().withIgnoreNullValues();
        return Example.of(planet, exampleMatcher);
    }
}
