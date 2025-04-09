package Interactables;

import boilerplate.rendering.BufferBuilder2f;
import boilerplate.rendering.Shape2d;
import boilerplate.rendering.ShapeMode;
import boilerplate.rendering.text.FontManager;
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
    public Color color = Color.white;
    public FontManager.LoadedFont font = FontManager.getLoadedFont(1);

    public boolean isMouseHovering = false;
    public boolean isWobbling = false;

    private final ArrayList<ButtonCallback> callbacks = new ArrayList<>();

    public Button(Vec2 pos, Vec2 size, String text) {
        this.pos = pos;
        this.size = size;
        this.text = text;
    }

    public void setMouseHovering(boolean val) {
        isMouseHovering = val;
        isWobbling = val;
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

    public void appendToBufferBuilder(BufferBuilder2f sb) {
        // outline
        float[] outlineFloats = new float[]{-1, -1, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), 0, 0};
        Shape2d.Poly outlinePoly = Shape2d.createRectOutline(pos, size, 3, new ShapeMode.Append(outlineFloats));
        sb.pushSeparatedPolygon(outlinePoly);

        // text
        int textHeight = font.glyphMap.get(' ').height;
        float textWidth = font.findLineWidth(text);

        if (text != null && !text.isEmpty()) {
            float[] textFloats = new float[]{color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), 0, 0};
            float accumulatedX = size.x * .5f - textWidth * .5f;
            float yPosMiddle = pos.y + (size.y * .5f - textHeight * .5f);
            boolean initial = true;

            for (char c : text.toCharArray()) {
                FontManager.Glyph glyph = font.getGlyph(c);
                Vec2 size = new Vec2(glyph.width, glyph.height);
                Vec2 topLeft = new Vec2(pos.x + accumulatedX, yPosMiddle);

                Shape2d.Poly texturePoints = Shape2d.createRect(glyph.texTopLeft, glyph.texSize);
                ShapeMode.UnpackAppend mode = new ShapeMode.UnpackAppend(texturePoints.toArray(), textFloats);
                Shape2d.Poly charPoly = Shape2d.createRect(topLeft, size, mode);

                if (initial) {
                    sb.pushSeparatedPolygon(charPoly);
                    initial = false;
                } else sb.pushPolygon(charPoly);
                accumulatedX += (int) size.x;
            }
        }

        // hovering wobble
        if (isWobbling) {
            float[] wobbleFloats = new float[]{-1, -1, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), 1};
            java.util.List<float[]> wobbleIndexes = List.of(new float[]{0}, new float[]{1}, new float[]{2}, new float[]{3});
            Shape2d.Poly poly = Shape2d.createRect(pos, size, new ShapeMode.AppendUnpack(wobbleFloats, wobbleIndexes));
            sb.pushSeparatedPolygon(poly);
        }
    }
}
