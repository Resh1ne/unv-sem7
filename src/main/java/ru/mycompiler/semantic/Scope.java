package ru.mycompiler.semantic;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    private final Scope parent; // Ссылка на родительскую область видимости
    private final Map<String, Symbol> symbols = new HashMap<>();

    public Scope(Scope parent) {
        this.parent = parent;
    }

    // Добавить новый символ в ТЕКУЩУЮ область видимости.
    public void define(Symbol symbol) {
        symbols.put(symbol.getName(), symbol);
    }

    // Найти символ ТОЛЬКО в ТЕКУЩЕЙ области видимости.
    // Используется для проверки на повторное объявление.
    public Symbol resolve(String name) {
        return symbols.get(name);
    }

    // Найти символ в ТЕКУЩЕЙ, а затем во всех родительских областях видимости.
    // Используется для проверки, была ли переменная объявлена перед использованием.
    public Symbol lookup(String name) {
        Symbol symbol = resolve(name);
        if (symbol != null) {
            return symbol;
        }
        if (parent != null) {
            return parent.lookup(name);
        }
        return null; // Символ не найден ни в одной из областей видимости
    }
}