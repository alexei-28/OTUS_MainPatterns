package ru.otus.main_patterns.hw02.model;

import java.util.Objects;

public class Direction {
    private int alpha;
    private int angularVelocity;

    public Direction(int alpha, int angularVelocity) {
        this.alpha = alpha;
        this.angularVelocity = angularVelocity;
    }

    public Direction next(int angularVelocity) {
        return new Direction(alpha + angularVelocity, angularVelocity);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Direction direction = (Direction) o;
        return alpha == direction.alpha && angularVelocity == direction.angularVelocity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(alpha, angularVelocity);
    }

    @Override
    public String toString() {
        return "Direction{" +
                "alpha=" + alpha +
                ", angularVelocity=" + angularVelocity +
                '}';
    }
}
