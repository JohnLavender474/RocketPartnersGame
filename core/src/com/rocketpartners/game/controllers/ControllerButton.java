package com.rocketpartners.game.controllers;

import com.badlogic.gdx.Input;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ControllerButton {
    LEFT(Input.Keys.A),
    RIGHT(Input.Keys.D),
    UP(Input.Keys.W),
    DOWN(Input.Keys.S),
    A(Input.Keys.K),
    B(Input.Keys.J),
    START(Input.Keys.ENTER);

    private final int defaultKeyboardKey;
}
