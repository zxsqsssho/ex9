//src/main/java/com/library/repository/ReservationRepository.java
package com.library.repository;

import com.library.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserIdAndStatus(Long userId, Reservation.ReservationStatus status);
    List<Reservation> findByExpiryTimeBeforeAndStatus(LocalDateTime expiryTime, Reservation.ReservationStatus status);
    List<Reservation> findByStatus(Reservation.ReservationStatus status);
}