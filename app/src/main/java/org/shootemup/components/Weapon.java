package org.shootemup.components;

import java.util.Optional;
import java.util.function.BiFunction;

import org.shootemup.entities.Projectile;

public class Weapon<T extends Projectile> {
    private final BiFunction<Vector2D, Vector2D, T> bulletFactory;
    protected long nextShot;
    protected long reckoilMilis;

    protected Weapon(long reckoil, BiFunction<Vector2D, Vector2D, T> bulletFactoy ) {
        this.reckoilMilis = reckoil;
        this.bulletFactory = bulletFactoy;
    }

    public static Weapon<Projectile.Bullet> Pistol() {
        return new Weapon<>(100, Projectile.Bullet::new);
    }

    public static Weapon<Projectile.Ball> Cannon() {
        return new Weapon<>(500, Projectile.Ball::new);
    }

    public static Weapon<Projectile.Ball> TripleCannon() {
        return new Weapon<>(1000, Projectile.Ball::new);
    }

    public Optional<T> fire(long currentTime, Vector2D pos, Vector2D velocity) {
        if (currentTime > nextShot) {
            T bullet = bulletFactory.apply(pos, velocity);
            nextShot = currentTime + reckoilMilis;
            return Optional.of(bullet);
        }
        return Optional.empty();
    }

    public long getNextShot() {
        return nextShot;
    }

    public void setNextShot(long nextShotMilis) {
        nextShot = nextShotMilis;
    }

    public long getReckoilMilis() {
        return reckoilMilis;
    }

}
