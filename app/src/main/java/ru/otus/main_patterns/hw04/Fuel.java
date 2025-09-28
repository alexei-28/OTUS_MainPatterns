package ru.otus.main_patterns.hw04;

import java.util.Objects;

public class Fuel {
    private int value;

    public Fuel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Fuel fuel = (Fuel) o;
        return getValue() == fuel.getValue();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }

    @Override
    public String toString() {
        return "Fuel{" +
                "value=" + value +
                '}';
    }
}
