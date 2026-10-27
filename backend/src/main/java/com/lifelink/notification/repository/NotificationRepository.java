package com.lifelink.notification.repository;

import com.lifelink.notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
        SELECT n FROM Notification n
        WHERE (n.recipientUser.id = :userId)
           OR (n.facility.id = :facilityId)
           OR (n.targetRole = :role)
        ORDER BY n.createdAt DESC
    """)
    Page<Notification> findUserNotifications(
            @Param("userId") Long userId,
            @Param("facilityId") Long facilityId,
            @Param("role") String role,
            Pageable pageable
    );

    @Query("""
        SELECT COUNT(n) FROM Notification n
        WHERE n.read = false
          AND ((n.recipientUser.id = :userId)
           OR (n.facility.id = :facilityId)
           OR (n.targetRole = :role))
    """)
    long countUnreadNotifications(
            @Param("userId") Long userId,
            @Param("facilityId") Long facilityId,
            @Param("role") String role
    );
}
