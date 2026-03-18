package com.example.services.repository;

import com.example.services.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository  extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    @Query("SELECT u " +
            "FROM User u LEFT JOIN u.wallet " +
            "WHERE LOWER(u.email) = LOWER(:email) ")
    Optional<User> findByEmailIgnoreCaseWithWallet(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.wallet WHERE u.email = :email")
    Optional<User> findByEmailWithWallet(String email);

    Optional<User> findByEmailIgnoreCase(String email);


}
