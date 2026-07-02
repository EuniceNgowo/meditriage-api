package com.example.triage_api.repository;

import com.example.triage_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    /** Bypass entity save to avoid Hibernate orphan-removal on sessions collection */
    @Modifying
    @Query(value = "UPDATE users SET is_active = false WHERE email = :email", nativeQuery = true)
    int deactivateByEmail(@Param("email") String email);

    @Modifying
    @Query(value = "UPDATE users SET reset_token = :token, reset_token_expires_at = :expires WHERE email = :email", nativeQuery = true)
    int setResetToken(@Param("email") String email, @Param("token") String token, @Param("expires") java.time.Instant expires);

    @Modifying
    @Query(value = "UPDATE users SET password_hash = :hash, reset_token = NULL, reset_token_expires_at = NULL WHERE email = :email", nativeQuery = true)
    int updatePassword(@Param("email") String email, @Param("hash") String hash);
}
