package com.auticare.service;

import com.auticare.entity.User;
import com.auticare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void init() {
        System.out.println("🔍 Vérification de la connexion à la base de données...");
        try {
            long count = userRepository.count();
            System.out.println("✅ Connexion réussie! Nombre d'utilisateurs: " + count);

            if (count > 0) {
                List<User> users = userRepository.findAll();
                System.out.println("📋 Liste des utilisateurs:");
                for (User user : users) {
                    System.out.println("   - ID: " + user.getId() +
                            ", Nom: " + user.getName() +
                            ", Email: " + user.getEmail() +
                            ", Rôle: " + user.getRole() +
                            ", Status: " + user.getStatus());
                }
            } else {
                System.out.println("⚠️ Aucun utilisateur trouvé dans la table 'users'");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur de connexion à la base de données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        System.out.println("📊 getAllUsers() - Nombre d'utilisateurs: " + users.size());
        return users;
    }

    public User saveUser(User user) {
        System.out.println("💾 Sauvegarde de l'utilisateur: " + user.getEmail());
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            System.out.println("🔍 Utilisateur trouvé par ID: " + id);
            return optionalUser.get();
        } else {
            System.out.println("🔍 Aucun utilisateur trouvé avec ID: " + id);
            return null;
        }
    }

    public void deleteUser(Long id) {
        System.out.println("🗑️ Suppression de l'utilisateur avec ID: " + id);
        userRepository.deleteById(id);
    }

    public User updateUser(User user) {
        if (user.getId() == null) {
            System.out.println("❌ Impossible de mettre à jour: ID null");
            return null;
        }

        System.out.println("🔄 Mise à jour de l'utilisateur ID: " + user.getId());
        return userRepository.findById(user.getId()).map(existingUser -> {
            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());
            existingUser.setRole(user.getRole());

            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                existingUser.setPassword(user.getPassword());
            }
            if (user.getPhoneNumber() != null) {
                existingUser.setPhoneNumber(user.getPhoneNumber());
            }
            if (user.getStatus() != null) {
                existingUser.setStatus(user.getStatus());
            }
            if (user.getPhotoUrl() != null) {
                existingUser.setPhotoUrl(user.getPhotoUrl());
            }

            return userRepository.save(existingUser);
        }).orElse(null);
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}