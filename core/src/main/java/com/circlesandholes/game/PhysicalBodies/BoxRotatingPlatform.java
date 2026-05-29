package com.circlesandholes.game.PhysicalBodies;

import com.badlogic.gdx.physics.box2d.*;

public class BoxRotatingPlatform {

    public Body create(World world, float halfW, float halfH) {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.KinematicBody;

        Body body = world.createBody(def);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(halfW, halfH);

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.restitution = 0.4f;
        fd.friction = 0.6f;

        body.createFixture(fd);
        shape.dispose();

        return body;
    }
}
