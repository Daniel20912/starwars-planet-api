package com.danieloliveira.starwarsplanetapi.web;

import com.danieloliveira.starwarsplanetapi.domain.Planet;
import com.danieloliveira.starwarsplanetapi.domain.PlanetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/planets")
public class PlanetController {

    @Autowired
    private PlanetService planetService;

    @PostMapping
    public ResponseEntity<Planet> create(@RequestBody @Valid Planet planet) {
        Planet planetCreated = planetService.create(planet);
        return ResponseEntity.status(HttpStatus.CREATED).body(planetCreated);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Planet> getPlanetById(@PathVariable Long id) {
        return planetService.get(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("name/{name}")
    public ResponseEntity<Planet> getByName(@PathVariable String name) {
        return planetService.getByName(name).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Planet>> getAll(@RequestParam(required = false) String terrain, @RequestParam(required = false) String climate) {
        List<Planet> planets = planetService.list(terrain, climate);
        return ResponseEntity.ok(planets);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable("id") Long id) {
        planetService.remove(id);
        return ResponseEntity.noContent().build();
    }

}
