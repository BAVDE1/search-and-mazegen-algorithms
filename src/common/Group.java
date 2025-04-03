package common;

import boilerplate.rendering.BufferBuilder2f;
import boilerplate.rendering.Renderer;
import boilerplate.rendering.ShaderHelper;
import boilerplate.rendering.VertexArray;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;

public class Group {
    public abstract static class Item {
        public abstract void appendToBuff(BufferBuilder2f builder2f);
    }

    private final ArrayList<Item> items = new ArrayList<>();
    private ShaderHelper sh = new ShaderHelper();
    private final VertexArray va = new VertexArray();
    private final BufferBuilder2f builder2f = new BufferBuilder2f();

    public void setShader(ShaderHelper sh) {
        this.sh = sh;
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void clear() {
        items.clear();
    }

    public void renderAll() {
        builder2f.clear();
        for (Item item : items) {
            item.appendToBuff(builder2f);
        }

        sh.bind();
        Renderer.draw(GL_TRIANGLE_STRIP, va, builder2f.getVertexCount());
    }
}
