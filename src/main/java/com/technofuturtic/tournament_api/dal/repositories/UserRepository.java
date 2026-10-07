package com.technofuturtic.tournament_api.dal.repositories;

import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    java.util.List<UserEntity> findTop20ByDeletedFalseAndUsernameContainingIgnoreCaseOrderByUsernameAsc(String username);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserEntity u where u.id = :id")
    Optional<UserEntity> findByIdForUpdate(Integer id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameAndIdNot(String username, Integer id);

    @Query("select u from UserEntity u join fetch u.role where u.id = :id")
    Optional<UserEntity> findWithRoleById(Integer id);

    @Query("select u from UserEntity u join fetch u.role where u.username = :username")
    Optional<UserEntity> findByUsername(String username);
}