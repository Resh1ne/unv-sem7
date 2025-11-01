package ru.mycompiler.semantic;

public abstract class Symbol {
    protected String name;
    protected String type;

    public Symbol(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }
    public String getType() { return type; }
}