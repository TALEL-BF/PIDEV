package tn.esprit.pidev;

import javafx.beans.property.*;

public class SuiviTotal {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty enfantId = new SimpleIntegerProperty();
    private final DoubleProperty noteEvaluation = new SimpleDoubleProperty();
    private final DoubleProperty noteConsultation = new SimpleDoubleProperty();

    // IMPORTANT: doit s'appeler "moyenne" pour TableView(PropertyValueFactory("moyenne"))
    private final DoubleProperty moyenne = new SimpleDoubleProperty();

    private final ObjectProperty<Integer> niveauId = new SimpleObjectProperty<>();
    private final StringProperty niveauLibelle = new SimpleStringProperty();

    public SuiviTotal() {}

    public SuiviTotal(int id, int enfantId, double noteEvaluation, double noteConsultation,
                      double moyenne, Integer niveauId, String niveauLibelle) {
        setId(id);
        setEnfantId(enfantId);
        setNoteEvaluation(noteEvaluation);
        setNoteConsultation(noteConsultation);
        setMoyenne(moyenne);
        setNiveauId(niveauId);
        setNiveauLibelle(niveauLibelle);
    }

    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }
    public IntegerProperty idProperty() { return id; }

    public int getEnfantId() { return enfantId.get(); }
    public void setEnfantId(int v) { enfantId.set(v); }
    public IntegerProperty enfantIdProperty() { return enfantId; }

    public double getNoteEvaluation() { return noteEvaluation.get(); }
    public void setNoteEvaluation(double v) { noteEvaluation.set(v); }
    public DoubleProperty noteEvaluationProperty() { return noteEvaluation; }

    public double getNoteConsultation() { return noteConsultation.get(); }
    public void setNoteConsultation(double v) { noteConsultation.set(v); }
    public DoubleProperty noteConsultationProperty() { return noteConsultation; }

    public double getMoyenne() { return moyenne.get(); }
    public void setMoyenne(double v) { moyenne.set(v); }
    public DoubleProperty moyenneProperty() { return moyenne; }

    // Compat si jamais tu avais encore "moyenneTotale" quelque part
    public double getMoyenneTotale() { return getMoyenne(); }
    public void setMoyenneTotale(double v) { setMoyenne(v); }
    public DoubleProperty moyenneTotaleProperty() { return moyenne; }

    public Integer getNiveauId() { return niveauId.get(); }
    public void setNiveauId(Integer v) { niveauId.set(v); }
    public ObjectProperty<Integer> niveauIdProperty() { return niveauId; }

    public String getNiveauLibelle() { return niveauLibelle.get(); }
    public void setNiveauLibelle(String v) { niveauLibelle.set(v); }
    public StringProperty niveauLibelleProperty() { return niveauLibelle; }
}
