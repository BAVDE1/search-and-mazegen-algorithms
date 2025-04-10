package Interactables;

import boilerplate.rendering.BufferBuilder2f;
import boilerplate.rendering.Shape2d;
import boilerplate.rendering.ShapeMode;
import boilerplate.rendering.text.FontManager;
import boilerplate.rendering.text.TextRenderer;
import boilerplate.utility.Vec2;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Button {
    public interface ButtonCallback {
        void call(Button btn);
    }

    public Vec2 pos;
    public Vec2 size;
    public String text;
    public float textScale = 1;
    public Color color = Color.white;
    public FontManager.LoadedFont font = FontManager.getLoadedFont(2);

    public boolean mouseHovering = false;
    public boolean wobbling = false;

    private final ArrayList<ButtonCallback> callbacks = new ArrayList<>();

    public Button(Vec2 pos, Vec2 size, String text) {
        this.pos = pos;
        this.size = size;
        this.text = text;
    }

    public Button(Vec2 pos, Vec2 size, String text, Color color) {
        this(pos, size, text);
        this.color = color;
    }

    public void setMouseHovering(boolean val) {
        mouseHovering = val;
        wobbling = val;
    }

    public void addCallback(ButtonCallback callback) {
        callbacks.add(callback);
    }

    public void clearCallbacks() {
        callbacks.clear();
    }

    public void click() {
        fireCallbacks();
    }

    public void fireCallbacks() {
        for (ButtonCallback callback : callbacks) callback.call(this);
    }

    public boolean isPointInBounds(Vec2 point) {
        return pos.x < point.x && point.x < pos.x + size.x &&
                pos.y < point.y && point.y < pos.y + size.y;
    }

    public float getWobbleSpeed() {
        return 1;
    }

    public void appendToBufferBuilder(BufferBuilder2f sb) {
        // outline
        float[] outlineFloats = new float[]{-1, -1, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), 0, 0};
        Shape2d.Poly outlinePoly = Shape2d.createRectOutline(pos, size, 3, new ShapeMode.Append(outlineFloats));
        sb.pushSeparatedPolygon(outlinePoly);

        // text
        if (text != null && !text.isEmpty()) {
            int textHeight = (int) (font.getLineHeight() * textScale);
            float textWidth = font.findLineWidth(text) * textScale;

            float[] textFloats = new float[]{color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), 0, 0};
            Vec2 midOffset = size.mul(.5f).sub(new Vec2(textWidth, textHeight).mul(.5f));
            TextRenderer.pushTextToBuilder(sb, text, font, pos.add(midOffset), textFloats, textScale);
        }

        // hovering wobble
        if (wobbling) {
            float[] wobbleFloats = new float[]{-1, -1, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), getWobbleSpeed()};
            java.util.List<float[]> wobbleIndexes = List.of(new float[]{0}, new float[]{1}, new float[]{2}, new float[]{3});
            Shape2d.Poly poly = Shape2d.createRect(pos, size, new ShapeMode.AppendUnpack(wobbleFloats, wobbleIndexes));
            sb.pushSeparatedPolygon(poly);
        }
    }
}
