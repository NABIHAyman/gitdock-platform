package edu.ehei.gitdock.gitdockauth.controller;

import edu.ehei.gitdock.gitdockauth.dto.UserSummaryDTO;
import edu.ehei.gitdock.gitdockauth.model.Company;
import edu.ehei.gitdock.gitdockauth.model.UserAccount;
import edu.ehei.gitdock.gitdockauth.repository.CompanyRepository;
import edu.ehei.gitdock.gitdockauth.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth/super-admin")
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')") // 🛡️ Sécurité stricte !
@RequiredArgsConstructor
public class SuperAdminController {

    private final UserAccountRepository userRepository;
    private final CompanyRepository companyRepository;

    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getBusinessKPIs() {
        Map<String, Object> kpis = new HashMap<>();

        // 1. Chiffres globaux
        kpis.put("totalUsers", userRepository.countByIsDeletedFalse());
        kpis.put("totalCompanies", companyRepository.countByIsDeletedFalse());

        // 2. Répartition des abonnements
        List<Company> allCompanies = companyRepository.findAll();
        long freeCount = allCompanies.stream().filter(c -> "FREE".equals(c.getSubscriptionPlan().name())).count();
        long proCount = allCompanies.stream().filter(c -> "PRO".equals(c.getSubscriptionPlan().name())).count();
        long entCount = allCompanies.stream().filter(c -> "ENTERPRISE".equals(c.getSubscriptionPlan().name())).count();

        kpis.put("subscriptions", Map.of(
                "FREE", freeCount,
                "PRO", proCount,
                "ENTERPRISE", entCount
        ));

        return ResponseEntity.ok(kpis);
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsersForAdmin() {
        // En production, utilise une Pageable ici !
        List<Map<String, Object>> users = userRepository.findAllByIsDeletedFalse().stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getFirstName() + " " + u.getLastName());
            map.put("email", u.getEmail());
            map.put("company", u.getCompany() != null ? u.getCompany().getName() : "Aucune");
            map.put("status", u.isEnabled() ? "active" : "suspended");
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }
}