package org.shootemup.engine;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.shootemup.GameLib;
import org.shootemup.components.Background;
import org.shootemup.components.Explosion;
import org.shootemup.components.Vector2D;
import org.shootemup.entities.Enemy;
import org.shootemup.entities.Player;
import org.shootemup.entities.Projectile;
import org.shootemup.utils.Direction;

/// Classe que representa o sistema de jogo
public class Game {
    private long currentTime = System.currentTimeMillis();
    private long delta;

    private boolean isRunning = false;

    private Background farStarBackground;
    private Background nearStarBackground;

    private List<Projectile> projectiles;
    private List<Enemy> enemies;
    private List<Explosion> explosions;

    private long nextCommonSpawn = currentTime + 2000;
    private long nextAdvancedSpawn = currentTime + 7000;
    private double advancedSpawnX = GameLib.WIDTH * 0.20;
    private int advancedFormationCount = 0;

    private Player player;


    public Game() {
        nearStarBackground = Background.forStars(Color.GRAY, 20, 3, 0.070);
        farStarBackground = Background.forStars(Color.DARK_GRAY, 20, 2, 0.045);

        player = new Player(
            new Vector2D(GameLib.WIDTH / 2, GameLib.HEIGHT / 2),
            12.0,
            Vector2D.ofScalar(0.25)
        );

        projectiles = new ArrayList<>(200); // Increased capacity for multiple projectiles
        enemies = new ArrayList<>(20);      // Increased capacity for multiple enemy types
        explosions = new ArrayList<>(15);
    }

    /// Lê a entrada do usuário e reage
    private void read_input() {
        if(GameLib.iskeyPressed(GameLib.KEY_UP)) player.move(delta, Direction.NORTH);
        if(GameLib.iskeyPressed(GameLib.KEY_DOWN)) player.move(delta, Direction.SOUTH);
        if(GameLib.iskeyPressed(GameLib.KEY_LEFT)) player.move(delta, Direction.WEST);
        if(GameLib.iskeyPressed(GameLib.KEY_RIGHT)) player.move(delta, Direction.EAST);

        if (GameLib.iskeyPressed(GameLib.KEY_CONTROL)) {
            // Tenta atirar se for um sucesso adiciona projétil a lista de projeteis
            player.shot(currentTime).ifPresent((bullet) -> projectiles.add(bullet));
        }

        if(GameLib.iskeyPressed(GameLib.KEY_ESCAPE)) isRunning = false;
    }

    private void update() {
        // try to revive the player
        player.revive(currentTime);

        /* Colisões */

        // colisoes entre player e projeteis do tipo Ball
        projectiles.stream()
            .filter(proj -> proj instanceof Projectile.Ball)
            .forEach(proj -> {
                if (player.intersects(proj)) {
                    player.dieForDuration(currentTime, 2000);
                    explosions.add(new Explosion(player.getPosition(), currentTime, 2000));
                }
            });

        // colisão entre player e enemy
        enemies.forEach(enemy -> {
           if (player.intersects(enemy)) {
               player.dieForDuration(currentTime, 2000);
               explosions.add(new Explosion(player.getPosition(), currentTime, 2000));
           }
        });

        // checa se cada inimigo colidiu com um projetil do player
        projectiles.stream()
            .filter(proj -> proj instanceof Projectile.Bullet)
            .forEach(bullet -> {
                enemies.removeIf(enemy -> {
                    if (enemy.intersects(bullet)) {
                        explosions.add(new Explosion(enemy.getPosition(), currentTime, 500));
                        return true;
                    }
                    return false;
                });
            });

        /* Movimenta/ Atualiza entidades */

        // Movimenta os planos de fundo
        nearStarBackground.animate(delta);
        farStarBackground.animate(delta);

        // Atualiza o estado das explosões e remove as que completaram
        explosions.removeIf(expl -> {expl.update(currentTime); return expl.isFinished();});

        // Atualiza a posicao dos projeteis
        // Remove projéteis fora da tela
        projectiles.removeIf(proj -> {
            proj.move(delta);
            Vector2D pos = proj.getPosition();
            return pos.getX() < 0 || pos.getX() > GameLib.WIDTH || pos.getY() < 0 || pos.getY() > GameLib.HEIGHT;
        });

        // Movimenta e remove inimigos fora da tela
        enemies.removeIf(enemy -> {
            enemy.move(delta);
            Vector2D pos = enemy.getPosition();
            return pos.getY() > GameLib.HEIGHT + 10;
        });
        // Inimigos fazem uma tentativa de tiro
        enemies.forEach(e -> {
            // Se inimigo for do tipo flyer atira com multiShot
            if (e instanceof Enemy.Flyer) {
                Enemy.Flyer advancedEnemy = (Enemy.Flyer) e;
                List<Projectile> multiShot = advancedEnemy.shotMultiple(currentTime);
                projectiles.addAll(multiShot);
            } else {
                e.shot(currentTime).ifPresent(bullet -> projectiles.add(bullet));
            }
        });

        /* Spawnar inimigos */

        if (currentTime > nextCommonSpawn) {
            enemies.add(new Enemy.Common(
                new Vector2D(Math.random() * (GameLib.WIDTH - 20) + 10, -10.0)
            ));
            nextCommonSpawn = currentTime + 500;
        }

        /* Spawnar inimigos avançados (tipo 2) */

        if (currentTime > nextAdvancedSpawn) {
            boolean spawnOnRight = advancedSpawnX > GameLib.WIDTH / 2;
            enemies.add(new Enemy.Flyer(
                new Vector2D(advancedSpawnX, -10.0),
                spawnOnRight
            ));

            advancedFormationCount++;

            if (advancedFormationCount < 10) {
                nextAdvancedSpawn = currentTime + 120;
            } else {
                advancedFormationCount = 0;
                advancedSpawnX = Math.random() > 0.5 ? GameLib.WIDTH * 0.2 : GameLib.WIDTH * 0.8;
                nextAdvancedSpawn = currentTime + 3000 + (long)(Math.random() * 3000);
            }
        }
    }

    private void render() {

        // Renderiza cada background
        farStarBackground.render();
        nearStarBackground.render();

        // Renderiza os projéteis
        projectiles.forEach((b)->b.render());
        // Renderiza inimigos
        enemies.forEach((e)->e.render());
        // Renderiza explosoes
        explosions.forEach(expl -> expl.render());

        // Renderiza o player
        player.render();

        GameLib.display();
    }

    // Executa o jogo
	public void run() {
   	    // inicializa a biblioteca gráfica
       	GameLib.initGraphics();
        //GameLib.initGraphics_SAFE_MODE();  // chame esta versão do método caso nada seja desenhado na janela do jogo.

        isRunning = true;

        while (isRunning) {
            delta = System.currentTimeMillis() - currentTime;
            currentTime = System.currentTimeMillis();

            update();
            read_input();
            render();

            // Deixa a thread em _idle_ para normalizar o frame rate
            busyWait(currentTime + 3);
        }
   	    System.exit(0);
	}

	/// Mantem a thread em estado de espera
	private static void busyWait(long time){
		while(System.currentTimeMillis() < time) Thread.yield();
	}
}
