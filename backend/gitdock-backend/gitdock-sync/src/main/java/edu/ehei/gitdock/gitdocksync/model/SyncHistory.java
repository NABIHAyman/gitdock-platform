package edu.ehei.gitdock.gitdocksync.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;



@Entity
@Table(name = "sync_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SyncHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;
    private String status; // SUCCESS, FAILED
    private LocalDateTime syncDate;
    private String details;
}