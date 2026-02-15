package Entites;

import java.time.LocalDateTime;

public class StatsSeanceDelta {
    public String nomEnfant;
    public LocalDateTime dateActuelle;

    public int humeurActuelle, stressActuel, attentionActuelle;
    public Integer prevHumeur, prevStress, prevAttention;

    public int deltaH() { return (prevHumeur == null) ? 0 : humeurActuelle - prevHumeur; }
    public int deltaA() { return (prevAttention == null) ? 0 : attentionActuelle - prevAttention; }

    // Stress: amélioration = baisse => positif si mieux
    public int progressStress() { return (prevStress == null) ? 0 : prevStress - stressActuel; }
}
