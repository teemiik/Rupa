package com.circlesandholes.game.PhysicalBodies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Created by artem.bolgov on 20.11.2017.
 */

public class BoxRectangleBarrier {

    public Body createBoxRectangleBarrier(World world, float sizeWidth, float sizeHeight) {

        Body pBody;
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        //def.position.set(board.getX(), board.getY());

        pBody = world.createBody(def);

        PolygonShape shape_board = new PolygonShape();
        shape_board.setAsBox(sizeWidth, sizeHeight, new Vector2(sizeWidth, sizeHeight), 0);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape_board;
        //fixtureDef.restitution = 0.4f;

        pBody.createFixture(fixtureDef);

        shape_board.dispose();
        return pBody;
    }
}
