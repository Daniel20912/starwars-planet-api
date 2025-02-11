package com.danieloliveira.starwarsplanetapi.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Example;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static com.danieloliveira.starwarsplanetapi.common.PlanetConstants.PLANET;
import static com.danieloliveira.starwarsplanetapi.common.PlanetConstants.TATOOINE;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // usa um banco de dados em memória que é o H2
public class PlanetRepositoryTest {

    @Autowired
    private PlanetRepository planetRepository;

    @Autowired
    private TestEntityManager testEntityManager; // permite interagir com o banco de dados sem ser via repository

    /*
        seta o id do PLANET para nulo após cada teste,
        pois os testes criam um id para o PLANETA quando ele é persistido
        e isso faz quando outro teste precisa fazer outro persist com o PLANET acaba dando um erro,
        pois não há como persistir um objeto que já tem id
     */
    @AfterEach
    public void afterEach() {
        PLANET.setId(null);
    }

    @Test
    public void criarPlaneta_ComDadosValidos_RetornaPlaneta() {
        // após fazer o save é preciso consultar no banco de dados se existe esse planeta, e depois se esse planeta encontrado é igual ao que foi mandado salvar
        // como o planetRepository está sendo testado, não pode usar ele para auxiliar no teste
        Planet planet = planetRepository.save(PLANET);

        Planet sut = testEntityManager.find(Planet.class, planet.getId());

        // não pode testar a igualdade diretamente entre os dois planetas, pois o PLANET não tem id, precisa testar a igualdade dos atributos
        Assertions.assertThat(sut).isNotNull();
        Assertions.assertThat(sut.getName()).isEqualTo(planet.getName());
        Assertions.assertThat(sut.getClimate()).isEqualTo(planet.getClimate());
        Assertions.assertThat(sut.getTerrain()).isEqualTo(planet.getTerrain());
    }

    @Test
    public void criarPlaneta_ComDadosInvalidos_LancaExcessao() {
        Planet emptyPlanet = new Planet();
        Planet invalidPlanet = new Planet("", "", "");

        Assertions.assertThatThrownBy(() -> planetRepository.save(emptyPlanet)).isInstanceOf(RuntimeException.class);
        Assertions.assertThatThrownBy(() -> planetRepository.save(invalidPlanet)).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void criarPlaneta_ComNomeExistente_LancaExcessao() {

        Planet planet = testEntityManager.persistFlushFind(PLANET);

        // faz o planeta parar de ser gerenciado pelo entityManager, se não ele vai entender que o save deve apenas atualizar ele
        testEntityManager.detach(planet);

        // o save serve tanto para criar quanto para atualizar o objeto, para saber se deve atualizar ele checa se o objeto tem id,
        // precisando tirar o id do objeto para ele tentar criar novamente e ver se vai lançar uma excessão devido a um nome já existente
        planet.setId(null);

        Assertions.assertThatThrownBy(() -> planetRepository.save(planet)).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void getPlaneta_ComIdExistente_RetornaPlaneta() {

        Planet planet = testEntityManager.persistFlushFind(PLANET);
        Optional<Planet> planetOptional = planetRepository.findById(planet.getId());

        Assertions.assertThat(planetOptional).isNotEmpty();
        Assertions.assertThat(planetOptional.get().getName()).isEqualTo(planet.getName());
        Assertions.assertThat(planetOptional.get().getClimate()).isEqualTo(planet.getClimate());
        Assertions.assertThat(planetOptional.get().getTerrain()).isEqualTo(planet.getTerrain());
        Assertions.assertThat(planetOptional.get().getId()).isEqualTo(planet.getId());
    }


    @Test
    public void getPlaneta_ComIdInexistente_LancaExcessao() {
        Optional<Planet> planetOptional = planetRepository.findById(1L);

        Assertions.assertThat(planetOptional).isEmpty();
    }

    @Test
    public void getPlaneta_ComNomeExistente_RetornaPlaneta() {

        Planet planet = testEntityManager.persistFlushFind(PLANET);
        Optional<Planet> planetOptional = planetRepository.findByName(planet.getName());

        Assertions.assertThat(planetOptional).isNotEmpty();
        Assertions.assertThat(planetOptional.get().getName()).isEqualTo(planet.getName());
        Assertions.assertThat(planetOptional.get().getClimate()).isEqualTo(planet.getClimate());
        Assertions.assertThat(planetOptional.get().getTerrain()).isEqualTo(planet.getTerrain());
        Assertions.assertThat(planetOptional.get().getId()).isEqualTo(planet.getId());
    }


    @Test
    public void getPlaneta_ComNomeInexistente_LancaExcessao() {
        Optional<Planet> planetOptional = planetRepository.findByName("name");

        Assertions.assertThat(planetOptional).isEmpty();
    }

    @Sql(scripts = "/import_planets.sql")
    @Test
    public void listPlanets_ReturnsFilteredPlanets() {
        Example<Planet> queryWithoutFilters = QueryBuilder.makeQuery(new Planet());
        Example<Planet> queryWithFilters = QueryBuilder.makeQuery(new Planet(TATOOINE.getClimate(), TATOOINE.getTerrain()));

        List<Planet> responseWithoutFilters = planetRepository.findAll(queryWithoutFilters);
        List<Planet> responseWithFilters = planetRepository.findAll(queryWithFilters);

        assertThat(responseWithoutFilters).isNotEmpty();
        assertThat(responseWithoutFilters).hasSize(3);

        assertThat(responseWithFilters).isNotEmpty();
        assertThat(responseWithFilters).hasSize(1);
        assertThat(responseWithFilters.getFirst()).isEqualTo(TATOOINE);
    }

    @Test
    public void listPlanets_ReturnsNoPlanets() {
        Example<Planet> query = QueryBuilder.makeQuery(new Planet());

        List<Planet> response = planetRepository.findAll(query);

        Assertions.assertThat(response).isEmpty();
    }

    @Test
    public void removePlanet_WithExistingId_RemovesPlanetFromDatabase() {
        Planet planet = testEntityManager.persistFlushFind(PLANET);
        planetRepository.deleteById(planet.getId());

        Planet removedPlanet = testEntityManager.find(Planet.class, planet.getId());
        Assertions.assertThat(removedPlanet).isNull();
    }

    @Test
    @Sql(scripts = "/import_planets.sql")

    public void removePlanet_WithNonExistingId_DoestNotChangePlanetList() {
        planetRepository.deleteById(4L);
        assertThat(testEntityManager.find(Planet.class, 1L)).isInstanceOf(Planet.class);
        assertThat(testEntityManager.find(Planet.class, 2L)).isInstanceOf(Planet.class);
        assertThat(testEntityManager.find(Planet.class, 3L)).isInstanceOf(Planet.class);
    }
}
