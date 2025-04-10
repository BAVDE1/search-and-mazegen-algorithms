package Interactables;

import boilerplate.utility.Vec2;

import java.awt.*;

public class ToggleButton extends Button {
    public boolean toggled = false;

    public ToggleButton(Vec2 pos, Vec2 size, String text) {
        super(pos, size, text);
    }

    public ToggleButton(Vec2 pos, Vec2 size, String text, Color color) {
        super(pos, size, text, color);
    }

    public void toggle() {
        toggle(!toggled);
    }

    public void toggle(boolean val) {
        if (val == toggled) return;
        toggled = val;
        if (!isMouseHovering) isWobbling = val;
        fireCallbacks();
    }

    @Override
    public float getWobbleSpeed() {
        return isMouseHovering ? 1:.4f;
    }

    @Override
    public void setMouseHovering(boolean val) {
        isMouseHovering = val;
        if (val || !toggled) isWobbling = val;
    }

    @Override
    public void click() {
        toggle();
        fireCallbacks();
    }
}
