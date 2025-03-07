module sicxesimulator {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires jdk.unsupported.desktop;
    requires java.logging;
    requires org.junit.jupiter.api;
    requires org.junit.platform.suite.api;

    // Só exporta o(s) pacote(s) que o seu código de teste (ou outra aplicação) precisa enxergar
    exports sicxesimulator.simulator.view to javafx.graphics;
    exports sicxesimulator.machine; // por ex., se o teste acessar classes neste pacote
}