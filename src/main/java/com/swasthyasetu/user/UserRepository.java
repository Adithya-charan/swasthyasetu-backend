package com.swasthyasetu.user;

import com.swasthyasetu.user.entity.AccountStatus;
import com.swasthyasetu.user.entity.Role;
import com.swasthyasetu.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Page<User> findByRole(Role role, Pageable pageable);
    Page<User> findByAccountStatus(AccountStatus status, Pageable pageable);
    Page<User> findByRoleAndAccountStatus(Role role, AccountStatus status, Pageable pageable);
}
