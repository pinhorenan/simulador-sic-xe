module sicxesimulator.hardware {
    requires sicxesimulator.common;
    exports sicxesimulator.hardware;
    exports sicxesimulator.hardware.cpu.core;
    exports sicxesimulator.hardware.cpu.decoder;
    exports sicxesimulator.hardware.cpu.exec.arith;
    exports sicxesimulator.hardware.cpu.exec.logic;
    exports sicxesimulator.hardware.cpu.exec.load;
    exports sicxesimulator.hardware.cpu.exec.jump;
    exports sicxesimulator.hardware.cpu.exec.store;
    exports sicxesimulator.hardware.cpu.exec.sys;
}
