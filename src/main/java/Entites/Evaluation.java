package Entites;

public class Evaluation {
    private int id_eval;
    private int id_cours;
    private String question;
    private String choix1;
    private String choix2;
    private String choix3;
    private String bonne_reponse;
    private int score;
    private Cours cours; // Pour la jointure

    // Constructeurs
    public Evaluation() {}

    public Evaluation(int id_eval, int id_cours, String question, String choix1,
                      String choix2, String choix3, String bonne_reponse, int score) {
        this.id_eval = id_eval;
        this.id_cours = id_cours;
        this.question = question;
        this.choix1 = choix1;
        this.choix2 = choix2;
        this.choix3 = choix3;
        this.bonne_reponse = bonne_reponse;
        this.score = score;
    }

    public Evaluation(int id_cours, String question, String choix1, String choix2,
                      String choix3, String bonne_reponse, int score) {
        this.id_cours = id_cours;
        this.question = question;
        this.choix1 = choix1;
        this.choix2 = choix2;
        this.choix3 = choix3;
        this.bonne_reponse = bonne_reponse;
        this.score = score;
    }

    // Getters et Setters
    public int getId_eval() { return id_eval; }
    public void setId_eval(int id_eval) { this.id_eval = id_eval; }

    public int getId_cours() { return id_cours; }
    public void setId_cours(int id_cours) { this.id_cours = id_cours; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getChoix1() { return choix1; }
    public void setChoix1(String choix1) { this.choix1 = choix1; }

    public String getChoix2() { return choix2; }
    public void setChoix2(String choix2) { this.choix2 = choix2; }

    public String getChoix3() { return choix3; }
    public void setChoix3(String choix3) { this.choix3 = choix3; }

    public String getBonne_reponse() { return bonne_reponse; }
    public void setBonne_reponse(String bonne_reponse) { this.bonne_reponse = bonne_reponse; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public Cours getCours() { return cours; }
    public void setCours(Cours cours) { this.cours = cours; }

    @Override
    public String toString() {
        return "Evaluation{" +
                "id_eval=" + id_eval +
                ", question='" + question + '\'' +
                ", score=" + score +
                '}';
    }
}