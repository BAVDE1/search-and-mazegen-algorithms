package Interactables;

import boilerplate.common.BoilerplateConstants;
import boilerplate.rendering.*;
import boilerplate.rendering.Shape;
import boilerplate.utility.Vec2;
import boilerplate.utility.Vec4;
import common.Constants;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;

public class ButtonGroup {
    public static class Button {
        public Vec2 pos;
        public Vec2 size;
        public String text;
        public Color color = Color.WHITE;

        public Button(Vec2 pos, Vec2 size, String text) {
            this.pos = pos;
            this.size = size;
            this.text = text;
        }

        public void appendToBufferBuilder(BufferBuilder2f sb) {
            Shape.Quad q = Shape.createRect(pos, size);
            Vec4 c = new Vec4(color);
            sb.pushRawSeparatedVertices(new float[] {
                    q.a.x, q.a.y, -1, -1, c.x, c.y, c.z, c.w,
                    q.b.x, q.b.y, -1, -1, c.x, c.y, c.z, c.w,
                    q.c.x, q.c.y, -1, -1, c.x, c.y, c.z, c.w,
                    q.d.x, q.d.y, -1, -1, c.x, c.y, c.z, c.w
            });
        }
    }

    private final ArrayList<Button> buttons = new ArrayList<>();

    private final ShaderHelper sh = new ShaderHelper();
    private final VertexArray va = new VertexArray();
    private final VertexBuffer vb = new VertexBuffer();
    private final BufferBuilder2f sb = new BufferBuilder2f();

    private boolean hasChanged = false;

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
        va.pushBuffer(vb, vaLayout);

        sb.setAutoResize(true);
        sb.setAdditionalVertFloats(vaLayout.getTotalItems() - 2);  // minus pos
    }

    public void addButton(Button btn) {
        buttons.add(btn);
        hasChanged = true;
    }

    public void clear() {
        buttons.clear();
        hasChanged = true;
    }

    public void renderAll() {
        if (hasChanged) {
            hasChanged = false;
            sb.clear();
            for (Button btn : buttons) btn.appendToBufferBuilder(sb);
            vb.bufferSetData(sb);
        }

        // render buffer!
        sh.bind();
        Renderer.draw(GL_TRIANGLE_STRIP, va, sb.getVertexCount());
    }
}
