package com.circlesandholes.game.PhysicalBodies;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import static com.circlesandholes.game.Intro.w_world;

/**
 * Created by bolgov.artem on 05.10.17.
 */

public class BoxHole {

    public Body createBoxHole(World world) {
        Body pBody;
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        //def.position.set(board.getX(), board.getY());

        pBody = world.createBody(def);

        CircleShape shape_circle = new CircleShape();
        shape_circle.setRadius((float) (w_world * 0.01));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape_circle;
        fixtureDef.density = 1;
        fixtureDef.friction = 10;
        fixtureDef.restitution = 0.4f;

        pBody.createFixture(shape_circle, 0.0f);

        shape_circle.dispose();
        return pBody;
    }

}
