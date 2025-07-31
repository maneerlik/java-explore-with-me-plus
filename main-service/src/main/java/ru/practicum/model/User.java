package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true, nullable = false, length = 254)
    private String email;

    @Column(nullable = false, length = 250)
    private String name;
}
