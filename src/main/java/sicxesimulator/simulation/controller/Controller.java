package sicxesimulator.simulation.controller;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sicxesimulator.simulation.util.DialogUtil;
import sicxesimulator.simulation.view.Layout;
import sicxesimulator.simulation.view.ViewUpdater;
import sicxesimulator.software.data.ObjectFile;
import sicxesimulator.simulation.model.Model;
import sicxesimulator.simulation.data.ObjectFileTableItem;
import sicxesimulator.simulation.data.records.MemoryEntry;

import sicxesimulator.simulation.data.records.RegisterEntry;
import sicxesimulator.simulation.data.records.SymbolEntry;
import sicxesimulator.utils.Constants;

import java.io.*;
import java.util.*;

public class Controller {
    private final Model model;
    private final Layout mainLayout;
    private final ViewUpdater updater;

    public Controller(Model model, Layout mainLayout) {
        this.model = model;
        this.mainLayout = mainLayout;
        this.updater = new ViewUpdater(this, mainLayout);
        this.model.addListener(this::initializeFilesView);
    }

    public void initializeFilesView() {
        List<ObjectFile> files = loadSavedObjectFiles();
        var objectFileTable = mainLayout.getObjectFilePanel().getObjectFileTable();

        if (!files.isEmpty()) {
            objectFileTable.getItems().clear();
            for (ObjectFile file : files) {
                objectFileTable.addEntry(new ObjectFileTableItem(file));
            }
        }
    }

    public void handleAssembleAction() {
        String rawSourceText = mainLayout.getInputPanel().getInputText();
        List<String> rawSourceLines = Arrays.asList(rawSourceText.split("\\r?\\n"));

        try {
            List<String> processedSourceLines = model.processCodeMacros(rawSourceLines);
            ObjectFile objectFile = model.assembleCode(rawSourceLines, processedSourceLines);

            mainLayout.getExecutionPanel().clearOutput();
            mainLayout.getInputPanel().setInputText(rawSourceText);

            mainLayout.getExecutionPanel().getMachineOutput().appendText("Programa montado com sucesso!\n" + objectFile + "\n");

            initializeFilesView();

        } catch (IllegalArgumentException | IOException e) {
            DialogUtil.showError("Erro na montagem: " + e.getMessage());
        }
    }

    public void handleLinkSelectedFilesAction() {
        List<ObjectFile> selectedFiles = mainLayout.getObjectFilePanel()
                .getObjectFileTable()
                .getSelectedFiles();

        if (selectedFiles.size() < 2) {
            DialogUtil.showError("Selecione ao menos 2 módulos para ligar.");
            return;
        }

        int loadAddress;
        boolean fullReloc;

        // Se o modo de ligador for ABSOLUTO:
        if (model.getLinkerMode() == Model.LinkerMode.ABSOLUTO) {
            try {
                // Sempre pergunta o endereço base
                loadAddress = DialogUtil.askForInteger(
                        "Endereço de Carga",
                        "Linkagem Absoluta",
                        "Informe o endereço."
                );
                fullReloc = true;

            } catch (IOException e) {
                DialogUtil.showError("Operação cancelada ou inválida: " + e.getMessage());
                return;
            }
        } else {
            // Modo RELOCÁVEL => Endereço base = 0
            loadAddress = 0;

            // Pergunta se o usuário quer realocar agora ou deixar para o Loader
            fullReloc = DialogUtil.askForBoolean("Relocação Final",
                    "Deseja que o Linker aplique a realocação agora?");
        }

        String outputFileName = DialogUtil.askForString("Nomear arquivo de saída",
                "Linkagem de Módulos",
                "Informe o nome do arquivo de saída:");

        // Invoca a lógica de linkagem do Model
        ObjectFile linkedObject = model.linkObjectFiles(selectedFiles, loadAddress, fullReloc, outputFileName);

        DialogUtil.showInfoDialog("Arquivos Linkados",
                "Arquivos linkados com sucesso!",
                "O arquivo " + linkedObject.getProgramName() + " foi criado.");

        initializeFilesView();
    }

    public void handleDeleteSelectedFilesAction() {
        var objectFileTable = mainLayout.getObjectFilePanel().getObjectFileTable();
        List<ObjectFileTableItem> selectedItems = new ArrayList<>(objectFileTable.getSelectionModel().getSelectedItems());

        for (ObjectFileTableItem item : selectedItems) {
            ObjectFile objectFile = item.getObjectFile();
            model.deleteSavedProgram(objectFile);
        }

        // Atualiza a tabela removendo os itens selecionados
        objectFileTable.getItems().removeAll(selectedItems);

        // Se a tabela ficou vazia, forçamos uma atualização manual
        if (objectFileTable.getItems().isEmpty()) {
            initializeFilesView();  // Isso garante que a tabela será atualizada corretamente.
        }
    }

    public void handleNextAction() {
        if (model.codeLoadedProperty().get() && !model.simulationFinishedProperty().get()) {
            try {
                model.runNextInstruction();
                String log = model.getMachine().getControlUnit().getLastExecutionLog();
                mainLayout.getExecutionPanel().getMachineOutput().appendText(log + "\n");
                updater.updateAllTables();
                model.setSimulationFinished(model.getMachine().getControlUnit().isHalted());
                // Aguarda a atualização da interface antes de capturar o estado detalhado:
                Platform.runLater(() -> model.logDetailedState("Após execução de instrução em handleNextAction()"));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    model.logDetailedState("Erro em handleNextAction()");
                    DialogUtil.showError("Erro na execução: " + e.getMessage());
                });
            }
        } else {
            DialogUtil.showError("Nenhum programa montado ou simulação já concluída!");
        }
    }

    public void handleRestartAction() {
        model.restartMachine();
        mainLayout.getExecutionPanel().getMachineOutput().clear();
        mainLayout.getExecutionPanel().getMachineOutput().appendText("Máquina reiniciada!\n");
        updateAllTables();
    }

    public void handleLoadObjectFileAction() {
        var table = mainLayout.getObjectFilePanel().getObjectFileTable();
        List<ObjectFileTableItem> selectedItems = new ArrayList<>(table.getSelectionModel().getSelectedItems());

        if (selectedItems.isEmpty()) {
            DialogUtil.showError("Nenhum arquivo selecionado para carregar.");
            return;
        }

        ObjectFile selectedFile = selectedItems.getFirst().getObjectFile();
        int userLoadAddress;

        // Se o objeto final já está totalmente realocado (modo absoluto),
        // usamos esse endereço diretamente.
        if (selectedFile.isFullyRelocated()) {
            userLoadAddress = selectedFile.getStartAddress();
        } else {
            // Caso contrário, solicita ao usuário o endereço de carga.
            try {
                userLoadAddress = DialogUtil.askForInteger("Endereço de Carga", "Carregador", "Informe o endereço onde carregar (em HEX, use '0xNUMEROHEX'):");
            } catch (IOException e) {
                DialogUtil.showError("Operação cancelada ou inválida: " + e.getMessage());
                return;
            }
        }

        model.loadProgramToMachine(selectedFile, userLoadAddress);
        updateAllTables();
        mainLayout.getExecutionPanel().getMachineOutput().clear();
        mainLayout.getExecutionPanel().getMachineOutput().appendText("Programa carregado com sucesso!\n" + selectedFile + "\n");
    }

    public Stage getStage() {
        return mainLayout.getRoot().getScene().getWindow() instanceof Stage ? (Stage) mainLayout.getRoot().getScene().getWindow() : null;
    }

    public Model getModel() {
        return model;
    }

    public int getMemorySize() {
        return model.getMemorySize();
    }

    public List<MemoryEntry> getMemoryEntries() {
       return model.getMemoryEntries();
    }

    public List<RegisterEntry> getRegisterEntries() {
        return model.getRegisterEntries();
    }

    public List<SymbolEntry> getSymbolEntries() {
        return model.getSymbolEntries();
    }

    public BooleanProperty getCodeLoadedProperty() {
        return model.codeLoadedProperty();
    }

    public BooleanProperty getSimulationFinishedProperty() {
        return model.simulationFinishedProperty();
    }

    public void loadInputField(String content) {
        mainLayout.getInputPanel().setInputText(content);
    }

    public void handleClearMemory() {
        model.getMachine().getMemory().reset();
        updater.updateMemoryTableView();
        mainLayout.getExecutionPanel().getMachineOutput().appendText("Memória limpa!\n");
        // Atualiza a label da bottom bar
        if (mainLayout.getLabelsPanel() != null) {
            mainLayout.getLabelsPanel().updateMemorySizeLabel();
        }
    }

    public void handleChangeMemorySize(int newSizeInBytes) {
        try {
            model.getMachine().changeMemorySize(newSizeInBytes);
            model.setMemorySize(newSizeInBytes);
            mainLayout.getExecutionPanel().getMachineOutput().appendText("Memória alterada para " + newSizeInBytes + " bytes.\n");
            updater.updateMemoryTableView();
            if (mainLayout.getLabelsPanel() != null) {
                mainLayout.getLabelsPanel().updateMemorySizeLabel();
            }
        } catch (Exception e) {
            DialogUtil.showError("Erro ao alterar o tamanho da memória: " + e.getMessage());
        }
    }

    public void setLinkerMode(Model.LinkerMode mode) {
        model.setLinkerMode(mode);
        updateAllLabels();
    }

    public void updateAllTables() {
        updater.updateAllTables();
    }

    public void updateAllLabels() {
        updater.updateAllLabels();
    }

    public Layout getMainLayout() {
        return mainLayout;
    }

    public ViewUpdater getUpdater() {
        return updater;
    }

    private VBox createSection(String title, String body) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #4d4d4d;");

        Label bodyLabel = new Label(body.strip());
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");

        VBox section = new VBox(5, titleLabel, bodyLabel);
        section.setStyle("-fx-background-color: #fafafa; -fx-padding: 10px; -fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, #cccccc, 10, 0, 0, 2);");
        return section;
    }

    private List<ObjectFile> loadSavedObjectFiles() {
        List<ObjectFile> objectFiles = new ArrayList<>();
        File savedDir = new File(Constants.SAVE_DIR);

        if (savedDir.exists() && savedDir.isDirectory()) {
            // Carrega arquivos .meta (serializados)
            File[] metaFiles = savedDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".meta"));
            if (metaFiles != null) {
                for (File file : metaFiles) {
                    try {
                        ObjectFile objectFile = ObjectFile.loadFromFile(file); // desserializa
                        objectFiles.add(objectFile);
                    } catch (IOException e) {
                        DialogUtil.showError("Erro ao carregar arquivo meta: " + file.getName());
                    }
                }
            }
        }
        return objectFiles;
    }

    public void showHelpWindow() {
        Stage helpStage = new Stage();
        helpStage.setTitle("Ajuda do Simulador SIC/XE");
        helpStage.initModality(Modality.APPLICATION_MODAL);

        // Título
        Label titleLabel = new Label("Ajuda do Simulador SIC/XE");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f4f4f4; -fx-border-radius: 10px;");  // Fundo suave

        content.getChildren().add(titleLabel);

        content.getChildren().addAll(
                createSection("1. Memória", """
            A memória do simulador tem tamanho padrão de 24576 bytes. O valor pode ser alterado a partir do menu "Memória", no topo da janela.
            A memória é endereçada à palavras de 3 bytes (24 bits).
            O endereço de memória é exibido em formato hexadecimal.
            O conteúdo da memória pode ser exibido em diferentes formatos (hexadecimal, decimal, octal).
        """),

                createSection("2. Registradores", """
            O SIC/XE possui os seguintes registradores:
            - A: Acumulador
            - X: Registrador de índice
            - L: Registrador de ligação
            - B: Registrador base
            - S, T: Uso geral
            - F: Acumulador de ponto flutuante (48 bits)
            - PC: Contador de programa
            - SW: Palavra de status
        """),

                createSection("3. Modos de Endereçamento", """
            - Direto: usa endereço informado diretamente.
            - Indireto: o endereço é recuperado indiretamente da memória.
            - Imediato: o valor do operando está embutido na instrução.
        """),

                createSection("4. Instruções", """
            Instruções comuns:
            ADD, SUB, LDA, STA, COMP, J, JSUB, RSUB, entre outras.
            Consulte a especificação para ver a lista completa.
        """),

                createSection("5. Como usar o simulador", """
            - Começe importando um código .ASM a partir do menu "Arquivo", no canto superior esquerdo.
            - Você também pode escrever um código diretamente na caixa de entrada.
            - Clique em "Montar" para gerar o código objeto. O processo de montagem ativa a expansão dos macros, que acontece antes da montagem.
            - O código .ASM expandido pode ser visto na caixa de texto abaixo da caixa de entrada.
            - Para carregar o código objeto na memória, selecione um arquivo na tabela de arquivos montados e clique em "Carregar".
            - Use os botões de execução para rodar instruções.
        """),

                createSection("6. Ligação de Módulos", """
            - Para ligar módulos, selecione pelo menos 2 arquivos na tabela de arquivos montados e clique em "Linkar".
            - Carregue o programa resultante na memória para executá-lo.
            - O modo de ligação (absoluto ou relocável) pode ser escolhido no menu "Ligador".
            - Use os botões de execução para rodar instruções.
        """)
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setPrefSize(600, 500);

        Scene scene = new Scene(scrollPane);
        scene.getStylesheets().add("styles.css");  // Adiciona a folha de estilos externa (opcional)

        helpStage.setScene(scene);
        helpStage.show();
    }
}