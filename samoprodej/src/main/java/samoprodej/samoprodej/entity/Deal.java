package samoprodej.samoprodej.entity;


import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "deal")
public class Deal {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID dealId;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//
public Deal(){}
}
