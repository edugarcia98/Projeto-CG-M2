package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Triangle;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import static java.awt.SystemColor.text;
import java.util.ArrayList;
import java.util.Random;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication implements AnimEventListener {

    private CameraNode camNode;
    private Node bulletNode;
    private Node enemyBulletNode;
    private Vector3f posinit;
    private Spatial tank;
    private int cont = 1;
    private int life = 100;
    private long delayEnemy;
    private long delayMediumSpecial;
    private long delayStrongSpecial;
    private ColorRGBA bulletColor;
    private AnimChannel channel;
    private AnimControl control;
    private ArrayList<Enemy> enemyList = new ArrayList<Enemy>();
    private ArrayList<EnemyBullet> enemyBulletList = new ArrayList<EnemyBullet>();
    private ArrayList<EnemyBullet> enemyBulletListOut = new ArrayList<EnemyBullet>();
    private ArrayList<Special> specials = new ArrayList<Special>();
    private ArrayList<Special> specialsOut = new ArrayList<Special>();
    private Vector3f upVector = new Vector3f(0, 1, 0);
    private Vector3f anterior = new Vector3f();
    private int greenAmmo = 0;
    private int yellowAmmo = 0;
    private int enemiesOnScreen;
    Random rand = new Random();
    private boolean fimJogo = false;
    private boolean gamePaused = false;
    private int score = 0;
    private int recordScore = 0;
    private BitmapText text;
    private BitmapText finalScore;
    private BitmapText scoreText;
    private BitmapText lifeText;
    private BitmapText blueAmmoText;
    private BitmapText greenAmmoText;
    private BitmapText yellowAmmoText;
    private BitmapText pauseText;
    private BitmapText restartText;
    private BitmapText recordText;
    private AudioNode  audio;
    private AudioNode  bala;
    
    public static void main(String[] args) {
        Main app = new Main();
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(75);
        bulletNode = new Node("BulletNode");
        enemyBulletNode = new Node("EnemyBulletNode");
        bulletColor = ColorRGBA.Blue;
        enemiesOnScreen = 0;

        /**
         * A white, directional light source
         */
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        initKeys();
        showInfoOnScreen();
        initaudio();

        tank = createTank("MyTank");
        tank.setLocalTranslation(0, -3, 0);
        rootNode.attachChild(tank);

        camNode = new CameraNode("CamNode", cam);
        //camNode.setControlDir(ControlDirection.SpatialToCamera);
        rootNode.attachChild(camNode);

        Vector3f pos = tank.getLocalTranslation().clone();

        posinit = tank.getLocalTranslation().clone();
        posinit.y += 40;

        pos.z -= 3;
        pos.y += 50;

        camNode.setLocalTranslation(pos);
        camNode.lookAt(posinit, Vector3f.UNIT_Z);
    }

    @Override
    public void simpleUpdate(float tpf) {

        //TODO: add update code
        //tank.move(0,0,tpf);
        //camNode.lookAt(posinit, Vector3f.UNIT_Z);
        if (!gamePaused) {
            if (!fimJogo) {

                long time = System.currentTimeMillis();
                for (Spatial a : bulletNode.getChildren()) {
                    boolean hasCollisionBullet = true;
                    a.move(0, 0, tpf * 40);

                    if (hasCollisionBullet(a)) {

                        bulletNode.detachChild(a);
                        rootNode.detachChild(a);
                    }

                    if (a.getLocalTranslation().getZ() >= 41.5) {
                        bulletNode.detachChild(a);
                        rootNode.detachChild(a);
                    }
                }

                for (EnemyBullet eb : enemyBulletList) {
                    eb.getGeom().move(eb.getEnemy().getSpatial().getLocalRotation().getW() * 75 * tpf, 0, -tpf * 30);

                    if (eb.getGeom().getLocalTranslation().getZ() <= -14.5f) {
                        enemyBulletNode.detachChild(eb.getGeom());
                        rootNode.detachChild(eb.getGeom());
                        enemyBulletListOut.add(eb);
                    }

                    if (hasCollisionEnemyBullet(eb)) {
                        enemyBulletNode.detachChild(eb.getGeom());
                        rootNode.detachChild(eb.getGeom());
                        enemyBulletListOut.add(eb);
                    }
                }

                eliminateEnemyBullets();

                if (time > delayEnemy + ((rand.nextInt(5) + 2) * 1000)) {
                    boolean hasCollision = true;
                    
                    if (enemiesOnScreen < 6)
                    {
                        Enemy enemy = createEnemy(time);
                        do {
                            hasCollision = hasCollision(enemy);
                            if (hasCollision) {
                                enemy.getSpatial().setLocalTranslation(rand.nextInt(52) - 26, -3, 30);
                            }

                        } while (hasCollision);

                        enemyList.add(enemy);
                        rootNode.attachChild(enemy.getSpatial());
                        enemiesOnScreen++;
                    }
                }

                for (Enemy e : enemyList) {
                    e.getSpatial().lookAt(tank.getLocalTranslation(), upVector);
                    if (time > e.getDelayEnemyBullet() + ((rand.nextInt(100) + 2) * 1000)) {
                        EnemyBullet eb = createEnemyBullet(e, time);
                        enemyBulletList.add(eb);
                        enemyBulletNode.attachChild(eb.getGeom());
                        rootNode.attachChild(enemyBulletNode);
                    }
                }

                if (time > delayMediumSpecial + ((rand.nextInt(20) + 30) * 1000)) {
                    Special mediumSpecial = createSpecial(time, ColorRGBA.Green);

                    rootNode.attachChild(mediumSpecial.getGeom());
                    specials.add(mediumSpecial);
                }

                if (time > delayStrongSpecial + ((rand.nextInt(60) + 60) * 1000)) {
                    Special strongSpecial = createSpecial(time, ColorRGBA.Yellow);

                    rootNode.attachChild(strongSpecial.getGeom());
                    specials.add(strongSpecial);
                }

                for (Special s : specials) {
                    s.getGeom().move((rand.nextInt(25) - 10) * tpf, 0, (rand.nextInt(25) - 10) * tpf);
                    if (hasCollisionSpecial(s.getGeom())) {
                        if (s.getColorName().equals("Green")) {
                            greenAmmo += 20;
                            bulletColor = ColorRGBA.Green;

                        } else if (s.getColorName().equals("Yellow")) {
                            yellowAmmo += 10;
                            bulletColor = ColorRGBA.Yellow;
                        }
                        rootNode.detachChild(s.getGeom());
                        specialsOut.add(s);
                        showInfoOnScreen();
                    }
                    if ((greenAmmo == 0) && (yellowAmmo == 0)) {
                        bulletColor = ColorRGBA.Blue;
                    }

                }
                eliminateSpecials();
                anterior = tank.getLocalTranslation();

            } else {
                for (Spatial a : bulletNode.getChildren()) {

                    bulletNode.detachChild(a);// tirando as balas
                    rootNode.detachChild(a);

                }

                for (EnemyBullet eb : enemyBulletList) {
                    enemyBulletNode.detachChild(eb.getGeom());
                    rootNode.detachChild(eb.getGeom());
                    enemyBulletListOut.add(eb);
                }

                eliminateEnemyBullets();

                for (Enemy e : enemyList) {
                    rootNode.detachChild(e.getSpatial()); // tirando os enemy   
                }
                for (Special s : specials) {
                    rootNode.detachChild(s.getGeom());
                }
                specials.removeAll(specials);
                enemyList.removeAll(enemyList);
            }
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    public Spatial createTank(String name) {
        /**
         * Load a model. Uses model and texture from jme3-test-data library!
         */
        Spatial tank = assetManager.loadModel("Models/Tank/tank.j3o");

        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture monkeyTex = assetManager.loadTexture("mygame/mengao.jpg");
        boxMat.setTexture("ColorMap", monkeyTex);
        tank.setMaterial(boxMat);

        tank.scale(0.25f);
        tank.setName(name);

        return tank;
    }

    public Enemy createEnemy(long timeEnemy) {
        delayEnemy = timeEnemy;

        Enemy enemy = new Enemy();
        Spatial s = assetManager.loadModel("Models/Tank/tank.j3o");
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture monkeyTex = assetManager.loadTexture("mygame/vasco.jpg");
        boxMat.setTexture("ColorMap", monkeyTex);
        s.setMaterial(boxMat);

        s.scale(0.25f);

        //s.setLocalTranslation(((rand.nextFloat() * 140) - 70), -3, 30);
        s.setLocalTranslation(rand.nextInt(52) - 26, -3, 30);
        s.rotate(0, FastMath.PI, 0);
        s.setName(Integer.toString(cont));
        cont++;

        enemy.setSpatial(s);
        enemy.setName(s.getName());

        return enemy;
    }

    public boolean hasCollision(Enemy enemy) {
        CollisionResults results = new CollisionResults();
        for (Enemy e : enemyList) {
            enemy.getSpatial().collideWith(e.getSpatial().getWorldBound(), results);

            if (results.size() > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCollisionBullet(Spatial geo) {
        CollisionResults results = new CollisionResults();

        for (Enemy e : enemyList) {
            geo.collideWith(e.getSpatial().getWorldBound(), results);

            if (results.size() > 0) {
                if (geo.getName().equals("BlueBullet")) {
                    e.setLife(e.getLife() - 25);
                } else if (geo.getName().equals("GreenBullet")) {
                    e.setLife(e.getLife() - 50);
                } else if (geo.getName().equals("YellowBullet")) {
                    e.setLife(e.getLife() - 100);
                }

                if (e.getLife() <= 0) {
                    rootNode.detachChild(e.getSpatial());
                    enemyList.remove(e);
                    score++;
                    enemiesOnScreen--;
                    showInfoOnScreen();
                }

                return true;
            }
        }

        return false;
    }
    private void initaudio()
    {
        audio = new AudioNode(assetManager,"mygame/mengaofunk.wav",AudioData.DataType.Buffer);
        audio.setLooping(true);
        audio.setPositional(false);
        audio.setVolume(0.025f);
        audio.play();
        
        bala = new AudioNode(assetManager,"mygame/porra.wav",AudioData.DataType.Buffer);
        bala.setLooping(false);
        bala.setPositional(false);
        bala.setVolume(0.5f);
       
    }

    public boolean hasCollisionEnemyBullet(EnemyBullet eb) {
        CollisionResults results = new CollisionResults();

        eb.getGeom().collideWith(tank.getWorldBound(), results);

        if (results.size() > 0) {
            life -= 10;

            showInfoOnScreen();

            if (life <= 0) {
                fimJogo = true;
                if (score > recordScore)
                    recordScore = score;
                showInfoOnScreen();
            }

            return true;
        }

        return false;
    }

    public Geometry createBullet(ColorRGBA color, String name) {

        //Blue: Bala Fraca - Dano 25
        //Green: Bala Média - Dano 50
        //Yellow: Bala Forte - Dano 100
        //Implementar o dano de cada bala
        Box b = new Box(0.1f, 0.1f, 0.8f);
        Geometry geom = new Geometry("Box", b);
        geom.setLocalTranslation(tank.getLocalTranslation());
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        geom.setMaterial(mat);
        geom.setName(name);
        return geom;
    }

    public Special createSpecial(long time, ColorRGBA color) {
        if (color == ColorRGBA.Green) {
            delayMediumSpecial = time;
        } else {
            delayStrongSpecial = time;
        }

        Special s = new Special();

        Sphere sphere = new Sphere(30, 30, 0.5f);
        Geometry geom = new Geometry("MediumSpecial", sphere);
        geom.setLocalTranslation(((rand.nextFloat() * 70) - 40), -3, (rand.nextInt(14) + 7));
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        geom.setMaterial(mat);

        s.setGeom(geom);
        if (color == ColorRGBA.Green) {
            s.setColorName("Green");
        } else {
            s.setColorName("Yellow");
        }

        return s;
    }

    public boolean hasCollisionSpecial(Spatial g) {
        CollisionResults results = new CollisionResults();

        tank.collideWith(g.getWorldBound(), results);

        if (results.size() > 0) {
            return true;
        }

        return false;
    }

    public EnemyBullet createEnemyBullet(Enemy e, long timeEnemyBullet) {
        e.setDelayEnemyBullet(timeEnemyBullet);

        EnemyBullet enemyBullet = new EnemyBullet();

        Box b = new Box(0.1f, 0.1f, 0.8f);
        Geometry geom = new Geometry("Box", b);
        geom.setLocalTranslation(e.getSpatial().getLocalTranslation());
        geom.setLocalRotation(e.getSpatial().getLocalRotation());
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Red);
        geom.setMaterial(mat);

        enemyBullet.setName("EnemyBullet");
        enemyBullet.setGeom(geom);
        enemyBullet.setEnemy(e);

        return enemyBullet;
    }

    public void eliminateEnemyBullets() {
        for (EnemyBullet eb : enemyBulletListOut) {
            enemyBulletList.remove(eb);
        }

        enemyBulletListOut.clear();
    }

    public void eliminateSpecials() {
        for (Special s : specialsOut) {
            specials.remove(s);
        }

        specialsOut.clear();
    }

    public void showInfoOnScreen() {
        guiNode.detachAllChildren();
        
        if(!gamePaused)
        {
            if (!fimJogo) {
                scoreText = new BitmapText(guiFont, false);
                scoreText.setSize(guiFont.getCharSet().getRenderedSize());
                scoreText.setLocalTranslation(10, scoreText.getLineHeight() * 22.5f, 0);
                scoreText.setText("Score: " + score);
                guiNode.attachChild(scoreText);

                lifeText = new BitmapText(guiFont, false);
                lifeText.setSize(guiFont.getCharSet().getRenderedSize());
                lifeText.setLocalTranslation(10, lifeText.getLineHeight() * 21f, 0);
                lifeText.setText("Life: " + life + "%");
                guiNode.attachChild(lifeText);

                blueAmmoText = new BitmapText(guiFont, false);
                blueAmmoText.setSize(guiFont.getCharSet().getRenderedSize());
                blueAmmoText.setLocalTranslation(440, blueAmmoText.getLineHeight() * 22.5f, 0);
                blueAmmoText.setText("Municao Azul(C): INFINITA");
                guiNode.attachChild(blueAmmoText);

                greenAmmoText = new BitmapText(guiFont, false);
                greenAmmoText.setSize(guiFont.getCharSet().getRenderedSize());
                greenAmmoText.setLocalTranslation(440, greenAmmoText.getLineHeight() * 21f, 0);
                greenAmmoText.setText("Municao Verde(V): " + greenAmmo);
                guiNode.attachChild(greenAmmoText);

                yellowAmmoText = new BitmapText(guiFont, false);
                yellowAmmoText.setSize(guiFont.getCharSet().getRenderedSize());
                yellowAmmoText.setLocalTranslation(440, greenAmmoText.getLineHeight() * 19.5f, 0);
                yellowAmmoText.setText("Municao Amarela(B): " + yellowAmmo);
                guiNode.attachChild(yellowAmmoText);
            } else {
                text = new BitmapText(guiFont, false);
                text.setSize(guiFont.getCharSet().getRenderedSize());
                text.setLocalTranslation(230, text.getLineHeight() * 17, 0);
                text.setText("FIM DE JOGO");
                guiNode.attachChild(text);

                finalScore = new BitmapText(guiFont, false);
                finalScore.setSize(guiFont.getCharSet().getRenderedSize());
                finalScore.setLocalTranslation(200, finalScore.getLineHeight() * 15, 0);
                finalScore.setText("SCORE: " + score);
                guiNode.attachChild(finalScore);
                
                recordText = new BitmapText(guiFont, false);
                recordText.setSize(guiFont.getCharSet().getRenderedSize());
                recordText.setLocalTranslation(200, recordText.getLineHeight() * 13, 0);
                recordText.setText("SEU RECORDE ATUAL É DE: " + recordScore);
                guiNode.attachChild(recordText);
                
                restartText = new BitmapText(guiFont, false);
                restartText.setSize(guiFont.getCharSet().getRenderedSize());
                restartText.setLocalTranslation(200, restartText.getLineHeight() * 11, 0);
                restartText.setText("PRESSIONE 'R' PARA REINICIAR ");
                guiNode.attachChild(restartText);
            }
        }
        else
        {
            pauseText = new BitmapText(guiFont, false);
            pauseText.setSize(guiFont.getCharSet().getRenderedSize());
            pauseText.setLocalTranslation(290, pauseText.getLineHeight() * 15, 0);
            pauseText.setText("PAUSE");
            guiNode.attachChild(pauseText);
        }
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        //implementar
    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        //unused
    }

    private void initKeys() {
        inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("Front", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Back", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Reiniciar", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping("ChangeBlue", new KeyTrigger(KeyInput.KEY_C));
        inputManager.addMapping("ChangeGreen", new KeyTrigger(KeyInput.KEY_V));
        inputManager.addMapping("ChangeYellow", new KeyTrigger(KeyInput.KEY_B));
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addListener(actionListener, "Shoot");
        inputManager.addListener(analogListener, "Front");
        inputManager.addListener(analogListener, "Back");
        inputManager.addListener(analogListener, "Right");
        inputManager.addListener(analogListener, "Left");
        inputManager.addListener(actionListener, "Reiniciar");
        inputManager.addListener(actionListener, "ChangeBlue");
        inputManager.addListener(actionListener, "ChangeGreen");
        inputManager.addListener(actionListener, "ChangeYellow");
        inputManager.addListener(actionListener, "Pause");
    }

    /**
     * Use ActionListener to respond to pressed/released inputs (key presses,
     * mouse clicks)
     */
    private ActionListener actionListener = new ActionListener() {

        public void onAction(String name, boolean keyPressed, float tpf) {

            if (name.equals("Pause") && !keyPressed) {
                gamePaused = !gamePaused;
                showInfoOnScreen();
            }

            if (!gamePaused) {
                if ((bulletColor == ColorRGBA.Blue)
                        || (bulletColor == ColorRGBA.Green && greenAmmo > 0)
                        || (bulletColor == ColorRGBA.Yellow && yellowAmmo > 0)) {
                    if (name.equals("Shoot") && !keyPressed) {
                        //bala.play();
                        if (bulletColor == ColorRGBA.Blue) {
                            bulletNode.attachChild(createBullet(bulletColor, "BlueBullet"));
                        } else if (bulletColor == ColorRGBA.Green) {
                            bulletNode.attachChild(createBullet(bulletColor, "GreenBullet"));
                            greenAmmo--;
                            showInfoOnScreen();
                        } else if (bulletColor == ColorRGBA.Yellow) {
                            bulletNode.attachChild(createBullet(bulletColor, "YellowBullet"));
                            yellowAmmo--;
                            showInfoOnScreen();
                        }

                        rootNode.attachChild(bulletNode);
                    }
                    //Printar na tela a munição de cada bala que possui
                }
                if (name.equals("Reiniciar") && !keyPressed) {
                    fimJogo = false;
                    score = 0;
                    life = 100;
                    greenAmmo = 0;
                    yellowAmmo = 0;
                    bulletColor = ColorRGBA.Blue;
                    enemiesOnScreen = 0;
                    showInfoOnScreen();
                }

                if (name.equals("ChangeBlue") && !keyPressed) {
                    bulletColor = ColorRGBA.Blue;
                }

                if (name.equals("ChangeGreen") && !keyPressed) {
                    bulletColor = ColorRGBA.Green;
                }

                if (name.equals("ChangeYellow") && !keyPressed) {
                    bulletColor = ColorRGBA.Yellow;
                }
            }
        }
    };

    private AnalogListener analogListener = new AnalogListener() {

        public void onAnalog(String name, float value, float tpf) {

            if (!gamePaused) {
                if (!fimJogo) {

                    if (name.equals("Front") && tank.getLocalTranslation().z < 20.8f) {
                        tank.move(0, 0, 0.03f);
                    }

                    if (name.equals("Back") && tank.getLocalTranslation().z > -5.2f) {
                        tank.move(0, 0, -0.03f);
                    }

                    if (name.equals("Right") && tank.getLocalTranslation().x > -24.3f) {
                        tank.move(-0.03f, 0, 0);
                    }

                    if (name.equals("Left") && tank.getLocalTranslation().x < 23.8f) {
                        tank.move(0.03f, 0, 0);
                    }
                }
            }
        }
    };

}
