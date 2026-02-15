package test;

import Entites.Event;
import Services.EventServices;
import Utils.Mydatabase;

public class Main {
    public static void main(String[] args) {
        Mydatabase.getInstance();

        EventServices rdvs = new EventServices();


    }
}
