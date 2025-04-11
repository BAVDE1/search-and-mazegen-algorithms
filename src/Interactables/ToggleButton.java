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
        if (!mouseHovering) wobbling = val;
        fireCallbacks();
    }

    @Override
    public float getWobbleSpeed() {
        return mouseHovering ? 1:.2f;
    }

    @Override
    public void setMouseHovering(boolean val) {
        mouseHovering = val;
        if (val || !toggled) wobbling = val;
    }

    @Override
    public void click() {
        toggle();
        fireCallbacks();
    }
}
