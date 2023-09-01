package net.flectone.messages;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TextParameters {

    private final String text;

    private final List<String> parameters = new ArrayList<>();

    public TextParameters(@NotNull String text) {
        this.text = text;
    }

    public void add(String parameter) {
        parameters.add(parameter);
    }

    public void add(List<String> parameters) {
        if (parameters == null || parameters.isEmpty()) return;
        this.parameters.addAll(parameters);
    }

    public List<String> getParameters() {
        return parameters;
    }

    public String getText() {
        return text;
    }

    public void remove(String parameter) {
        parameters.remove(parameter);
    }

    public void clear() {
        parameters.clear();
    }

    public boolean contains(String parameter) {
        return parameters.contains(parameter);
    }
}
