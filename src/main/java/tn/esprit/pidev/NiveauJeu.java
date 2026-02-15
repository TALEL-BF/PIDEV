package tn.esprit.pidev;

import javafx.beans.property.*;

public class NiveauJeu {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty libelle = new SimpleStringProperty();
    private final DoubleProperty minMoyenne = new SimpleDoubleProperty();
    private final DoubleProperty maxMoyenne = new SimpleDoubleProperty();
    private final StringProperty description = new SimpleStringProperty();

    public NiveauJeu() {}

    public NiveauJeu(int id, String libelle, double minMoyenne, double maxMoyenne, String description) {
        setId(id);
        setLibelle(libelle);
        setMinMoyenne(minMoyenne);
        setMaxMoyenne(maxMoyenne);
        setDescription(description);
    }

    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }
    public IntegerProperty idProperty() { return id; }

    public String getLibelle() { return libelle.get(); }
    public void setLibelle(String v) { libelle.set(v); }
    public StringProperty libelleProperty() { return libelle; }

    public double getMinMoyenne() { return minMoyenne.get(); }
    public void setMinMoyenne(double v) { minMoyenne.set(v); }
    public DoubleProperty minMoyenneProperty() { return minMoyenne; }

    public double getMaxMoyenne() { return maxMoyenne.get(); }
    public void setMaxMoyenne(double v) { maxMoyenne.set(v); }
    public DoubleProperty maxMoyenneProperty() { return maxMoyenne; }

    public String getDescription() { return description.get(); }
    public void setDescription(String v) { description.set(v); }
    public StringProperty descriptionProperty() { return description; }
}
