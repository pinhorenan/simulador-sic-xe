package sicxesimulator.ui.controller;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sicxesimulator.ui.util.DialogUtil;
import sicxesimulator.ui.view.Layout;
import sicxesimulator.ui.view.ViewUpdater;
import sicxesimulator.software.data.ObjectFile;
import sicxesimulator.ui.model.Model;
import sicxesimulator.ui.data.ObjectFileTableItem;
import sicxesimulator.ui.data.records.MemoryEntry;

import sicxesimulator.ui.data.records.RegisterEntry;
import sicxesimulator.ui.data.records.SymbolEntry;
import sicxesimulator.utils.Constants;
import sicxesimulator.utils.FileUtils;


import java.io.*;
import java.util.*;

public class Controller {
    private final Model model;
    private final Layout mainLayout;
    private final ViewUpdater updater;

    /**
     * Cria o controlador principal da interface gráfica.
     *
     * <p>Responsável por coordenar interações entre a interface visual (Layout),
     * o modelo de dados (Model) e atualizações da interface (ViewUpdater).</p>
     *
     * @param model Modelo de dados e lógica de aplicação.
     * @param mainLayout Layout principal da interface gráfica.
     */
    public Controller(Model model, Layout mainLayout) {
        this.model = model;
        this.mainLayout = mainLayout;
        this.updater = new ViewUpdater(this, mainLayout);
        this.model.addListener(this::initializeFilesView);
    }

    /**
     * Define o conteúdo do campo de entrada com o texto fornecido.
     *
     * @param content Texto a ser exibido no campo de entrada.
     */
    public void loadInputField(String content) {
        mainLayout.getInputPanel().setInputText(content);
    }

    /**
     * Realiza a montagem do código de entrada, incluindo expansão de macros e geração do código objeto.
     *
     * <p>Exibe mensagens de sucesso ou erro, e atualiza a tabela de arquivos montados.</p>
     */
    public void handleAssembleAction() {
        String rawSourceText = mainLayout.getInputPanel().getInputText();
        List<String> rawSourceLines = Arrays.asList(rawSourceText.split("\\r?\\n"));

        try {
            List<String> processedSourceLines = model.processCodeMacros(rawSourceLines);
            ObjectFile objectFile = model.assembleCode(rawSourceLines, processedSourceLines);

            mainLayout.getExecutionPanel().clearOutput();
            mainLayout.getInputPanel().setInputText(rawSourceText);

            DialogUtil.showInfoDialog("Programa montado", "Montagem concluída!", "O arquivo " + objectFile.getProgramName() + " foi montado com sucesso!");

            initializeFilesView();

        } catch (IllegalArgumentException | IOException e) {
            DialogUtil.showError("Erro na montagem: " + e.getMessage());
        }
    }

    /**
     * Executa a próxima instrução do programa carregado.
     *
     * <p>Atualiza a interface e verifica se a simulação foi concluída.</p>
     */
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

    /**
     * Reinicia a máquina e limpa a saída da execução.
     */
    public void handleRestartAction() {
        model.restartMachine();
        mainLayout.getExecutionPanel().getMachineOutput().clear();

        mainLayout.getExecutionPanel().getMachineOutput().appendText("Máquina reiniciada!\n");
        updateAllTables();
    }

    /**
     * Executa a ligação (link) entre dois ou mais arquivos objeto selecionados.
     *
     * <p>Permite definir endereço base e modo de relocação.</p>
     */
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

    /**
     * Remove arquivos objeto selecionados da tabela e do armazenamento local.
     */
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

    /**
     * Carrega um arquivo objeto selecionado para a memória da máquina.
     *
     * <p>Solicita endereço de carga se necessário.</p>
     */
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
                userLoadAddress = DialogUtil.askForInteger("Endereço de Carga", "Carregador", "Informe o endereço onde carregar:");
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

    /**
     * Limpa a memória da máquina e atualiza a tabela.
     */
    public void handleClearMemoryAction() {
        model.getMachine().getMemory().reset();
        updater.updateMemoryTableView();
        mainLayout.getExecutionPanel().getMachineOutput().appendText("Memória limpa!\n");
        // Atualiza a label da bottom bar
        if (mainLayout.getLabelsPanel() != null) {
            mainLayout.getLabelsPanel().updateMemorySizeLabel();
        }
    }

    /**
     * Solicita novo tamanho de memória via diálogo e aplica alteração.
     */
    public void handleChangeMemorySizeAction() {
        int newSizeInBytes;
        try {
            newSizeInBytes = DialogUtil.askForInteger("Atualizar tamanho da memória", "Digite o novo tamanho da memória (em bytes):", "Tamanho da memória (em bytes):");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    /**
     * Exibe uma janela com informações de ajuda sobre o simulador.
     */
    public void handleHelpAction() {
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
            A memória do simulador possui tamanho padrão de 24576 bytes (8192 palavras de 3 bytes). \s
            Esse valor pode ser alterado pelo menu "Memória", no topo da janela.
           \s
            A memória é endereçada por palavras de 3 bytes (24 bits), com endereços exibidos em formato hexadecimal. \s
            O conteúdo pode ser visualizado em diferentes formatos: hexadecimal, decimal ou octal.
                       \s
       \s"""),

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
            - Se quiser ver o código objeto gerado, selecione um arquivo na tabela de arquivos montados e clique em "Mostrar código objeto", no menu "Montador" do menu superior.
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

        helpStage.setScene(scene);
        helpStage.show();
    }

    /**
     * Cria uma seção visual formatada para o painel de ajuda.
     *
     * <p>Inclui um título destacado e um corpo com texto informativo,
     * envolto por um contorno suave com sombreamento.</p>
     *
     * @param title Título da seção.
     * @param body Texto explicativo da seção.
     * @return VBox estilizado contendo a seção montada.
     */
    private static VBox createSection(String title, String body) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #4d4d4d;");

        Label bodyLabel = new Label(body.strip());
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");

        VBox section = new VBox(5, titleLabel, bodyLabel);
        section.setStyle("""
        -fx-background-color: #fafafa;
        -fx-padding: 10px;
        -fx-border-radius: 5px;
        -fx-effect: dropshadow(gaussian, #cccccc, 10, 0, 0, 2);
    """);
        return section;
    }

    /**
     * Importa um arquivo '.asm' do disco para o campo de entrada.
     */
    public void handleImportASM() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos Assembly", "*.asm"));
        fileChooser.setInitialDirectory(new File(Constants.SAMPLES_DIR));

        File file = fileChooser.showOpenDialog(getStage());

        if (file != null) {
            try {
                String content = FileUtils.readFile(file.getAbsolutePath());
                loadInputField(content);
            } catch (IOException e) {
                DialogUtil.showError("Erro ao importar arquivo ASM: " + e.getMessage());
            }
        }
    }

    /**
     * Exibe o código objeto textual de um arquivo selecionado.
     */
    public void handleShowObjectCode() {
        var table = getMainLayout().getObjectFilePanel().getObjectFileTable();
        var sel = table.getSelectionModel().getSelectedItems();
        if (sel.size() != 1) {
            DialogUtil.showError("Selecione um único arquivo .obj");
            return;
        }

        ObjectFile obj = sel.getFirst().getObjectFile();

        String textual = obj.getObjectCodeAsString();  // Implementar no ObjectFile
        getMainLayout().getInputPanel().getExpandedCodeArea().setText("=== Código Objeto ===\n" + textual + "\n");
    }

    /**
     * Abre uma janela para o usuário definir o modo de ligação (absoluto/relocável).
     */
    public void handleSetLinkerModeAction() {
        // Pega o modo atual do Model
        Model.LinkerMode currentMode = model.getLinkerMode();

        // Abre um ChoiceDialog para trocar
        ChoiceDialog<Model.LinkerMode> dialog = new ChoiceDialog<>(currentMode, Model.LinkerMode.values());
        dialog.setTitle("Modo de Ligação");
        dialog.setHeaderText("Selecione o modo de ligação:");
        dialog.setContentText("Modo:");


        Optional<Model.LinkerMode> result = dialog.showAndWait();
        if (result.isPresent()) {
            Model.LinkerMode newMode = result.get();
            setLinkerMode(newMode);
        }
    }

    /**
     * Atualiza todas as tabelas da interface.
     */
    public void updateAllTables() {
        updater.updateAllTables();
    }

    /**
     * Atualiza todos os rótulos (labels) da interface.
     */
    public void updateAllLabels() {
        updater.updateAllLabels();
    }

    /**
     * Inicializa a tabela de arquivos objeto com os programas salvos no disco.
     */
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

    /**
     * Retorna o Stage principal da aplicação.
     */
    public Stage getStage() {
        return mainLayout.getRoot().getScene().getWindow() instanceof Stage ? (Stage) mainLayout.getRoot().getScene().getWindow() : null;
    }

    /**
     * Retorna o layout principal da interface.
     */
    public Layout getMainLayout() {
        return mainLayout;
    }

    /**
     * Retorna o modelo associado ao simulador.
     */
    public Model getModel() {
        return model;
    }

    /**
     * Retorna o atualizador de visualização da interface.
     */
    public ViewUpdater getUpdater() {
        return updater;
    }

    /**
     * Retorna o tamanho atual da memória da máquina.
     */
    public int getMemorySize() {
        return model.getMemorySize();
    }

    /**
     * Retorna os registros de memória formatados para exibição.
     */
    public List<MemoryEntry> getMemoryEntries() {
       return model.getMemoryEntries();
    }

    /**
     * Retorna os registradores formatados para exibição.
     */
    public List<RegisterEntry> getRegisterEntries() {
        return model.getRegisterEntries();
    }

    /**
     * Retorna os símbolos da Tabela de Símbolos formatados para exibição.
     */
    public List<SymbolEntry> getSymbolEntries() {
        return model.getSymbolEntries();
    }

    /**
     * Retorna a propriedade de controle que indica se um código foi carregado.
     */
    public BooleanProperty getCodeLoadedProperty() {
        return model.codeLoadedProperty();
    }

    /**
     * Retorna a propriedade de controle que indica se a simulação foi finalizada.
     */
    public BooleanProperty getSimulationFinishedProperty() {
        return model.simulationFinishedProperty();
    }

    /**
     * Define o modo de linkagem (relocável ou absoluto).
     *
     * @param mode Novo modo de ligação a ser aplicado.
     */
    public void setLinkerMode(Model.LinkerMode mode) {
        model.setLinkerMode(mode);
        updateAllLabels();
    }

    /**
     * Carrega da pasta de salvamento todos os arquivos objeto serializados (.meta).
     *
     * <p>Utiliza o métodO {@link ObjectFile#loadFromFile(File)} para desserializar os objetos salvos no disco.</p>
     * <p>Arquivos inválidos ou com erro de leitura são ignorados, exibindo uma mensagem de erro na interface.</p>
     *
     * @return Lista de {@link ObjectFile} carregados com sucesso.
     */
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

}
