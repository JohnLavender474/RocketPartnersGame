package com.rocketpartners.game.drawables;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.engine.common.interfaces.Updatable;
import com.engine.drawables.IDrawable;
import com.engine.drawables.sorting.DrawingPriority;
import com.engine.drawables.sorting.DrawingSection;
import com.engine.drawables.sprites.SpriteMatrix;

public class Background implements Updatable, IDrawable<Batch> {

    protected SpriteMatrix backgroundSprites;

    public Background(float startX, float startY, TextureRegion model, float modelWidth, float modelHeight, int rows,
                      int cols) {
        this(startX, startY, model, modelWidth, modelHeight, rows, cols, new DrawingPriority(DrawingSection.BACKGROUND, 0));
    }

    public Background(float startX, float startY, TextureRegion model, float modelWidth, float modelHeight, int rows,
                      int cols, DrawingPriority priority) {
        backgroundSprites = new SpriteMatrix(model, priority, modelWidth, modelHeight, rows, cols);
        backgroundSprites.setPosition(startX, startY);
    }

    @Override
    public void update(float delta) {
        // optional update method
    }

    @Override
    public void draw(Batch batch) {
        backgroundSprites.draw(batch);
    }
}
