package ru.otus.main_patterns.hw04.impl;

import ru.otus.main_patterns.hw04.interfaces.Rotatable;
import ru.otus.main_patterns.hw04.interfaces.UObject;
import ru.otus.main_patterns.hw04.model.Direction;

public class RotateAdapter implements Rotatable {
    public static final String DIRECTION = "Direction";
    public static final String ANGULAR_VELOCITY = "AngularVelocity";
    private final UObject uObject;

    public RotateAdapter(UObject uObject) {
        this.uObject = uObject;
    }

    @Override
    public Direction getDirection() {
        return (Direction) uObject.getProperty(DIRECTION);
    }

    @Override
    public int getAngularVelocity() {
        return (int) uObject.getProperty(ANGULAR_VELOCITY);
    }

    @Override
    public void setDirection(Direction newDirection) {
        uObject.setProperty(DIRECTION, newDirection);
    }
}
