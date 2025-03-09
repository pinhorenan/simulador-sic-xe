module sicxesimulator {
    // Dependências de produção
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires jdk.unsupported.desktop;
    requires java.logging;

    // Exporta apenas o(s) pacote(s) que deve(m) ser acessível(is) por
    // outros módulos ou pelo JavaFX.
    // "mainView" exportado para o módulo javafx.graphics, por exemplo, se houver FXML que precise de acesso.
    exports sicxesimulator.simulator.view to javafx.graphics;
    exports sicxesimulator.simulator.view.components to javafx.graphics;
    // Exemplo de pacote exportado sem restrição:
    // (Se o teste ou outra parte do código no mesmo módulo precisa enxergar "machine",
    //  e/ou se você quer que seja público para quem usar seu módulo.)
    exports sicxesimulator.machine;
    exports sicxesimulator.linker;
    exports sicxesimulator.loader;
    exports sicxesimulator.macroprocessor;
    exports sicxesimulator.assembler;
    exports sicxesimulator.assembler.models;
    exports sicxesimulator.utils;
    exports sicxesimulator.simulator.model;
    exports sicxesimulator.simulator.controller;
}
