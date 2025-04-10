package Interactables;

import boilerplate.utility.Vec2;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class InputGroup extends InteractableGroup {
    private final ArrayList<Input> inputs = new ArrayList<>();
    private Input selectedInput;

    public void addInput(Input ...inputs) {
        for (Input input : inputs) {
            this.inputs.add(input);
            input.setGroup(this);
        }
        hasChanged = true;
    }

    public void clear() {
        inputs.clear();
        hasChanged = true;
    }

    public void keyPressed(int key, int scancode) {
        if (!visible || selectedInput == null) return;
        if (key == GLFW_KEY_ENTER || key == GLFW_KEY_TAB) {
            unselectCurrentInput();
            return;
        }
        selectedInput.keyPressed(key, scancode);
    }

    public void updateMouse(Vec2 mousePos) {
        if (!visible) return;
        for (Input input : inputs) {
            boolean within = input.isPointInBounds(mousePos);
            if (within == input.mouseHovering) continue;
            input.setMouseHovering(within);
            if (!input.selected) hasChanged = true;
        }
    }

    public void mouseClicked() {
        if (!visible) return;

        unselectCurrentInput();
        for (Input input : inputs) {
            if (!input.mouseHovering) continue;
            input.select();
            selectedInput = input;
            hasChanged = true;
            break;
        }
    }

    public void unselectCurrentInput() {
        if (selectedInput == null) return;
        selectedInput.unselect();
        selectedInput = null;
        hasChanged = true;
    }

    @Override
    public void reBuildBuffer() {
        for (Input input : inputs) input.appendToBufferBuilder(sb);
    }
}
