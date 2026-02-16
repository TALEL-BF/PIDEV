package com.auticare.repository;

import com.auticare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Trouver un utilisateur par email (pour login)
    Optional<User> findByEmail(String email);

    // Trouver tous les utilisateurs par rôle
    List<User> findByRole(String role);

    // Trouver tous les utilisateurs par statut
    List<User> findByStatus(String status);

    // Compter les utilisateurs par rôle
    long countByRole(String role);

    // Compter les utilisateurs par statut
    long countByStatus(String status);

    // Vérifier si un email existe déjà
    boolean existsByEmail(String email);

    // Recherche par nom ou email (insensible à la casse)
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchUsers(@Param("search") String search);

    // Trouver les utilisateurs récents (inscrits après une certaine date)
    @Query("SELECT u FROM User u WHERE u.registrationDate >= :date ORDER BY u.registrationDate DESC")
    List<User> findRecentUsers(@Param("date") java.time.LocalDate date);

    // Trouver les administrateurs
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN'")
    List<User> findAdmins();

    // Trouver les utilisateurs actifs
    @Query("SELECT u FROM User u WHERE u.status = 'Actif'")
    List<User> findActiveUsers();

    // Trouver les utilisateurs par rôle et statut
    List<User> findByRoleAndStatus(String role, String status);

    // Supprimer un utilisateur par email
    void deleteByEmail(String email);

    // Obtenir le nombre total d'utilisateurs
    @Query("SELECT COUNT(u) FROM User u")
    long getTotalUserCount();

    // Obtenir les statistiques par rôle
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> getUserCountByRole();

    // Obtenir les statistiques par statut
    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
    List<Object[]> getUserCountByStatus();

    // Trouver les utilisateurs sans numéro de téléphone
    List<User> findByPhoneNumberIsNull();

    // Trouver les utilisateurs avec photo
    List<User> findByPhotoUrlIsNotNull();

    // Trouver les 10 derniers utilisateurs inscrits
    List<User> findTop10ByOrderByRegistrationDateDesc();

    // Trouver les utilisateurs par nom (exact)
    List<User> findByName(String name);

    // Trouver les utilisateurs par nom contenant (insensible à la casse)
    List<User> findByNameContainingIgnoreCase(String name);

    // Trouver les utilisateurs par email contenant
    List<User> findByEmailContainingIgnoreCase(String email);
}