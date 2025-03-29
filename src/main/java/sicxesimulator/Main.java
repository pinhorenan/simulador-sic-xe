package sicxesimulator;

import sicxesimulator.simulation.model.Model;
import sicxesimulator.simulation.view.View;

public class Main {
    public static void main(String[] args) {
        Model model = new Model();  // Crie e configure seu Model aqui
        View.setModel(model);           // Injete o Model na MainApp
        javafx.application.Application.launch(View.class, args);
    }
}
