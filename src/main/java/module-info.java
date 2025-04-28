module sicxesimulator {
    // Common utilities
    exports sicxesimulator.common.utils;

    // Hardware
    exports sicxesimulator.hardware.cpu.control;
    exports sicxesimulator.hardware.cpu.decoder;
    exports sicxesimulator.hardware.cpu.exec;
    exports sicxesimulator.hardware.cpu.exec.arith;
    exports sicxesimulator.hardware.cpu.exec.jump;
    exports sicxesimulator.hardware.cpu.exec.load;
    exports sicxesimulator.hardware.cpu.exec.logic;
    exports sicxesimulator.hardware.cpu.exec.store;
    exports sicxesimulator.hardware.cpu.exec.sys;
    exports sicxesimulator.hardware.cpu.model;
    exports sicxesimulator.hardware.cpu.register;
    exports sicxesimulator.hardware.memory;
    exports sicxesimulator.hardware.system;

    // Software
    exports sicxesimulator.software.assembler;
    exports sicxesimulator.software.macroprocessor;
    exports sicxesimulator.software.linker;
    exports sicxesimulator.software.loader;
    exports sicxesimulator.software.data;
    exports sicxesimulator.software.util;

    // UI
    exports sicxesimulator.ui.components.buttons;
    exports sicxesimulator.ui.components.panels;
    exports sicxesimulator.ui.components.tables;
    exports sicxesimulator.ui.controller;
    exports sicxesimulator.ui.data.records;
    exports sicxesimulator.ui.interfaces;
    exports sicxesimulator.ui.model;
    exports sicxesimulator.ui.util;
    exports sicxesimulator.ui.view;

    // Dependencies
    requires java.logging;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.swing;
}
