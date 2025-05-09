package sicxesimulator.ui.core.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sicxesimulator.ui.components.buttons.AssemblerButtons;
import sicxesimulator.ui.components.buttons.ExecutionButtons;
import sicxesimulator.ui.components.buttons.FileListButtons;
import sicxesimulator.ui.core.controller.Controller;
import sicxesimulator.ui.core.model.Model;


/**
 * Classe principal da interface JavaFX que inicializa a janela do simulador SIC/XE.
 *
 * <p>Cria e conecta os seguintes elementos da interface:</p>
 * <ul>
 *   <li>Layout principal {@link Layout}</li>
 *   <li>Controlador principal {@link Controller}</li>
 *   <li>Botões de arquivos, execução e montagem</li>
 * </ul>
 *
 * <p>Responsável por injetar o modelo {@link Model} e disparar a atualização inicial da interface.</p>
 *
 * <p>Esta classe deve ser inicializada via {@code Application.launch()} após uso de {@link #setModel(Model)}.</p>
 */
public class View extends Application {
    private static Model injectedModel;

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setResizable(false);
        primaryStage.setTitle("Simulador SIC/XE");

        if (injectedModel == null) {
            throw new IllegalStateException("O model nao foi injetado! Utilize View.setModel(model) antes de chamar launch().");
        }

        Layout mainLayout = new Layout();
        Controller mainController = new Controller(injectedModel, mainLayout);
        mainLayout.setController(mainController);
        mainLayout.updateLabelsPanel();

        FileListButtons fileListButtons = new FileListButtons(mainController, mainLayout);
        ExecutionButtons executionButtons = new ExecutionButtons(mainController, mainLayout);
        AssemblerButtons assemblerButtons = new AssemblerButtons(mainController, mainLayout);

        mainLayout.setButtons(fileListButtons, executionButtons, assemblerButtons);

        mainController.initializeFilesView();
        mainController.updateAllTables();
        mainController.updateAllLabels();

        Scene scene = new Scene(mainLayout.getRoot(), 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        fileListButtons.setupBindings();
        executionButtons.setupBindings();
        assemblerButtons.setupBindings();
    }

    /**
     * Injeta o modelo da aplicação antes da execução da interface.
     * Deve ser chamado antes de {@code Application.launch()}.
     *
     * @param model Instância do modelo a ser utilizada pela interface.
     */
    public static void setModel(Model model) {
        injectedModel = model;
    }
}
