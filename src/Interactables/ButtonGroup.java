package Interactables;

import boilerplate.rendering.*;
import boilerplate.rendering.text.FontManager;
import boilerplate.utility.Vec2;
import common.Constants;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.*;

public class ButtonGroup {
    public static class Button {
        public Vec2 pos;
        public Vec2 size;
        public String text;
        public Color color = Color.white;
        public FontManager.LoadedFont font = FontManager.getLoadedFont(1);

        public boolean isMouseInBounds = false;

        public Button(Vec2 pos, Vec2 size, String text) {
            this.pos = pos;
            this.size = size;
            this.text = text;
        }

        public boolean isPointInBounds(Vec2 point) {
            return pos.x < point.x && point.x < pos.x + size.x &&
                    pos.y < point.y && point.y < pos.y + size.y;
        }

        public void appendToBufferBuilder(BufferBuilder2f sb) {
            // outline
            float[] additionalFloats = new float[] {-1, -1, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), 0, 0};

            Shape2d.Poly outlinePoly = Shape2d.createRectOutline(pos, size, 3, new ShapeMode.Append(additionalFloats));
            sb.pushSeparatedPolygon(outlinePoly);

            int textHeight = font.glyphMap.get(' ').height;
            float textWidth = font.findLineWidth(text);

            // text
            if (text != null && !text.isEmpty()) {
                float accumulatedX = size.x * .5f - textWidth * .5f;
                float yPosMiddle = pos.y + (size.y * .5f - textHeight * .5f);
                boolean initial = true;

                for (char c : text.toCharArray()) {
                    FontManager.Glyph glyph = font.getGlyph(c);
                    Vec2 size = new Vec2(glyph.width, glyph.height);
                    Vec2 topLeft = new Vec2(pos.x + accumulatedX, yPosMiddle);

                    Shape2d.Poly texturePoints = Shape2d.createRect(glyph.texTopLeft, glyph.texSize);
                    float[] colorVars = new float[] {color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), 0, 0};

                    ShapeMode.UnpackAppend mode = new ShapeMode.UnpackAppend(texturePoints.toArray(), colorVars);
                    Shape2d.Poly charPoly = Shape2d.createRect(topLeft, size, mode);

                    if (initial) {
                        sb.pushSeparatedPolygon(charPoly);
                        initial = false;
                    }
                    else sb.pushPolygon(charPoly);
                    accumulatedX += (int) size.x;
                }
            }

            // hovering
            if (isMouseInBounds) {
                float[] colorFloats = new float[] {-1, -1, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), 1};
                java.util.List<float[]> wobbleIndexes = List.of(new float[] {0}, new float[] {1}, new float[] {2}, new float[] {3});
                Shape2d.Poly poly = Shape2d.createRect(pos, size, new ShapeMode.AppendUnpack(colorFloats, wobbleIndexes));
                sb.pushSeparatedPolygon(poly);
            }
        }
    }

    private final ArrayList<Button> buttons = new ArrayList<>();

    private final ShaderHelper sh = new ShaderHelper();
    private final VertexArray va = new VertexArray();
    private final VertexBuffer vb = new VertexBuffer();
    private final BufferBuilder2f sb = new BufferBuilder2f();

    public boolean hasChanged = false;

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
        vaLayout.pushFloat(1);  // is mouse hovering
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
        for (Button btn : buttons) {
            boolean within = btn.isPointInBounds(mousePos);
            if (within != btn.isMouseInBounds) {
                btn.isMouseInBounds = within;
                hasChanged = true;
            }
        }
    }

    public void mouseClicked() {
        for (Button btn : buttons) {
            if (btn.isMouseInBounds) {
                break;
            }
        }
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
        ShaderHelper.uniform1f(sh, "time", (float) glfwGetTime());
        Renderer.draw(GL_TRIANGLE_STRIP, va, sb.getVertexCount());
    }
}
