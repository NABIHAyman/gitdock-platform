package edu.ehei.gitdock.gitdockproject.enums;

public enum ProjectStatus {
    PENDING,    // En cours de création par la Saga
    ACTIVE,     // Création réussie, équipe assignée
    FAILED      // La Saga a échoué (sera supprimé ou archivé)
}
