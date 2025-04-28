package sicxesimulator;

import sicxesimulator.ui.model.Model;
import sicxesimulator.ui.view.View;

public class Main {
    public static void main(String[] args) {
        Model model = new Model();
        View.setModel(model);
        javafx.application.Application.launch(View.class, args);
    }
}
