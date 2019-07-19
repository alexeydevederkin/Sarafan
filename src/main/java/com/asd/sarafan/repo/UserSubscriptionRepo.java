package com.asd.sarafan.repo;

import com.asd.sarafan.domain.User;
import com.asd.sarafan.domain.UserSubscription;
import com.asd.sarafan.domain.UserSubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSubscriptionRepo extends JpaRepository<UserSubscription, UserSubscriptionId> {
    List<UserSubscription> findBySubscriber(User user);
}
