package sn.samabank.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.samabank.notification.entity.Notification;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {}
