package Interactables;

import boilerplate.rendering.BufferBuilder2f;
import boilerplate.rendering.Shape2d;
import boilerplate.rendering.ShapeMode;
import boilerplate.utility.Vec2;

import java.awt.*;
import java.util.List;

public class InputRange extends Input {
    public boolean mouseHoveringBar = false;

    public int barRangeWidth = 80;
    public Vec2 barMargin = new Vec2(16);
    private Vec2 barPos;
    private Vec2 barSize;

    private Vec2 thumbPos;
    public Vec2 thumbSize = new Vec2(20);
    public boolean thumbHeld = false;

    public InputRange(Vec2 pos, String title, int defaultVal, int minValue, int maxValue) {
        super(pos, title, String.valueOf(defaultVal));
        rangeMin = minValue;
        rangeMax = maxValue;
    }

    public InputRange(Vec2 pos, String title, int defaultVal, int minValue, int maxValue, Color color) {
        super(pos, title, String.valueOf(defaultVal), color);
        rangeMin = minValue;
        rangeMax = maxValue;
    }

    @Override
    public void deselect() {
        thumbHeld = false;
        super.deselect();
    }

    public boolean isPointInBarArea(Vec2 point) {
        if (barPos == null || disabled) return false;
        return barPos.x < point.x && point.x < barPos.x + barSize.x &&
                barPos.y < point.y && point.y < barPos.y + barSize.y;
    }

    private void calculateBarPos() {
        barSize = new Vec2(barRangeWidth + (barMargin.x * 2), barMargin.y * 2);
        barPos = new Vec2(pos.x - (barSize.x * .5f), areaPos.y + areaSize.y + areaMargin.y);
    }

    private void updateThumbPos() {
        try {
            int val = clampValue(Integer.parseInt(value));
            float thumbX = pos.x + (((val - (rangeMax + rangeMin) * .5f) / (rangeMax - rangeMin)) * barRangeWidth);
            float thumbY = barPos.y + barMargin.y - (thumbSize.y * .5f);
            thumbPos = new Vec2(thumbX - thumbSize.x * .5f, thumbY);
        } catch (NumberFormatException _) {}  // not a valid number, just ignore it
    }

    public void updateValueFromMousePos(Vec2 mousePos) {
        float percent = (mousePos.x - barPos.x - barMargin.x) / barRangeWidth;
        int newValInt = clampValue((int) (rangeMin + ((rangeMax - rangeMin) * percent)));
        newValInt = validateIntValue(newValInt);
        String newVal = String.valueOf(newValInt);
        if (!newVal.equals(value)) {
            value = newVal;
            group.hasChanged = true;
            fireCallbacks();
        }
    }

    private int clampValue(int val) {
        return Math.clamp(val, rangeMin, rangeMax);
    }

    @Override
    public void appendToBufferBuilder(BufferBuilder2f sb) {
        super.appendToBufferBuilder(sb);

        if (barPos == null) calculateBarPos();  // needs to be done after areaPos.y and areaSize.y is calculated
        updateThumbPos();

        // range bar
        float[] barFloats = new float[]{-1, -1, color.getRed(), color.getGreen(), color.getBlue(), getAlpha(), mouseHoveringBar || thumbHeld ? .6f:.2f};
        java.util.List<float[]> wobbleIndexes = List.of(new float[]{0}, new float[]{1}, new float[]{2}, new float[]{3});
        Shape2d.Poly bar = Shape2d.createRect(barPos, barSize, new ShapeMode.AppendUnpack(barFloats, wobbleIndexes));
        sb.pushSeparatedPolygon(bar);

        // thumb
        float[] thumbFloats = new float[]{-1, -1, color.getRed(), color.getGreen(), color.getBlue(), getAlpha(), .2f};
        Shape2d.Poly thumb = Shape2d.createRect(thumbPos, thumbSize, new ShapeMode.AppendUnpack(thumbFloats, wobbleIndexes));
        sb.pushSeparatedPolygon(thumb);
    }
}
