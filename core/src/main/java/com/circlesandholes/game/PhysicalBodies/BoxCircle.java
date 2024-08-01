package com.circlesandholes.game.PhysicalBodies;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import static com.circlesandholes.game.Intro.circle_image;

/**
 * Created by bolgov.artem on 05.10.17.
 */

public class BoxCircle {

    public static Fixture fixture_circle;

    public Body createBoxCircle(World world, Sprite board) {
        Body pBody;
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(board.getX() * 4, board.getY() + board.getY());

        pBody = world.createBody(def);

        CircleShape shape_circle = new CircleShape();
        shape_circle.setRadius((float) (circle_image.getTextureData().getHeight() / 2));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape_circle;
        fixtureDef.density = 1f;
        fixtureDef.friction = 10f;
        fixtureDef.restitution = 0.4f;

        fixture_circle = pBody.createFixture(fixtureDef);

        shape_circle.dispose();
        return pBody;
    }
}
