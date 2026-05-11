package edu.ehei.gitdock.gitdockauth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ehei.gitdock.gitdockauth.dto.RegisterRequestDTO;
import edu.ehei.gitdock.gitdockauth.dto.AuthenticationRequestDTO;
import edu.ehei.gitdock.gitdockauth.repository.UserAccountRepository;
import edu.ehei.gitdock.gitdockauth.repository.RoleRepository;
import edu.ehei.gitdock.gitdockauth.repository.ActivationTokenRepository;
import edu.ehei.gitdock.gitdockauth.repository.CompanyRepository;
import edu.ehei.gitdock.gitdockauth.model.Role;
import edu.ehei.gitdock.gitdockauth.model.UserAccount;
import edu.ehei.gitdock.gitdockauth.enums.RoleName;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import java.util.Optional;
import org.springframework.boot.test.mock.mockito.SpyBean;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class AuthenticationServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @SpyBean//pour pouvoir "saboter" RabbitMQ
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setUp() {
        // NETTOYAGE COMPLET (Ordre respectant les FK)
        activationTokenRepository.deleteAll();
        userAccountRepository.deleteAll();
        companyRepository.deleteAll();
        roleRepository.deleteAll();

        // INITIALISATION
        Role ownerRole = Role.builder()
                .name(RoleName.ROLE_WORKSPACE_OWNER)
                .description("Owner of the workspace")
                .build();
        roleRepository.save(ownerRole);
    }

    @Test
    void register_Integration_Test() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setFirstName("Amal");
        request.setLastName("Test");
        request.setEmail("amal.test@gitdock.com");
        request.setUsername("amaltest");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        assertThat(userAccountRepository.findByEmailAndIsDeletedFalse("amal.test@gitdock.com")).isPresent();
    }

    @Test
    void register_ShouldFail_WhenEmailAlreadyExists() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setFirstName("Duplicate");
        request.setLastName("User");
        request.setEmail("duplicate@gitdock.com");
        request.setUsername("duplicateuser");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        try {
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
            org.junit.jupiter.api.Assertions.fail("Exception non jetée");
        } catch (Exception e) {
            assertThat(e.getCause().getMessage()).contains("Cet email est déjà utilisé");
        }
    }

    @Test
    void authenticate_ShouldReturnToken_WhenCredentialsAreValid() throws Exception {
        // 1. Inscription
        RegisterRequestDTO regRequest = new RegisterRequestDTO();
        regRequest.setFirstName("Amal");
        regRequest.setLastName("Login");
        regRequest.setEmail("login.test@gitdock.com");
        regRequest.setUsername("loginuser");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isAccepted());

        // 2. ACTIVATION + PASSWORD ENCODING
        // On récupère l'user créé et on lui met un password encodé par l'encoder du projet
        UserAccount user = userAccountRepository.findByEmailAndIsDeletedFalse("login.test@gitdock.com")
                .orElseThrow();

        user.setEnabled(true);
        // On encode "password" avec l'outil officiel de ton projet
        user.setPassword(passwordEncoder.encode("password"));
        userAccountRepository.save(user);

        // 3. Login
        AuthenticationRequestDTO loginRequest = new AuthenticationRequestDTO();
        loginRequest.setEmail("login.test@gitdock.com");
        loginRequest.setPassword("password"); // Mot de passe en clair pour le login

        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    // On vérifie le token sans se soucier de la casse (Token ou token)
                    assertThat(json.toLowerCase()).contains("token");
                });
    }
    @Test
    void testSagaRollback_WhenRabbitFails() throws Exception {
        // 1. On force RabbitMQ à jeter une erreur
        doThrow(new RuntimeException("RabbitMQ Down"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        // 2. Préparation des données
        RegisterRequestDTO reg = new RegisterRequestDTO();
        reg.setEmail("test.saga@gitdock.com");
        reg.setFirstName("Saga");
        reg.setLastName("Test");

        // 3. On exécute l'appel et on IGNORE l'exception pour passer à la suite
        try {
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reg)));
        } catch (Exception e) {
            // On attrape l'erreur ici pour que le test ne s'arrête pas en erreur
            System.out.println("Exception capturée comme prévu : " + e.getMessage());
        }

        // 4. VÉRIFICATION DU ROLLBACK (Le moment de vérité)
        // On laisse un petit temps si nécessaire ou on vérifie direct
        Optional<UserAccount> user = userAccountRepository.findByEmailAndIsDeletedFalse("test.saga@gitdock.com");

        // Si l'utilisateur est vide, c'est que @Transactional a fonctionné !
        assertThat(user).isEmpty();
    }
}