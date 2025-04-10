package Interactables;

import boilerplate.rendering.*;
import boilerplate.utility.Vec2;
import common.Constants;

import java.util.ArrayList;
import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.*;

public class ButtonGroup {
    private final ArrayList<Button> buttons = new ArrayList<>();

    private final ShaderHelper sh = new ShaderHelper();
    private final VertexArray va = new VertexArray();
    private final VertexBuffer vb = new VertexBuffer();
    private final BufferBuilder2f sb = new BufferBuilder2f();

    private boolean visible = true;
    public boolean hasChanged = false;
    public boolean radioToggles = false;

    public void setupBufferObjects() {
        sh.autoInitializeShadersMulti("shaders/button.glsl");
        ShaderHelper.uniformResolutionData(sh, Constants.SCREEN_SIZE, Constants.PROJECTION_MATRIX);
        ShaderHelper.uniform1i(sh, "fontTexture", 1);

        va.genId();
        vb.genId();
        VertexArray.Layout vaLayout = new VertexArray.Layout();
        vaLayout.pushFloat(2);  // pos
        vaLayout.pushFloat(2);  // texture pos
        vaLayout.pushFloat(4);  // color
        vaLayout.pushFloat(1);  // wobble strength
        vaLayout.pushFloat(1);  // wobble index
        va.pushBuffer(vb, vaLayout);

        sb.setAutoResize(true);
        sb.setAdditionalVertFloats(vaLayout.getTotalItems() - 2);  // minus pos
    }

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
            if (within == btn.isMouseHovering) continue;
            btn.setMouseHovering(within);
            hasChanged = true;
        }
    }

    public void mouseClicked() {
        if (!visible) return;
        for (Button btn : buttons) {
            if (btn instanceof ToggleButton toggleBtn) {
                if (!btn.isMouseHovering || (radioToggles && toggleBtn.toggled)) continue;
                if (radioToggles) toggleAll(false, toggleBtn);
                toggleBtn.toggle();
                hasChanged = true;
                break;
            }
            if (!btn.isMouseHovering) continue;
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

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean val) {
        if (visible == val) return;;
        visible = val;
        hasChanged = true;
    }

    public void renderAll() {
        if (!visible) return;
        if (hasChanged) {
            hasChanged = false;
            sb.clear();
            for (Button btn : buttons) btn.appendToBufferBuilder(sb);
            vb.bufferSetData(sb);
        }

        // render buffer!
        sh.bind();
        ShaderHelper.uniform1f(sh, "time", (float) glfwGetTime());
        Renderer.draw(GL_TRIANGLE_STRIP, va, sb.getVertexCount());
    }
}
