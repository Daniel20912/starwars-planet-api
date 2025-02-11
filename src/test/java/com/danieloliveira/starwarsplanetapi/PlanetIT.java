package com.danieloliveira.starwarsplanetapi;

import com.danieloliveira.starwarsplanetapi.domain.Planet;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static com.danieloliveira.starwarsplanetapi.common.PlanetConstants.PLANET;
import static com.danieloliveira.starwarsplanetapi.common.PlanetConstants.TATOOINE;

// TESTES SUBCUTÂNEOS

/*
    sobe um servidor de aplicação dos testes e cria o contexto de aplicação do spring e coloca todos os beans nele
    Isso significa que todas as configurações, beans, serviços e componentes registrados na aplicação serão inicializados
    Para testar componentes individuais, como serviços ou repositórios, prefira @MockBean e @DataJpaTest para evitar sobrecarga
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// define uma porta aleatória para rodar o servidor de testes
@ActiveProfiles("it") // vai usar o application properties "it"
@Sql(scripts = {"/remove_planets.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
// limpa a tabela após cada teste
@Sql(scripts = {"/import_planets.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
// insere dados na tabela antes de cada teste
public class PlanetIT {

    @Autowired
    private TestRestTemplate restTemplate; // é uma classe do Spring Boot usada para fazer requisições HTTP em testes de integração

    @Test
    // verifica se o contexto de aplicação foi carregado com sucesso
    // pode ficar vazio, pois ao usar o SpringBootTest e o Test, que o Spring vai testar o carregamento do contexto da aplicação
    public void contextLoads() {
    }


    @Test
    public void createPlanet_WithValidData_ReturnsPlanet() {
        ResponseEntity<Planet> sut = restTemplate.postForEntity("/planets", PLANET, Planet.class); // url da requisição, corpo da requisição, tipo de resposta da requisição

        Assertions.assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(sut.getBody().getId()).isNotNull();
        Assertions.assertThat(sut.getBody().getName()).isEqualTo(PLANET.getName());
        Assertions.assertThat(sut.getBody().getClimate()).isEqualTo(PLANET.getClimate());
        Assertions.assertThat(sut.getBody().getTerrain()).isEqualTo(PLANET.getTerrain());

    }

    @Test
    public void getPlanet_WithValidData_ReturnsPlanet() {
        ResponseEntity<Planet> sut = restTemplate.getForEntity("/planets/1", Planet.class); // url da requisição, tipo de resposta da requisição

        Assertions.assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(sut.getBody()).isEqualTo(TATOOINE);

    }


    @Test
    public void getPlanetByName_ReturnsPlanet() {
        ResponseEntity<Planet> sut = restTemplate.getForEntity("/planets/name/" + TATOOINE.getName(), Planet.class); // url da requisição, tipo de resposta da requisição

        Assertions.assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(sut.getBody()).isEqualTo(TATOOINE);
    }

    @Test
    public void listPlanets_ReturnsAllPlanets() {
        ResponseEntity<Planet[]> sut = restTemplate.getForEntity("/planets", Planet[].class); // url da requisição, tipo de resposta da requisição

        Assertions.assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(sut.getBody()).hasSize(3);
        Assertions.assertThat(sut.getBody()[0]).isEqualTo(TATOOINE);
    }

    @Test
    public void listPlanets_ByClimate_ReturnsPlanets() {
        ResponseEntity<Planet[]> sut = restTemplate.getForEntity("/planets?climate=" + TATOOINE.getClimate(), Planet[].class); // url da requisição, tipo de resposta da requisição

        Assertions.assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(sut.getBody()).hasSize(1);
        Assertions.assertThat(sut.getBody()[0]).isEqualTo(TATOOINE);
    }

    @Test
    public void listPlanets_ByTerrain_ReturnsPlanets() {
        ResponseEntity<Planet[]> sut = restTemplate.getForEntity("/planets?terrain=" + TATOOINE.getTerrain(), Planet[].class);

        Assertions.assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(sut.getBody()).hasSize(1);
        Assertions.assertThat(sut.getBody()[0]).isEqualTo(TATOOINE);
    }

    @Test
    public void removePlanet_ReturnsNoContent() {
        ResponseEntity<Void> sut = restTemplate.exchange("/planets/" + TATOOINE.getId(), HttpMethod.DELETE, null, Void.class);

        Assertions.assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

}
