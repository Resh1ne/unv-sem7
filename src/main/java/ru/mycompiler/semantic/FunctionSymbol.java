package ru.mycompiler.semantic;

import java.util.LinkedHashMap;
import java.util.Map;

public class FunctionSymbol extends Symbol {
    // Используем LinkedHashMap для сохранения порядка аргументов
    private final Map<String, Symbol> arguments = new LinkedHashMap<>();

    public FunctionSymbol(String name, String returnType) {
        super(name, returnType);
    }

    public void addArgument(Symbol arg) {
        arguments.put(arg.getName(), arg);
    }

    public Map<String, Symbol> getArguments() {
        return arguments;
    }
}