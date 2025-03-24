package com.erojas;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture spriteSheet;
    private Animation<TextureRegion>[] animations;
    private float stateTime;

    private Texture background;
    private TextureRegion bgRegion;

    private float posX, posY;
    private float bgX = 0, bgY = 0;
    private float speed = 200;

    private int currentDirection = 0;
    private final int IDLE = -1, UP = 3, DOWN = 0, LEFT = 1, RIGHT = 2;
    private boolean isMoving = false;

    private float spriteWidth, spriteHeight;
    private Rectangle up, down, left, right;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Cargar la spritesheet y dividirla en regiones (4 direcciones, 4 frames cada una)
        spriteSheet = new Texture(Gdx.files.internal("sprite.png"));
        TextureRegion[][] tmpFrames = TextureRegion.split(spriteSheet, spriteSheet.getWidth() / 4, spriteSheet.getHeight() / 4);

        // Crear animaciones para cada dirección
        animations = new Animation[4];
        for (int i = 0; i < 4; i++) {
            animations[i] = new Animation<>(0.1f, tmpFrames[i]);
        }

        stateTime = 0f;

        // Hacer el personaje más grande
        spriteWidth = spriteSheet.getWidth() / 4f * 1.5f;
        spriteHeight = spriteSheet.getHeight() / 4f * 1.5f;

        // Posición inicial en el centro de la pantalla
        posX = (Gdx.graphics.getWidth() - spriteWidth) / 2;
        posY = (Gdx.graphics.getHeight() - spriteHeight) / 2;

        // Cargar el fondo y configurarlo para que se repita
        background = new Texture(Gdx.files.internal("background.png"));
        background.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);
        bgRegion = new TextureRegion(background);

        // Definir regiones de la pantalla para el joystick virtual
        up = new Rectangle(0, Gdx.graphics.getHeight() * 2 / 3f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 3f);
        down = new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 3f);
        left = new Rectangle(0, 0, Gdx.graphics.getWidth() / 3f, Gdx.graphics.getHeight());
        right = new Rectangle(Gdx.graphics.getWidth() * 2 / 3f, 0, Gdx.graphics.getWidth() / 3f, Gdx.graphics.getHeight());
    }

    @Override
    public void render() {
        stateTime += Gdx.graphics.getDeltaTime();

        // Obtener la dirección según el joystick virtual
        int direction = virtualJoystickControl();
        isMoving = (direction != IDLE);

        if (isMoving) {
            currentDirection = direction;

            // Actualizar posición del background para simular movimiento
            if (currentDirection == DOWN) bgY += speed * Gdx.graphics.getDeltaTime();
            if (currentDirection == RIGHT) bgX += speed * Gdx.graphics.getDeltaTime();
            if (currentDirection == LEFT) bgX -= speed * Gdx.graphics.getDeltaTime();
            if (currentDirection == UP) bgY -= speed * Gdx.graphics.getDeltaTime();
        }

        // Evitar que el personaje salga del mapa
        float maxX = Gdx.graphics.getWidth() - spriteWidth;
        float maxY = Gdx.graphics.getHeight() - spriteHeight;

        posX = Math.max(0, Math.min(posX, maxX));
        posY = Math.max(0, Math.min(posY, maxY));

        // Configurar la región del fondo para simular movimiento
        bgRegion.setRegion((int) bgX, (int) bgY, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Obtener el frame actual según la dirección
        TextureRegion currentFrame;
        if (isMoving) {
            currentFrame = animations[currentDirection].getKeyFrame(stateTime, true);
        } else {
            currentFrame = animations[currentDirection].getKeyFrame(0); // Frame estático si no se mueve
        }

        // Dibujar
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(bgRegion, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(currentFrame, posX, posY, spriteWidth, spriteHeight);
        batch.end();
    }

    private int virtualJoystickControl() {
        for (int i = 0; i < 10; i++) { // Soporte multitouch
            if (Gdx.input.isTouched(i)) {
                Vector3 touchPos = new Vector3(Gdx.input.getX(i), Gdx.input.getY(i), 0);
                touchPos.y = Gdx.graphics.getHeight() - touchPos.y; // Ajuste por coordenadas de libGDX

                if (up.contains(touchPos.x, touchPos.y)) return UP;
                if (down.contains(touchPos.x, touchPos.y)) return DOWN;
                if (left.contains(touchPos.x, touchPos.y)) return LEFT;
                if (right.contains(touchPos.x, touchPos.y)) return RIGHT;
            }
        }
        return IDLE; // Si no se toca ninguna zona, el personaje no se mueve
    }

    @Override
    public void dispose() {
        batch.dispose();
        spriteSheet.dispose();
        background.dispose();
    }
}
