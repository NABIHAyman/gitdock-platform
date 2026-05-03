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
    @GetMapping("/summaries")
    public ResponseEntity<List<UserSummaryDTO>> getUsersSummaries(
            @RequestParam(required = false) List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(userService.getAllUsersSummaries());
        }

        return ResponseEntity.ok(userService.getUsersSummaries(ids));
    }

    // =========================
    // 📧 GET USER BY EMAIL
    // =========================
    @GetMapping("/by-email")
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
    }

    // =========================
    // ✏️ UPDATE USER
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<UserSummaryDTO> updateUser(
            @PathVariable Long id,
            @RequestBody CreateUserRequestDTO request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    // =========================
    // 🟡 SOFT DELETE
    // =========================
    @PutMapping("/{id}/soft-delete")
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

    // =========================
    // 📩 BATCH BY EMAILS (SYMFONY OPTIMIZATION)
    // =========================
    @PostMapping("/internal/by-emails")
    public ResponseEntity<List<UserSummaryDTO>> getUsersByEmails(
            @RequestBody List<String> emails) {
        return ResponseEntity.ok(userService.getUsersByEmails(emails));
    }
}