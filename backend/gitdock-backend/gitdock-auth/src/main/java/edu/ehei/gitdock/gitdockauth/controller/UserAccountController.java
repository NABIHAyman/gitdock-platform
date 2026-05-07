package edu.ehei.gitdock.gitdockauth.controller;

import edu.ehei.gitdock.gitdockauth.dto.CreateUserRequestDTO;
import edu.ehei.gitdock.gitdockauth.dto.InviteCollaboratorRequestDTO;
import edu.ehei.gitdock.gitdockauth.dto.UserSummaryDTO;
import edu.ehei.gitdock.gitdockauth.service.interfaces.IUserAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/users")
@Slf4j
@RequiredArgsConstructor
public class UserAccountController {

    private final IUserAccountService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserSummaryDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/summaries")
    public ResponseEntity<List<UserSummaryDTO>> getUsersSummaries(
            @RequestParam(value = "ids", required = false) List<Long> ids) {

        log.info("MOUCHARD SPRING - Requête reçue sur /summaries ! IDs demandés : {}", ids);

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(userService.getAllUsersSummaries());
        }

        return ResponseEntity.ok(userService.getUsersSummaries(ids));
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserSummaryDTO> getUserByEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    /**
     * Invitation projet : crée ou rattache l’utilisateur et notifie via RabbitMQ (TYPE_PROJECT_INVITATION).
     */
    @PostMapping("/internal/invite")
    public ResponseEntity<UserSummaryDTO> inviteUser(@RequestBody InviteCollaboratorRequestDTO request) {
        UserSummaryDTO createdUser = userService.inviteUserFromProject(request);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserSummaryDTO> updateUser(
            @PathVariable Long id,
            @RequestBody CreateUserRequestDTO request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PutMapping("/{id}/soft-delete")
    public ResponseEntity<Void> softDeleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Void> restoreUser(@PathVariable Long id) {
        userService.restoreUser(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/internal/by-emails")
    public ResponseEntity<List<UserSummaryDTO>> getUsersByEmailsInternal(@RequestBody List<String> emails) {
        return ResponseEntity.ok(userService.getUsersByEmails(emails));
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Void> checkUserExists(@PathVariable Long id) {
        log.info("Vérification d'existence pour l'utilisateur ID: {}", id);

        boolean exists = userService.existsById(id);

        if (exists) {
            return ResponseEntity.ok().build();
        } else {
            log.warn("Tentative de validation pour un utilisateur inconnu ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
