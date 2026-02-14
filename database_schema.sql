-- AutiCare Database Schema
-- This schema defines the tables for managing Seances, RDV, and Emploi du Temps

-- Drop existing tables if they exist (be careful in production!)
DROP TABLE IF EXISTS emploi_du_temps;
DROP TABLE IF EXISTS seance;
DROP TABLE IF EXISTS rdv;

-- Table: seance
CREATE TABLE seance (
    id_seance INT AUTO_INCREMENT PRIMARY KEY,
    titre_seance VARCHAR(100) NOT NULL,
    description TEXT,
    date_seance DATETIME NOT NULL,
    jours_semaine VARCHAR(20) NOT NULL,
    duree INT NOT NULL COMMENT 'Duration in minutes',
    statut_seance ENUM('planifiee', 'confirme', 'annule', 'reporte', 'termine') DEFAULT 'planifiee',
    id_autiste INT NOT NULL,
    id_professeur INT NOT NULL,
    id_cours INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: rdv (rendez-vous)
CREATE TABLE rdv (
    id_rdv INT AUTO_INCREMENT PRIMARY KEY,
    type_consultation ENUM('premiere_consultation', 'suivi', 'urgence', 'familiale', 'bilan') NOT NULL,
    date_heure_rdv DATETIME NOT NULL,
    statut_rdv ENUM('planifiee', 'confirme', 'annule', 'reporte', 'termine') DEFAULT 'planifiee',
    duree_rdv_minutes INT NOT NULL,
    id_psychologue INT NOT NULL,
    id_autiste INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: emploi_du_temps
CREATE TABLE emploi_du_temps (
    id_emploi INT AUTO_INCREMENT PRIMARY KEY,
    annee_scolaire VARCHAR(20) NOT NULL COMMENT 'e.g., 2024-2025',
    jour_semaine ENUM('lundi', 'mardi', 'mercredi', 'jeudi', 'vendredi', 'samedi', 'dimanche') NOT NULL,
    tranche_horaire ENUM('matin', 'apres_midi', 'soir', 'journee') NOT NULL,
    id_rdv INT NULL,
    id_seance INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_rdv) REFERENCES rdv(id_rdv) ON DELETE CASCADE,
    FOREIGN KEY (id_seance) REFERENCES seance(id_seance) ON DELETE CASCADE,
    CHECK (id_rdv IS NOT NULL OR id_seance IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for better query performance
CREATE INDEX idx_seance_statut ON seance(statut_seance);
CREATE INDEX idx_seance_date ON seance(date_seance);
CREATE INDEX idx_seance_jour ON seance(jours_semaine);
CREATE INDEX idx_seance_autiste ON seance(id_autiste);
CREATE INDEX idx_seance_professeur ON seance(id_professeur);

CREATE INDEX idx_rdv_statut ON rdv(statut_rdv);
CREATE INDEX idx_rdv_date ON rdv(date_heure_rdv);
CREATE INDEX idx_rdv_psychologue ON rdv(id_psychologue);
CREATE INDEX idx_rdv_autiste ON rdv(id_autiste);
CREATE INDEX idx_rdv_type ON rdv(type_consultation);

CREATE INDEX idx_emploi_jour ON emploi_du_temps(jour_semaine);
CREATE INDEX idx_emploi_tranche ON emploi_du_temps(tranche_horaire);
CREATE INDEX idx_emploi_annee ON emploi_du_temps(annee_scolaire);

-- Insert sample data for testing

-- Sample Seances
INSERT INTO seance (titre_seance, description, date_seance, jours_semaine, duree, statut_seance, id_autiste, id_professeur, id_cours) VALUES
('Cours de Mathématiques', 'Introduction aux nombres', '2025-03-15 09:00:00', 'lundi', 60, 'planifiee', 1, 1, 1),
('Cours de Français', 'Lecture et écriture', '2025-03-16 10:00:00', 'mardi', 45, 'confirme', 1, 2, 2),
('Atelier Artistique', 'Peinture et créativité', '2025-03-17 14:00:00', 'mercredi', 90, 'confirme', 2, 3, 3),
('Cours de Sciences', 'Découverte de la nature', '2025-03-18 11:00:00', 'jeudi', 60, 'planifiee', 3, 1, 4),
('Éducation Physique', 'Activités motrices', '2025-03-19 15:00:00', 'vendredi', 75, 'termine', 2, 4, 5);

-- Sample RDV
INSERT INTO rdv (type_consultation, date_heure_rdv, statut_rdv, duree_rdv_minutes, id_psychologue, id_autiste) VALUES
('premiere_consultation', '2025-03-20 09:00:00', 'confirme', 60, 1, 1),
('suivi', '2025-03-21 10:30:00', 'planifiee', 45, 1, 2),
('urgence', '2025-03-22 14:00:00', 'confirme', 30, 2, 3),
('familiale', '2025-03-23 16:00:00', 'planifiee', 90, 2, 1),
('bilan', '2025-03-24 11:00:00', 'termine', 120, 1, 2);

-- Sample Emploi du Temps
INSERT INTO emploi_du_temps (annee_scolaire, jour_semaine, tranche_horaire, id_rdv, id_seance) VALUES
('2024-2025', 'lundi', 'matin', NULL, 1),
('2024-2025', 'mardi', 'matin', NULL, 2),
('2024-2025', 'mercredi', 'apres_midi', NULL, 3),
('2024-2025', 'jeudi', 'matin', NULL, 4),
('2024-2025', 'vendredi', 'apres_midi', NULL, 5),
('2024-2025', 'lundi', 'matin', 1, NULL),
('2024-2025', 'mardi', 'matin', 2, NULL),
('2024-2025', 'mercredi', 'apres_midi', 3, NULL);

-- Display counts
SELECT 'Seances' AS Table_Name, COUNT(*) AS Count FROM seance
UNION ALL
SELECT 'RDV' AS Table_Name, COUNT(*) AS Count FROM rdv
UNION ALL
SELECT 'Emploi du Temps' AS Table_Name, COUNT(*) AS Count FROM emploi_du_temps;

