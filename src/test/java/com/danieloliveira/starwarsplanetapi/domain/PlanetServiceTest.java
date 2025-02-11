package com.danieloliveira.starwarsplanetapi.domain;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.danieloliveira.starwarsplanetapi.common.PlanetConstants.INVALID_PLANET;
import static com.danieloliveira.starwarsplanetapi.common.PlanetConstants.PLANET;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlanetServiceTest {

    @InjectMocks // cria uma instância real do PlanetService e todas as dependências dele são injetadas com mocks
    private PlanetService planetService;

    @Mock
    private PlanetRepository planetRepository;

    @Test // operaçãoQueEstaSendoTestada_parametrosQueElaRecebe_retornoEsperado
    public void createPlanet_ComDadosValidos_ReturnaUmPlaneta() {
        // ARRANGE
        // quando o metodo create no service é chamado ele chama o save do repositório, então nessa linha é específica que quando o metodo save for chamado ele deve retorna o PLANET
        when(planetRepository.save(PLANET)).thenReturn(PLANET);

        // ACT
        Planet sut = planetService.create(PLANET);

        // ASSERT
        Assertions.assertThat(sut).isEqualTo(PLANET);
    }

    @Test
    public void createPlanet_ComDadosInvalidos_LancaException() {
        when(planetRepository.save(INVALID_PLANET)).thenThrow(RuntimeException.class);

        Assertions.assertThatThrownBy(() -> planetService.create(INVALID_PLANET)).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void getPlanetById_PorIdExistente_ReturnaUmPlaneta() {
        when(planetRepository.findById(anyLong())).thenReturn(Optional.of(PLANET));

        Optional<Planet> sut = planetService.get(1L);

        Assertions.assertThat(sut).isNotEmpty();
        Assertions.assertThat(sut.get()).isEqualTo(PLANET);
    }

    @Test
    public void getPlanetById_PorIdInexistente_ReturnaExcessao() {
        // quando o metodo get no service é chamado ele chama o findById do repositório, então nessa linha é específica que quando o findById for chamado ele deve retorna um Optional vazio propositalmente
        when(planetRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Planet> sut = planetService.get(1L);

        Assertions.assertThat(sut).isEmpty();
    }

    @Test
    public void getPlanetById_PorNomeExistente_ReturnaPlaneta() {
        when(planetRepository.findByName("name")).thenReturn(Optional.of(PLANET));

        Optional<Planet> sut = planetService.getByName(PLANET.getName());

        Assertions.assertThat(sut).isNotEmpty();
        Assertions.assertThat(sut.get()).isEqualTo(PLANET);
    }

    @Test
    public void getPlanetById_PorNomeInexistente_ReturnaExcessao() {
        when(planetRepository.findByName("name")).thenReturn(Optional.empty());

        Optional<Planet> sut = planetService.getByName(PLANET.getName());

        Assertions.assertThat(sut).isEmpty();
    }

    @Test
    public void listarPlanetas_ReturnaTodosOsPlanetas() {
        List<Planet> planets = new ArrayList<>() {
            {
                add(PLANET);
            }
        };

        Example<Planet> query = QueryBuilder.makeQuery(new Planet(PLANET.getClimate(), PLANET.getTerrain()));

        when(planetRepository.findAll(query)).thenReturn(planets);

        List<Planet> sut = planetService.list(PLANET.getTerrain(), PLANET.getClimate());

        Assertions.assertThat(sut).isNotEmpty();
        Assertions.assertThat(sut).hasSize(planets.size());
        Assertions.assertThat(sut.getFirst()).isEqualTo(PLANET);

    }

    @Test
    public void listarPlanetas_ReturnaNenhumPlaneta() {
        when(planetRepository.findAll(any())).thenReturn(Collections.emptyList());

        List<Planet> sut = planetService.list(PLANET.getTerrain(), PLANET.getClimate());

        Assertions.assertThat(sut).isEmpty();
    }

    @Test
    public void removePlanet_PorIdExistente_NaoLancaNenhumaExcessao() {
        Assertions.assertThatCode(() -> planetService.remove(1L)).doesNotThrowAnyException();
    }

    @Test
    public void removePlanet_PorIdInexistente_LancaException() {
        doThrow(new RuntimeException()).when(planetRepository).deleteById(99L);
        Assertions.assertThatThrownBy(() -> planetService.remove(99L)).isInstanceOf(RuntimeException.class);
    }

}
