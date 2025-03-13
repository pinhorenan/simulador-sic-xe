package sicxesimulator.linker;

import sicxesimulator.models.*;

import java.util.*;

public class Linker {

    /**
     * linkModules: une os módulos objeto em um único executável final.
     *
     * @param modules Lista de ObjectFile a serem ligados.
     * @param finalRelocation Se true, o Linker soma os endereços absolutos, gerando um executável fixo.
     *                        Se false, deixa a relocação para o Carregador, mas ainda podemos resolver importados.
     * @param loadAddress Endereço de carga inicial (em bytes) do primeiro módulo, se finalRelocation = true.
     * @param outputFileName Nome do programa final gerado.
     * @return Um ObjectFile representando o executável final.
     */
    public ObjectFile linkModules(List<ObjectFile> modules,
                                  boolean finalRelocation,
                                  int loadAddress,
                                  String outputFileName) {
        if (modules.isEmpty()) {
            throw new IllegalArgumentException("Nenhum módulo para linkar.");
        }

        // Passo 1: atribuir base a cada módulo e coletar símbolos públicos
        int currentBase = loadAddress;
        Map<ObjectFile, Integer> baseMap = new HashMap<>();
        Map<String, Integer> globalSymbols = new HashMap<>(); // symbolName -> globalAddress

        // Coleta de símbolos
        for (ObjectFile mod : modules) {
            baseMap.put(mod, currentBase);

            // para cada símbolo do SymbolTable do mod
            for (var entry : mod.getSymbolTable().getAllSymbols().entrySet()) {
                String symName = entry.getKey();
                SymbolTable.SymbolInfo info = entry.getValue();
                if (info.isPublic) {
                    int globalAddr = finalRelocation
                            ? (currentBase + info.address)
                            : (info.address);
                    // se nao for reloc final, podemos armazenar somente offset local
                    globalSymbols.put(symName, globalAddr);
                }
            }

            if (finalRelocation) {
                currentBase += mod.getProgramLength();
            } else {
                // se não reloc final, também podemos avançar base para concatenar.
                currentBase += mod.getProgramLength();
            }
        }

        // Passo 1.2: verifica símbolos importados
        for (ObjectFile mod : modules) {
            for (String sym : mod.getImportedSymbols().keySet()) {
                if (!globalSymbols.containsKey(sym)) {
                    throw new IllegalArgumentException("Símbolo importado [" + sym
                            + "] não definido em nenhum módulo!");
                }
            }
        }

        // Passo 2: Ajustar relocationRecords e concatenar
        int totalSize = modules.stream().mapToInt(ObjectFile::getProgramLength).sum();
        byte[] finalCode = new byte[totalSize];

        // SymbolTable final
        SymbolTable finalSymTab = new SymbolTable();

        int offsetGlobal = 0;

        for (ObjectFile mod : modules) {
            byte[] code = mod.getObjectCode();
            int base = baseMap.get(mod);

            // Ajusta relocation records se finalRelocation = true
            if (finalRelocation) {
                for (RelocationRecord rec : mod.getRelocationRecords()) {
                    fixAddressInCode(code, rec, mod, base, globalSymbols);
                }
            } else {
                // Caso não seja reloc final, podemos manter ou gerar M records,
                // ou simplesmente manter relocationRecords no final.
            }

            // Copia code no finalCode
            System.arraycopy(code, 0, finalCode, offsetGlobal, code.length);

            // Adiciona símbolos do mod no finalSymTab
            for (var entry : mod.getSymbolTable().getAllSymbols().entrySet()) {
                String symName = entry.getKey();
                SymbolTable.SymbolInfo info = entry.getValue();
                int finalAddr;
                if (finalRelocation) {
                    finalAddr = base + info.address;
                } else {
                    finalAddr = offsetGlobal + info.address;
                }
                finalSymTab.addSymbol(symName, finalAddr, info.isPublic);
            }

            offsetGlobal += code.length;
        }

        // Monta os relocationRecords do arquivo final
        List<RelocationRecord> finalRelocs = new ArrayList<>();
        if (!finalRelocation) {
            // Precisamos realocar no loader => reconstroi os relocationRecords
            // ajustando offsetGlobal para cada modulo
            int moduleBaseOffset = 0;
            for (ObjectFile mod : modules) {
                for (RelocationRecord rec : mod.getRelocationRecords()) {
                    // rec.offset + moduleBaseOffset
                    finalRelocs.add(new RelocationRecord(
                            rec.offset() + moduleBaseOffset,
                            rec.symbol(),
                            rec.length(),
                            rec.pcRelative()
                    ));
                }
                moduleBaseOffset += mod.getProgramLength();
            }
        }

        // Gera o ObjectFile final
        ObjectFile finalObj = new ObjectFile(
                finalRelocation ? loadAddress : modules.get(0).getStartAddress(),
                finalCode,
                finalSymTab,
                outputFileName,
                null, // rawSourceCode, se quisermos unificar
                Map.of(), // importado n/a no final
                finalRelocs
        );

        finalObj.setRelocated(finalRelocation);

        return finalObj;
    }

    /**
     * Ajusta o valor do code no offset rec.offset, somando a base do modulo ou
     * o globalSymbols do rec.getSymbol() se for importado.
     * Exemplo simplificado:
     */
    private void fixAddressInCode(
            byte[] code,
            RelocationRecord rec,
            ObjectFile mod,
            int moduleBase,
            Map<String,Integer> globalSymbols
    ) {
        int offset = rec.offset();
        int length = rec.length(); // ex: 3 (24 bits)
        if (offset + length > code.length) {
            throw new IllegalArgumentException("RelocationRecord fora dos limites do machineCode do modulo " + mod.getProgramName());
        }

        // Ler valor atual
        int value = 0;
        for (int i = 0; i < length; i++) {
            value = (value << 8) | (code[offset + i] & 0xFF);
        }

        // Descobrir se rec.getSymbol() é local ou importado
        Integer glob = globalSymbols.get(rec.symbol());
        if (glob == null) {
            // Pode ser local ao modulo?
            Integer localAddr = mod.getSymbolTable().getAddress(rec.symbol());
            if (localAddr != null) {
                glob = moduleBase + localAddr;
            } else {
                throw new IllegalArgumentException("Simbolo [" + rec.symbol() + "] nao encontrado em globalSymbols nem local no modulo " + mod.getProgramName());
            }
        }

        // soma
        int newValue = value + glob;

        if (rec.pcRelative()) {
            // Se for PC-Relative, ex.: newValue -= (offsetNoSIC??)
            // Depende do design do montador. Em SIC, normalmente subtrai base do PC + 3...
            // Exemplo simplificado, iremos ignorar:
            // newValue -= someConstant
        }

        // escrever de volta
        int temp = newValue;
        for (int i = length - 1; i >= 0; i--) {
            code[offset + i] = (byte)(temp & 0xFF);
            temp >>>= 8;
        }
    }
}
