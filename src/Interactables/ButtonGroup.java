package Interactables;

import boilerplate.utility.Vec2;

import java.util.ArrayList;
import java.util.Arrays;

public class ButtonGroup extends InteractableGroup {
    private final ArrayList<Button> buttons = new ArrayList<>();
    public boolean radioToggles = false;
    public ToggleButton radioBtnSelected;

    public void addButton(Button ...btns) {
        buttons.addAll(Arrays.asList(btns));
        hasChanged = true;
    }

    public void clear() {
        buttons.clear();
        hasChanged = true;
    }

    public void updateMouse(Vec2 mousePos) {
        if (!visible) return;
        for (Button btn : buttons) {
            boolean within = btn.isPointInBounds(mousePos);
            if (within == btn.mouseHovering) continue;
            btn.setMouseHovering(within);
            hasChanged = true;
        }
    }

    public void mouseClicked() {
        if (!visible) return;
        for (Button btn : buttons) {
            if (btn instanceof ToggleButton toggleBtn) {
                if (!btn.mouseHovering || (radioToggles && toggleBtn.toggled)) continue;
                if (radioToggles) {
                    unToggleCurrent();
                    radioBtnSelected = toggleBtn;
                }
                toggleBtn.toggle();
                hasChanged = true;
                break;
            }
            if (!btn.mouseHovering) continue;
            btn.click();
            break;
        }
    }

    public void toggleAll(boolean val) {
        toggleAll(val, null);
    }

    public void toggleAll(boolean val, ToggleButton exclude) {
        for (Button btn : buttons) {
            if (btn instanceof ToggleButton toggleBtn) {
                if (exclude != null && toggleBtn == exclude) continue;
                toggleBtn.toggle(val);
            }
        }
    }

    public void toggleBtn(ToggleButton toggleBtn, boolean val) {
        unToggleCurrent();
        if (radioToggles) radioBtnSelected = toggleBtn;
        toggleBtn.toggle(val);
        hasChanged = true;
    }

    public void unToggleCurrent() {
        if (radioBtnSelected == null) return;
        radioBtnSelected.toggle(false);
        radioBtnSelected = null;
        hasChanged = true;
    }

    @Override
    public void reBuildBuffer() {
        for (Button btn : buttons) btn.appendToBufferBuilder(sb);
    }
}
