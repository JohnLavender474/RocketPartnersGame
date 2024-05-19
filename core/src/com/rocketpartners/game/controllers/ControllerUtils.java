package com.rocketpartners.game.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.engine.controller.buttons.Button;
import com.engine.controller.buttons.Buttons;
import com.rocketpartners.game.Constants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ControllerUtils {

    private static ControllerUtils instance;

    public static ControllerUtils getInstance() {
        if (instance == null) {
            instance = new ControllerUtils();
        }
        return instance;
    }

    public Preferences getKeyboardPreferences() {
        return Gdx.app.getPreferences(Constants.PreferenceFiles.ROCKET_PARTNERS_KEYBOARD_PREFERENCES);
    }

    public Buttons loadButtons() {
        Buttons buttons = new Buttons();
        Preferences keyboardPreferences = getKeyboardPreferences();
        for (ControllerButton controllerButton : ControllerButton.values()) {
            int keyboardCode = keyboardPreferences.getInteger(controllerButton.name(), controllerButton.getDefaultKeyboardKey());
            buttons.put(controllerButton, new Button(keyboardCode, null, true));
        }
        return buttons;
    }

    public void resetButtonsToDefaults(Buttons buttons) {
        Preferences keyboardPreferences = getKeyboardPreferences();
        buttons.forEach(entry -> {
            ControllerButton controllerButton = (ControllerButton) entry.key;
            Button button = entry.value;

            int keyboardCode = controllerButton.getDefaultKeyboardKey();
            button.setKeyboardCode(keyboardCode);

            keyboardPreferences.putInteger(controllerButton.name(), keyboardCode);
        });
    }

}
