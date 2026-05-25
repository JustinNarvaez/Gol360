package com.pjg360.PJG360.repositories;

import com.pjg360.PJG360.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUserName(String userName);
    Boolean existsByEmail(String email);
    Boolean existsByUserName(String userName);
}
