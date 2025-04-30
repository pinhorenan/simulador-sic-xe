package sicxesimulator;

import sicxesimulator.ui.core.model.Model;
import sicxesimulator.ui.core.view.View;

public class Main {
    public static void main(String[] args) {
        Model model = new Model();
        View.setModel(model);
        javafx.application.Application.launch(View.class, args);
    }
}
