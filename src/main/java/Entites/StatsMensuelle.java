package Entites;

public class StatsMensuelle {
    public int year;
    public int month;
    public double avgHumeur;
    public double avgStress;
    public double avgAttention;
    public int nbSeances;

    public double progressIndex() {
        return avgHumeur + avgAttention - avgStress;
    }
}
