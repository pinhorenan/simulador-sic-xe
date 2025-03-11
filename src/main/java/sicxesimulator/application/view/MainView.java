package sicxesimulator.application.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sicxesimulator.application.controller.Controller;
import sicxesimulator.application.controller.MenuBarController;
import sicxesimulator.application.components.buttons.MainButtons;
import sicxesimulator.application.model.Model;

public class MainView extends Application {
    private static Model injectedModel;

    /**
     * Inicializa a aplicação JavaFX.
     *
     * @param primaryStage O palco principal da aplicação
     */
    @Override
    public void start(Stage primaryStage) {

        // Configurações da janela
        primaryStage.setResizable(false);
        primaryStage.setTitle("Simulador SIC/XE");

        // Verifica se o model foi injetado
        if (injectedModel == null) {
            throw new IllegalStateException("O model não foi injetado! Utilize MainApp.setModel(model) antes de chamar launch().");
        }

        // 1️⃣ Criamos o layout principal primeiro
        MainLayout mainLayout = new MainLayout();

        // 2️⃣ Criamos o Controller e passamos o MainLayout
        Controller mainController = new Controller(injectedModel, mainLayout);

        // Enviar o controlador para o layout, permitindo a inicialização da ObjectFilePanel.
        mainLayout.setController(mainController);
        mainLayout.updateBottomBar();

        // 3️⃣ Criamos o MenuBarController e passamos o Controller
        MenuBarController menuBarController = new MenuBarController(mainController);

        // 4️⃣ Criamos os botões principais
        MainButtons simulationToolbar = new MainButtons(mainController, mainLayout);

        // 5️⃣ Atualizamos o MainLayout
        mainLayout.setMenuBarController(menuBarController);
        mainLayout.setMainButtons(simulationToolbar);

        // 6️⃣ Carregamos os arquivos `.obj` imediatamente
        mainController.initializeFilesView();

        // 7️⃣ Atualizamos as tabelas na inicialização para mostrar os valores zerados
        mainController.updateAllTables();
        mainController.updateAllLabels();

        // 8️⃣ Criamos e exibimos a cena principal
        Scene scene = new Scene(mainLayout.getRoot(), 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // 9️⃣ Configuração inicial
        simulationToolbar.setupBindings();
    }


    /// ===== Métodos auxiliares =====

    public static void setModel(Model model) {
        injectedModel = model;
    }
}
