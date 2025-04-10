package Interactables;

import boilerplate.rendering.*;
import boilerplate.rendering.text.FontManager;
import common.Constants;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;

public abstract class InteractableGroup {
    final ShaderHelper sh = new ShaderHelper();
    final VertexArray va = new VertexArray();
    final VertexBuffer vb = new VertexBuffer();
    final BufferBuilder2f sb = new BufferBuilder2f(true);

    boolean visible = true;
    public boolean hasChanged = false;

    public void setupBufferObjects() {
        sh.autoInitializeShadersMulti("shaders/interactable.glsl");
        ShaderHelper.uniformResolutionData(sh, Constants.SCREEN_SIZE, Constants.PROJECTION_MATRIX);
        ShaderHelper.uniform1i(sh, "fontTexture", FontManager.FONT_TEXTURE_SLOT);

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

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean val) {
        if (visible == val) return;;
        visible = val;
        hasChanged = true;
    }

    /**
     * Put all the stuff into the buffer builder, clearing of sb and setting to vb is done automatically
     */
    public abstract void reBuildBuffer();

    public void renderAll() {
        if (!visible) return;
        if (hasChanged) {
            hasChanged = false;
            sb.clear();
            reBuildBuffer();
            vb.bufferSetData(sb);
        }

        // render buffer!
        sh.bind();
        ShaderHelper.uniform1f(sh, "time", (float) glfwGetTime());
        Renderer.draw(GL_TRIANGLE_STRIP, va, sb.getVertexCount());
    }
}
