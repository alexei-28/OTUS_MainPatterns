package ru.otus.main_patterns.hw02.command;

import ru.otus.main_patterns.hw02.inter.Rotatable;
import ru.otus.main_patterns.hw02.model.Direction;

public class Rotate {
    private final Rotatable rotatable;

    public Rotate(Rotatable rotatable) {
        this.rotatable = rotatable;
    }

    public void execute() {
        Direction direction = rotatable.getDirection();
        Direction nextDirection = direction.next(rotatable.getAngularVelocity());
        rotatable.setDirection(nextDirection);
    }
}
