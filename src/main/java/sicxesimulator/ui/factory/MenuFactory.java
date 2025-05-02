package sicxesimulator.ui.factory;

import javafx.scene.control.MenuItem;

public final class MenuFactory {
    private MenuFactory() {}

    /**
     * Cria um MenuItem com texto e action vinculados.
     *
     * @param text   texto exibido
     * @param action código a rodar no clique
     */
    public static MenuItem item(String text, Runnable action) {
        MenuItem mi = new MenuItem(text);
        mi.setOnAction(e -> action.run());
        return mi;
    }
}
