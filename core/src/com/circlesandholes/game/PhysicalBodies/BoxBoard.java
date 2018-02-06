package com.circlesandholes.game.PhysicalBodies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import static com.circlesandholes.game.Intro.w_world;

/**
 * Created by bolgov.artem on 05.10.17.
 */

public class BoxBoard {

    public Body createBoxBoard(World world) {
        float widnt = (float) ((w_world - w_world * 0.25) / 2);

        //System.out.println(widnt);

        Body pBody;
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        //def.position.set(board.getX(), board.getY());

        pBody = world.createBody(def);

        PolygonShape shape_board = new PolygonShape();
        shape_board.setAsBox(widnt, (float) (w_world * 0.04 / 2), new Vector2(widnt, (float) (w_world * 0.04 / 2)), 0);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape_board;
        fixtureDef.friction = 10f;
        //fixtureDef.restitution = 0.4f;

        pBody.createFixture(fixtureDef);

        shape_board.dispose();
        return pBody;
    }
}
