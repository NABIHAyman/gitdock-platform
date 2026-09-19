package edu.ehei.gitdock.gitdockproject.service;

import edu.ehei.gitdock.gitdockproject.client.AuthServiceClient;
import edu.ehei.gitdock.gitdockproject.dto.CreateProjectRequestDTO;
import edu.ehei.gitdock.gitdockproject.dto.ProjectDTO;
import edu.ehei.gitdock.gitdockproject.dto.SyncRequestMessageDTO;
import edu.ehei.gitdock.gitdockproject.repository.ProjectRepository;
import edu.ehei.gitdock.gitdockproject.repository.UserProjectRepository;
import edu.ehei.gitdock.gitdockproject.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
public class ProjectServiceIntegrationTest {

    @Autowired
    private ProjectServiceImpl projectService;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private UserProjectRepository userProjectRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private AuthServiceClient authServiceClient;

    @MockBean
    private JwtService jwtService;

    // Supprime le @MockBean HttpServletRequest s'il crée le crash
    // On va simuler la requête manuellement dans le setUp

    @BeforeEach
    void setUp() {
        // 1. Reset des mocks
        Mockito.reset(jwtService, projectRepository, rabbitTemplate, authServiceClient);

        // 2. Simulation du Contexte HTTP & JWT
        HttpServletRequest mockedRequest = Mockito.mock(HttpServletRequest.class);
        lenient().when(mockedRequest.getHeader("Authorization")).thenReturn("Bearer fake-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockedRequest));

        lenient().when(jwtService.extractUserId(anyString())).thenReturn(1L);
        lenient().when(jwtService.extractCompanyId(anyString())).thenReturn(10L);

        // 3. SIMULATION GLOBALE DU REPOSITORY (Pour éviter le savedProject null)
        lenient().when(projectRepository.save(any())).thenAnswer(invocation -> {
            edu.ehei.gitdock.gitdockproject.model.Project p = invocation.getArgument(0);
            try {
                java.lang.reflect.Field field;
                try {
                    field = p.getClass().getDeclaredField("id");
                } catch (NoSuchFieldException e) {
                    field = p.getClass().getSuperclass().getDeclaredField("id");
                }
                field.setAccessible(true);
                field.set(p, 100L); // On donne un ID fictif
            } catch (Exception e) {}
            return p;
        });
    }

    @Test
    void testCreateProject_Success() throws Exception {
        // ARRANGE
        CreateProjectRequestDTO requestDto = new CreateProjectRequestDTO();
        requestDto.setName("Mon Super Projet");
        requestDto.setUrl("https://github.com/test/repo");
        requestDto.setPlatform("GITHUB");
        requestDto.setVisibility("PUBLIC");


        // ACT
        ProjectDTO result = projectService.createProject(requestDto);

        // ASSERT
        assertNotNull(result);
        assertEquals(100L, result.getId());

        // Vérification RabbitMQ
        verify(rabbitTemplate, atLeastOnce()).convertAndSend(anyString(), anyString(), any(SyncRequestMessageDTO.class));
    }
    //Le Test de Résilience (Circuit Breaker)
    @Test
    void testCreateProject_CircuitBreaker_Fallback() {
        // 1. ARRANGE
        CreateProjectRequestDTO requestDto = new CreateProjectRequestDTO();
        requestDto.setName("Projet Résilient");
        requestDto.setVisibility("PUBLIC");
        requestDto.setUrl("https://github.com/test/resilience");
        requestDto.setPlatform("GITHUB");

        // On simule une PANNE sur la méthode getUserByEmail
        // On utilise anyString() pour le token et l'email
        when(authServiceClient.getUserByEmail(anyString(), anyString()))
                .thenThrow(new RuntimeException("Service Auth indisponible"));

        // 2. ACT
        // L'appel à createProject va déclencher en interne un appel à AuthServiceClient
        ProjectDTO result = projectService.createProject(requestDto);

        // 3. ASSERT
        assertNotNull(result, "Le service doit renvoyer un projet même si l'Auth est en panne (Fallback)");
        assertEquals("Projet Résilient", result.getName());

        System.out.println("✅ Succès : Le disjoncteur a protégé l'application !");
    }
    // Petite méthode utilitaire pour trouver l'ID par réflexion
    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return findField(clazz.getSuperclass(), fieldName);
        }
    }
}