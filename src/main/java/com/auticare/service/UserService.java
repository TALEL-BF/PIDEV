package com.auticare.service;

import com.auticare.entity.User;
import com.auticare.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                            ", Statut: " + user.getStatus());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur de connexion à la base de données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        System.out.println("📊 getAllUsers() - Récupération de tous les utilisateurs");
        try {
            List<User> users = userRepository.findAll();
            System.out.println("✅ " + users.size() + " utilisateurs trouvés");
            return users;
        } catch (Exception e) {
            System.err.println("❌ Erreur getAllUsers: " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public User saveUser(User user) {
        System.out.println("💾 saveUser() - Sauvegarde de l'utilisateur: " + user.getEmail());

        try {
            // Validation
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("L'email est requis");
            }

            // Vérifier si c'est une mise à jour
            if (user.getId() != null) {
                System.out.println("🔄 Mise à jour de l'utilisateur ID: " + user.getId());

                // Récupérer l'utilisateur existant
                Optional<User> existingUserOpt = userRepository.findById(user.getId());
                if (existingUserOpt.isPresent()) {
                    User existingUser = existingUserOpt.get();

                    // Vérifier si l'email a changé
                    if (!existingUser.getEmail().equals(user.getEmail())) {
                        System.out.println("⚠️ L'email change de " + existingUser.getEmail() + " à " + user.getEmail());

                        // Vérifier si le nouvel email existe déjà (chez un AUTRE utilisateur)
                        Optional<User> userWithSameEmail = userRepository.findByEmail(user.getEmail());
                        if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(user.getId())) {
                            throw new RuntimeException("L'email " + user.getEmail() + " est déjà utilisé par un autre compte");
                        }
                    } else {
                        System.out.println("✅ L'email n'a pas changé");
                    }

                    // Garder l'ancien mot de passe si aucun nouveau n'est fourni
                    if (user.getPassword() == null || user.getPassword().isEmpty()) {
                        user.setPassword(existingUser.getPassword());
                        System.out.println("🔑 Mot de passe conservé");
                    }

                    // Garder la date d'inscription originale
                    if (user.getRegistrationDate() == null) {
                        user.setRegistrationDate(existingUser.getRegistrationDate());
                    }
                }
            } else {
                System.out.println("➕ Création d'un nouvel utilisateur");
                // Vérifier si l'email existe déjà pour un nouvel utilisateur
                if (userRepository.existsByEmail(user.getEmail())) {
                    throw new RuntimeException("L'email " + user.getEmail() + " est déjà utilisé");
                }
            }

            User savedUser = userRepository.save(user);
            System.out.println("✅ Utilisateur sauvegardé avec ID: " + savedUser.getId());
            return savedUser;

        } catch (Exception e) {
            System.err.println("❌ Erreur saveUser: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        System.out.println("🔍 getUserById() - Recherche de l'utilisateur ID: " + id);
        try {
            Optional<User> optionalUser = userRepository.findById(id);
            if (optionalUser.isPresent()) {
                System.out.println("✅ Utilisateur trouvé: " + optionalUser.get().getEmail());
                return optionalUser.get();
            } else {
                System.out.println("⚠️ Aucun utilisateur trouvé avec ID: " + id);
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur getUserById: " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void deleteUser(Long id) {
        System.out.println("🗑️ deleteUser() - Suppression de l'utilisateur ID: " + id);
        try {
            userRepository.deleteById(id);
            System.out.println("✅ Utilisateur supprimé avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur deleteUser: " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public User updateUser(User user) {
        System.out.println("🔄 updateUser() - Mise à jour de l'utilisateur ID: " + user.getId());

        if (user.getId() == null) {
            throw new IllegalArgumentException("L'ID est requis pour la mise à jour");
        }

        try {
            return userRepository.findById(user.getId())
                    .map(existingUser -> {
                        // Vérifier si l'email a changé
                        if (!existingUser.getEmail().equals(user.getEmail())) {
                            // Vérifier si le nouvel email est déjà pris par un autre utilisateur
                            Optional<User> userWithSameEmail = userRepository.findByEmail(user.getEmail());
                            if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(user.getId())) {
                                throw new RuntimeException("L'email " + user.getEmail() + " est déjà utilisé");
                            }
                        }

                        // Mettre à jour les champs
                        existingUser.setName(user.getName());
                        existingUser.setEmail(user.getEmail());
                        existingUser.setRole(user.getRole());
                        existingUser.setStatus(user.getStatus());
                        existingUser.setPhoneNumber(user.getPhoneNumber());

                        // Mettre à jour le mot de passe seulement s'il est fourni
                        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                            existingUser.setPassword(user.getPassword());
                        }

                        // Mettre à jour la photo seulement si fournie
                        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                            existingUser.setPhotoUrl(user.getPhotoUrl());
                        }

                        User updatedUser = userRepository.save(existingUser);
                        System.out.println("✅ Utilisateur mis à jour avec succès");
                        return updatedUser;
                    })
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec ID: " + user.getId()));

        } catch (Exception e) {
            System.err.println("❌ Erreur updateUser: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        System.out.println("🔍 existsByEmail() - Vérification de l'email: " + email);
        try {
            boolean exists = userRepository.findByEmail(email).isPresent();
            System.out.println("✅ Email " + email + (exists ? " existe" : " n'existe pas"));
            return exists;
        } catch (Exception e) {
            System.err.println("❌ Erreur existsByEmail: " + e.getMessage());
            return false;
        }
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        System.out.println("🔍 findByEmail() - Recherche de l'email: " + email);
        try {
            return userRepository.findByEmail(email);
        } catch (Exception e) {
            System.err.println("❌ Erreur findByEmail: " + e.getMessage());
            throw e;
        }
    }
}