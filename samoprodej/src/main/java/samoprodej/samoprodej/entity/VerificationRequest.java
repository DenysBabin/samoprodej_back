package samoprodej.samoprodej.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "verification_request")
public class VerificationRequest {

    @Id
    UUID verificationRequest;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
}
