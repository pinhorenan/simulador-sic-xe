package sicxesimulator.simulation.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sicxesimulator.simulation.components.buttons.AssemblerButtons;
import sicxesimulator.simulation.components.buttons.ExecutionButtons;
import sicxesimulator.simulation.components.buttons.FileListButtons;
import sicxesimulator.simulation.controller.Controller;
import sicxesimulator.simulation.controller.MenuBarController;
import sicxesimulator.simulation.model.Model;

public class View extends Application {
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
        primaryStage.getIcons().add(new javafx.scene.image.Image("icon.png"));

        // Verifica se o model foi injetado
        if (injectedModel == null) {
            throw new IllegalStateException("O model nao foi injetado! Utilize MainApp.setModel(model) antes de chamar launch().");
        }

        //Criamos o layout principal primeiro
        Layout mainLayout = new Layout();

        // Criamos o Controller e passamos o MainLayout
        Controller mainController = new Controller(injectedModel, mainLayout);

        // Enviamos o controlador para o layout, permitindo a inicialização da ObjectFilePanel.
        mainLayout.setController(mainController);
        mainLayout.updateLabelsPanel();

        // Criamos o MenuBarController e passamos o Controller
        MenuBarController menuBarController = new MenuBarController(mainController);

        // Criamos os botões principais
        FileListButtons fileListButtons = new FileListButtons(mainController, mainLayout);
        ExecutionButtons executionButtons = new ExecutionButtons(mainController, mainLayout);
        AssemblerButtons assemblerButtons = new AssemblerButtons(mainController, mainLayout);

        // Atualizamos o MainLayout
        mainLayout.setMenuBarController(menuBarController);
        mainLayout.setButtons(fileListButtons, executionButtons, assemblerButtons);

        // Carregamos os arquivos ".obj" imediatamente
        mainController.initializeFilesView();

        // Atualizamos as tabelas na inicialização para mostrar os valores zerados
        mainController.updateAllTables();
        mainController.updateAllLabels();

        // Criamos e exibimos a cena principal
        Scene scene = new Scene(mainLayout.getRoot(), 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Configuração inicial
        fileListButtons.setupBindings();
        executionButtons.setupBindings();
        assemblerButtons.setupBindings();
    }

    /**
     * Define o model a ser injetado na aplicação.
     *
     * @param model O model a ser injetado
     */
    public static void setModel(Model model) {
        injectedModel = model;
    }
}
