package com.technofuturtic.tournament_api;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

//- Désactivé : charger le contexte demande une vraie base PostgreSQL (DATASOURCE_URL, JWT_SECRET...)
//- non disponible pendant `mvn verify`. À réactiver avec Testcontainers ou un profil de test.
@Disabled("Requires a running PostgreSQL database")
@SpringBootTest
class TournamentApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
