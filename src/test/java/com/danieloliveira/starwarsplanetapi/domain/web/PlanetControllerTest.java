package com.danieloliveira.starwarsplanetapi.domain.web;

import com.danieloliveira.starwarsplanetapi.domain.Planet;
import com.danieloliveira.starwarsplanetapi.domain.PlanetService;
import com.danieloliveira.starwarsplanetapi.web.PlanetController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.danieloliveira.starwarsplanetapi.common.PlanetConstants.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@WebMvcTest(PlanetController.class)
// usada para fazer requisições http (ele injeta o controlador e monta um contexto web para interagir)
public class PlanetControllerTest {

    // Ele também disponibiliza um cliente http para interagir nesse contexto
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlanetService planetService;

    @Test
    public void createPlanet_ComDadosValidos_returnaCreated() throws Exception {
        when(planetService.create(PLANET)).thenReturn(PLANET);

        // o post recebe a url para criar o planeta e o planeta de content que é os dados que serão usados para passar o planeta
        // o objectMapper transforma o PLANET em json
        mockMvc.perform(MockMvcRequestBuilders.post("/planets").content(objectMapper.writeValueAsString(PLANET)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$").value(PLANET)); // verifica que o jsonpath do objeto raiz da resposta ("$") vai ter um valor igual ao do PLANET
    }

    @Test
    public void createPlanet_ComDadosInvalidos_returnaBadRequest() throws Exception {
        Planet emptyPlanet = new Planet();
        Planet invalidPlanet = new Planet("", "", "");

        mockMvc.perform(MockMvcRequestBuilders.post("/planets").content(objectMapper.writeValueAsString(emptyPlanet)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());


        mockMvc.perform(MockMvcRequestBuilders.post("/planets").content(objectMapper.writeValueAsString(invalidPlanet)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }

    @Test
    public void criarPlaneta_ComNomeExistente_returnaConflito() throws Exception {
        when(planetService.create(any())).thenThrow(DataIntegrityViolationException.class);

        mockMvc.perform(MockMvcRequestBuilders.post("/planets")
                        .content(objectMapper.writeValueAsString(PLANET))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    public void getPlanets_ComIdExistente_returnaPlaneta() throws Exception {
        when(planetService.get(1L)).thenReturn(Optional.of(PLANET));

        mockMvc.perform(MockMvcRequestBuilders.get("/planets/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").value(PLANET));
    }


    @Test
    public void getPlanets_PorNomeExistente_retornaPlaneta() throws Exception {
        when(planetService.getByName(PLANET.getName())).thenReturn(Optional.of(PLANET));

        mockMvc.perform(MockMvcRequestBuilders.get("/planets/name/" + PLANET.getName()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").value(PLANET));
    }


    @Test
    public void getPlanets_ComNomeInexistente_throw404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/planets/name/1"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void listPlanets_ReturnsFilteredPlanets() throws Exception {
        when(planetService.list(null, null)).thenReturn(PLANETS);
        when(planetService.list(TATOOINE.getTerrain(), TATOOINE.getClimate())).thenReturn(List.of(TATOOINE));

        mockMvc.perform(MockMvcRequestBuilders.get("/planets"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(3)));

        mockMvc.perform(MockMvcRequestBuilders.get("/planets?" + String.format("terrain=%s&climate=%s", TATOOINE.getTerrain(), TATOOINE.getClimate())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value(TATOOINE));
    }

    @Test
    public void listPlanets_ReturnsNoPlanets() throws Exception {
        when(planetService.list(null, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/planets"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(0)));
    }


    @Test
    public void removePlanet_WithExistingId_ReturnsNoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/planets/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void removePlanet_WithUnexistingId_ReturnsNotFound() throws Exception {
        final Long planetId = 1L;

        doThrow(new EmptyResultDataAccessException(1)).when(planetService).remove(planetId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/planets/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
