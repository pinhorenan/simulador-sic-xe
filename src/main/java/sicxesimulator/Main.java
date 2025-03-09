package sicxesimulator;

import sicxesimulator.simulator.model.Model;
import sicxesimulator.simulator.view.MainView;

public class Main {
    public static void main(String[] args) {
        Model model = new Model();  // Crie e configure seu Model aqui
        MainView.setModel(model);           // Injete o Model na MainApp
        javafx.application.Application.launch(MainView.class, args);
    }
}

