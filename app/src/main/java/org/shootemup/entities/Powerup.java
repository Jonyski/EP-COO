package org.shootemup.entities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.shootemup.GameLib;
import org.shootemup.components.Vector2D;
import org.shootemup.components.Weapon;
import org.shootemup.utils.Shooter;


public abstract class Powerup extends Entity {
    protected Powerup(Color color, Vector2D pos, double radius, Vector2D velocity) {
           super(pos, velocity, radius, color);
    }

    public static class ZaWarudo extends Powerup {
        public final static long duration = 4000;

        public ZaWarudo(Vector2D pos) {
           super(Color.YELLOW, pos, 8.0, new Vector2D(0.0, 0.08 + Math.random() * 0.07));
        }

        public static void renderEffect(Vector2D position, long timeLeft, Color color) {
            if(timeLeft > 3600) {
                long r = (long)((4000 - timeLeft) * 1.6);
                GameLib.setColor(color);
                GameLib.drawCircle(position.x, position.y, r);
                r = r - 50 < 0 ? 0 : r - 50;
                GameLib.setColor(Color.LIGHT_GRAY);
                GameLib.drawCircle(position.x, position.y, r);
                r = r - 50 < 0 ? 0 : r - 50;
                GameLib.setColor(Color.DARK_GRAY);
                GameLib.drawCircle(position.x, position.y, r);
            }
            if(timeLeft < 500) {
                double r = (timeLeft * 0.75) - 100;
                GameLib.setColor(color);
                if(r >= 0) GameLib.drawCircle(position.x, position.y, r);
                r = r + 50 < 0 ? 0 : r + 50;
                GameLib.setColor(Color.LIGHT_GRAY);
                if(r >= 0) GameLib.drawCircle(position.x, position.y, r);
                r = r + 50 < 0 ? 0 : r + 50;
                GameLib.setColor(Color.DARK_GRAY);
                if(r >= 0) GameLib.drawCircle(position.x, position.y, r);
            }
        }
    }

    public static class LaserMode extends Powerup {
        public final static long duration = 8000;

        public LaserMode(Vector2D pos) {
            super(Color.ORANGE, pos, 8.0, new Vector2D(0.0, 0.08 + Math.random() * 0.07));
        }

        public static void renderEffect(Vector2D position, long timeLeft) {
            System.out.println(timeLeft / duration);
            double x1 = position.x - ((double)timeLeft / (double)duration) * 24;
            double x2 = position.x + ((double)timeLeft / (double)duration) * 24;
            double y = position.y - 15;
            GameLib.setColor(Color.RED);
            GameLib.drawLine(x1, y, x2, y);
            x1 = position.x - ((double)timeLeft / (double)duration) * 16;
            x2 = position.x + ((double)timeLeft / (double)duration) * 16;
            y -= 6;
            GameLib.drawLine(x1, y, x2, y);
            y -= 4;
            GameLib.drawCircle(position.x, y, 4);
        }
    }

    @Override
    public void render() {
        GameLib.setColor(color);
        GameLib.drawDiamond(position.getX(), position.getY(), radius);
    }
}
