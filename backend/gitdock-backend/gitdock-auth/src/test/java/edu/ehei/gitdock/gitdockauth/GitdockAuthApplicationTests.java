package edu.ehei.gitdock.gitdockauth;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
@Disabled("Test désactivé car AuthenticationServiceIntegrationTest couvre déjà le chargement du contexte")
@SpringBootTest
@ActiveProfiles("test")
class GitdockAuthApplicationTests {

    @Test
    void contextLoads() {
    }

}