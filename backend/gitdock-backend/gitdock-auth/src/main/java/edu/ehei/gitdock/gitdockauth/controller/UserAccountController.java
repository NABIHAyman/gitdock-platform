package edu.ehei.gitdock.gitdockauth.controller;

import edu.ehei.gitdock.gitdockauth.dto.CreateUserRequestDTO;
import edu.ehei.gitdock.gitdockauth.dto.InviteCollaboratorRequestDTO;
import edu.ehei.gitdock.gitdockauth.dto.UserSummaryDTO;
import edu.ehei.gitdock.gitdockauth.service.interfaces.IUserAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;


import java.util.List;

@RestController
@RequestMapping("/api/auth/users")
@Slf4j
@RequiredArgsConstructor
@Slf4j
public class UserAccountController {

    private final IUserAccountService userService;

    // =========================
    // 👤 GET USER BY ID (SYMFONY USE)
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<UserSummaryDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // =========================
    // 👥 BATCH USERS (SYMFONY / TASK SERVICE)
    // =========================
    // Endpoint utilisé en interne par gitdock-project via OpenFeign
    @GetMapping("/summaries")
    public ResponseEntity<List<UserSummaryDTO>> getUsersSummaries(
            @RequestParam(value = "ids", required = false) List<Long> ids) {
            @RequestParam(required = false) List<Long> ids) {

        log.info("MOUCHARD SPRING - Requête reçue sur /summaries ! IDs demandés : {}", ids);

        // Si aucun ID n'est passé par le frontend/Symfony, on renvoie tout l'annuaire
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(userService.getAllUsersSummaries());
        }

        // Sinon, on renvoie juste les IDs demandés
        return ResponseEntity.ok(userService.getUsersSummaries(ids));
    }

    // =========================
    // 📧 GET USER BY EMAIL
    // =========================
    @GetMapping("/by-email")
    public ResponseEntity<UserSummaryDTO> getUserByEmail(@RequestParam("email") String email) {
    public ResponseEntity<UserSummaryDTO> getUserByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    // =========================
    // 👥 INVITE USER (BUSINESS LOGIC)
    // =========================
    @PostMapping("/internal/invite")
    public ResponseEntity<UserSummaryDTO> inviteUser(
            @RequestBody InviteCollaboratorRequestDTO request) {
        return ResponseEntity.ok(userService.inviteUserFromProject(request));
    public ResponseEntity<UserSummaryDTO> inviteUser(@RequestBody InviteCollaboratorRequestDTO request) {
        // Cette méthode va pré-créer le compte et envoyer l'e-mail !
        UserSummaryDTO createdUser = userService.inviteUserFromProject(request);
        return ResponseEntity.ok(createdUser);
    }

    // =========================
    // ✏️ UPDATE USER
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<UserSummaryDTO> updateUser(@PathVariable Long id, @RequestBody CreateUserRequestDTO request) {
    public ResponseEntity<UserSummaryDTO> updateUser(
            @PathVariable Long id,
            @RequestBody CreateUserRequestDTO request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    // =========================
    // 🟡 SOFT DELETE
    // =========================
    @PutMapping("/{id}/soft-delete")
    @PutMapping("/soft-delete/{id}")
    public ResponseEntity<Void> softDeleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);
        return ResponseEntity.ok().build();
    }

    // =========================
    // 🔄 RESTORE USER
    // =========================
    @PostMapping("/{id}/restore")
    public ResponseEntity<Void> restoreUser(@PathVariable Long id) {
        userService.restoreUser(id);
        return ResponseEntity.ok().build();
    }

    // =========================
    // ❌ DELETE USER
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    // Endpoint INTERNE pour le Batching (Doit être autorisé dans SecurityConfig !)
    // =========================
    // 📩 BATCH BY EMAILS (SYMFONY OPTIMIZATION)
    // =========================
    @PostMapping("/internal/by-emails")
    public ResponseEntity<List<UserSummaryDTO>> getUsersByEmailsInternal(@RequestBody List<String> emails) {
    public ResponseEntity<List<UserSummaryDTO>> getUsersByEmails(
            @RequestBody List<String> emails) {
        return ResponseEntity.ok(userService.getUsersByEmails(emails));
    }


    // Endpoint utilisé par gitdock-gamification pour valider l'existence d'un user
    @GetMapping("/{id}/exists")
    public ResponseEntity<Void> checkUserExists(@PathVariable Long id) {
        log.info("Vérification d'existence pour l'utilisateur ID: {}", id);

        boolean exists = userService.existsById(id); // Tu devras peut-être ajouter cette méthode dans ton Service

        if (exists) {
            return ResponseEntity.ok().build();
        } else {
            log.warn("Tentative de validation pour un utilisateur inconnu ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}