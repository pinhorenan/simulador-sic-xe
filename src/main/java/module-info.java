module sicxesimulator {
    // common
    exports sicxesimulator.common.utils;

    // hardware
    exports sicxesimulator.hardware;
    exports sicxesimulator.hardware.cpu.core;
    exports sicxesimulator.hardware.cpu.decoder;
    exports sicxesimulator.hardware.cpu.exec.arith;
    exports sicxesimulator.hardware.cpu.exec.logic;
    exports sicxesimulator.hardware.cpu.exec.load;
    exports sicxesimulator.hardware.cpu.exec.jump;
    exports sicxesimulator.hardware.cpu.exec.store;
    exports sicxesimulator.hardware.cpu.exec.sys;
    exports sicxesimulator.hardware.data;

    //software
    exports sicxesimulator.software.assembler;
    exports sicxesimulator.software.assembler.data;
    exports sicxesimulator.software.assembler.util;
    exports sicxesimulator.software.data;
    exports sicxesimulator.software.linker;
    exports sicxesimulator.software.loader;
    exports sicxesimulator.software.macroprocessor;

    // ui
    exports sicxesimulator.ui.controller;
    exports sicxesimulator.ui.model;
    exports sicxesimulator.ui.view;
    exports sicxesimulator.ui.components.buttons;
    exports sicxesimulator.ui.components.panels;
    exports sicxesimulator.ui.components.tables;
    exports sicxesimulator.ui.interfaces;
    exports sicxesimulator.ui.util;

    // requirements
    requires java.logging;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.swing;
    requires javafx.media;
}