package sicxesimulator.model.systems;

import sicxesimulator.model.components.Machine;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Loader {

    public void loadProgram(String objectFilePath, Machine machine) throws IOException {
        List<String> records = Files.readAllLines(Paths.get(objectFilePath));
        int loadStartAddress = 0;
        int executionStartAddress = 0;
        boolean hasExecutionAddress = false;

        for (String record : records) {
            if (record.isEmpty()) continue;

            char type = record.charAt(0);
            switch (type) {
                case 'H':
                    loadStartAddress = processHeaderRecord(record);
                    break;
                case 'T':
                    processTextRecord(record, machine);
                    break;
                case 'E':
                    executionStartAddress = processEndRecord(record);
                    hasExecutionAddress = true;
                    break;
            }
        }

        // Define o PC com o endereço de execução ou o de carga se não houver registro E
        machine.setPC(hasExecutionAddress ? executionStartAddress : loadStartAddress);
        System.out.println("Programa carregado. PC inicial: " +
                String.format("%06X", machine.getPC().getIntValue()));
    }

    private int processHeaderRecord(String record) {
        // Formato: H<program name (6)>^<start address (6)>^<length (6)>
        String programName = record.substring(1, 7).trim();
        int startAddress = Integer.parseInt(record.substring(7, 13), 16);
        int programLength = Integer.parseInt(record.substring(13, 19), 16);

        System.out.println("Carregando programa: " + programName +
                " Tamanho: " + programLength + " bytes");
        return startAddress;
    }

    private void processTextRecord(String record, Machine machine) {
        // Formato: T<start address (6)>^<length (2)>^<data (hex pairs)>
        int startAddress = Integer.parseInt(record.substring(1, 7), 16);
        int byteCount = Integer.parseInt(record.substring(7, 9), 16);
        String data = record.substring(9);

        // Valida integridade dos dados
        if (data.length() != byteCount * 2) {
            throw new IllegalArgumentException("Registro T inválido. Esperados " +
                    byteCount * 2 + " caracteres hex, obtidos " +
                    data.length());
        }

        // Carrega cada byte na memória
        for (int i = 0; i < data.length(); i += 2) {
            int address = startAddress + (i / 2);
            String byteValue = data.substring(i, i + 2);
            machine.getMemory().write(address, byteValue);
        }
    }

    private int processEndRecord(String record) {
        // Formato: E<start address (6)>
        return record.length() > 1 ?
                Integer.parseInt(record.substring(1, 7), 16) :
                0;
    }
}