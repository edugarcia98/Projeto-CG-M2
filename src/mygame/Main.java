package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.app.SimpleApplication;
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
    Random rand = new Random();
    private boolean fimJogo = false;
    private int score = 0;
    private BitmapText text;
    private BitmapText finalScore;
    private BitmapText scoreText;

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

        /**
         * A white, directional light source
         */
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        initKeys();

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
    if(!fimJogo){
        guiNode.detachAllChildren();
        
        scoreText = new BitmapText(guiFont, false);
        scoreText.setSize(guiFont.getCharSet().getRenderedSize());
        scoreText.setLocalTranslation(10, text.getLineHeight()*22.5f, 0);
        scoreText.setText("Score: " + score);
        guiNode.attachChild(scoreText);

        
        
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

        if (time > delayEnemy + 4000) {
            boolean hasCollision = true;

            Enemy enemy = createEnemy(time);
            do {
                hasCollision = hasCollision(enemy);
                if (hasCollision) {
                    enemy.getSpatial().setLocalTranslation(((rand.nextFloat() * 70) - 40), -3, 30);
                }

            } while (hasCollision);

            enemyList.add(enemy);
            rootNode.attachChild(enemy.getSpatial());
        }

        for (Enemy e : enemyList) {
            e.getSpatial().lookAt(tank.getLocalTranslation(), upVector);
            if (time > e.getDelayEnemyBullet() + ((rand.nextInt(5) + 2) * 1000)) {
                EnemyBullet eb = createEnemyBullet(e, time);
                enemyBulletList.add(eb);
                enemyBulletNode.attachChild(eb.getGeom());
                rootNode.attachChild(enemyBulletNode);
            }
        }

        if (time > delayMediumSpecial + ((rand.nextInt(20) + 10) * 1000)) {
            Special mediumSpecial = createSpecial(time, ColorRGBA.Green);

            rootNode.attachChild(mediumSpecial.getGeom());
            specials.add(mediumSpecial);
        }
        
        if (time > delayStrongSpecial + ((rand.nextInt(60) + 30) * 1000)) {
            Special strongSpecial = createSpecial(time, ColorRGBA.Yellow);

            rootNode.attachChild(strongSpecial.getGeom());
            specials.add(strongSpecial);
        }
        
        for(Special s: specials)
        {
            s.getGeom().move((rand.nextInt(25) - 10)*tpf, 0, (rand.nextInt(25) - 10)*tpf);
            if(hasCollisionSpecial(s.getGeom()))
            {
                System.out.println("ANTES DA MUDANÇA DO ESPECIAL");
                System.out.println("VERDE:" + greenAmmo);
                System.out.println("AMARELO:" + yellowAmmo);
                if(s.getColorName().equals("Green")){
                    greenAmmo = 20;
                    bulletColor = ColorRGBA.Green;
                    
                }
                    
                else if(s.getColorName().equals("Yellow")){
                    yellowAmmo = 10;         
                    bulletColor = ColorRGBA.Yellow;
                }
                rootNode.detachChild(s.getGeom());
                specialsOut.add(s);
            }
            if((greenAmmo == 0) && (yellowAmmo == 0))
                bulletColor = ColorRGBA.Blue;
            
        }
        eliminateSpecials();
        anterior = tank.getLocalTranslation();
               
        }
    else{
        for (Spatial a : bulletNode.getChildren()) {
          
                bulletNode.detachChild(a);// tirando as balas
                rootNode.detachChild(a);
     
            }
            for(Enemy e : enemyList){
                    rootNode.detachChild(e.getSpatial()); // tirando os enemy   
            }
            for(Special s : specials)
            {
                rootNode.detachChild(s.getGeom());        
            }
            specials.removeAll(specials);
            enemyList.removeAll(enemyList);
               
                text = new BitmapText(guiFont, false);
                text.setSize(guiFont.getCharSet().getRenderedSize());
                text.setLocalTranslation(230, text.getLineHeight()*17, 0);
                text.setText("FIM DE JOGO");
                guiNode.attachChild(text);
                
                finalScore = new BitmapText(guiFont, false);
                finalScore.setSize(guiFont.getCharSet().getRenderedSize());
                finalScore.setLocalTranslation(200, text.getLineHeight()*15, 0);
                finalScore.setText("SCORE " + score);
                guiNode.attachChild(finalScore);
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
        tank.scale(0.35f);
        tank.setName(name);

        return tank;
    }

    public Enemy createEnemy(long timeEnemy) {
        delayEnemy = timeEnemy;

        Enemy enemy = new Enemy();
        Spatial s = assetManager.loadModel("Models/Tank/tank.j3o");
        s.scale(0.35f);

        s.setLocalTranslation(((rand.nextFloat() * 70) - 40), -3, 30);
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
                    rootNode.detachChild(e.getSpatial());
                    enemyList.remove(e);
                    score++;
                    return true;
                }
            }
        
        return false;
    }

    public boolean hasCollisionEnemyBullet(EnemyBullet eb) {
        CollisionResults results = new CollisionResults();

        eb.getGeom().collideWith(tank.getWorldBound(), results);

        if (results.size() > 0) {
            fimJogo = true;
            return true;
        }

        return false;
    }
     
    public Geometry createBullet(ColorRGBA color) {

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
        return geom;
    }

    public Special createSpecial(long time, ColorRGBA color) {     
        if(color == ColorRGBA.Green)
            delayMediumSpecial = time;
        else
            delayStrongSpecial = time;

        Special s = new Special();
        
        Sphere sphere = new Sphere(30, 30, 0.5f);
        Geometry geom = new Geometry("MediumSpecial", sphere);
        geom.setLocalTranslation(((rand.nextFloat() * 70) - 40), -3, (rand.nextInt(14) + 7));
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        geom.setMaterial(mat);
        
        s.setGeom(geom);
        if(color == ColorRGBA.Green)
            s.setColorName("Green");
        else
            s.setColorName("Yellow");
        
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
    
    public void eliminateSpecials()
    {
        for(Special s: specialsOut)
        {
            specials.remove(s);
        }
        
        specialsOut.clear();
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
        /*inputManager.addMapping("ChangeBlue", new KeyTrigger(KeyInput.KEY_C));
        inputManager.addMapping("ChangeGreen", new KeyTrigger(KeyInput.KEY_V));
        inputManager.addMapping("ChangeYellow", new KeyTrigger(KeyInput.KEY_B));*/
        inputManager.addListener(actionListener, "Shoot");
        inputManager.addListener(analogListener, "Front");
        inputManager.addListener(analogListener, "Back");
        inputManager.addListener(analogListener, "Right");
        inputManager.addListener(analogListener, "Left");
        inputManager.addListener(actionListener, "Reiniciar");
        
        
        /*inputManager.addListener(actionListener, "ChangeBlue");
        inputManager.addListener(actionListener, "ChangeGreen");
        inputManager.addListener(actionListener, "ChangeYellow");*/
    }

    /**
     * Use ActionListener to respond to pressed/released inputs (key presses,
     * mouse clicks)
     */
    private ActionListener actionListener = new ActionListener() {

        public void onAction(String name, boolean keyPressed, float tpf) {
            
            if((bulletColor == ColorRGBA.Blue) 
                || (bulletColor == ColorRGBA.Green && greenAmmo > 0)
                || (bulletColor == ColorRGBA.Yellow && yellowAmmo > 0))
            {
                if (name.equals("Shoot") && !keyPressed) {
                    bulletNode.attachChild(createBullet(bulletColor));
                    rootNode.attachChild(bulletNode);
                    System.out.println("ATIRANDO:");
                    System.out.println("VERDE:" + greenAmmo);
                    System.out.println("AMARELO:" + yellowAmmo);

                    if(bulletColor == ColorRGBA.Green)
                        greenAmmo--;
                    else if(bulletColor == ColorRGBA.Yellow)
                        yellowAmmo--;
                }
                //Printar na tela a munição de cada bala que possui
            }
            if(name.equals("Reiniciar") && !keyPressed)
            {
                fimJogo = false;
                score = 0;
                
                
            }
            
            /*if (name.equals("ChangeBlue") && !keyPressed)
                bulletColor = ColorRGBA.Blue;
            
            if (name.equals("ChangeGreen") && !keyPressed)
                bulletColor = ColorRGBA.Green;
            
            if (name.equals("ChangeYellow") && !keyPressed)
                bulletColor = ColorRGBA.Yellow;*/
        }
    };

    private AnalogListener analogListener = new AnalogListener() {

        public void onAnalog(String name, float value, float tpf) {
        
            if(!fimJogo){

                        if (name.equals("Front")) {
                            tank.move(0, 0, 0.02f);
                        }

                        if (name.equals("Back")) {
                            tank.move(0, 0, -0.02f);
                        }

                        if (name.equals("Right")) {
                            tank.move(-0.02f, 0, 0);
                        }

                        if (name.equals("Left")) {
                            tank.move(0.02f, 0, 0);
                        }
            }
        }
    };

}
