package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
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
    private Vector3f posinit;
    private Spatial tank;
    private int cont = 1;
    private long delayEnemy;
    private ArrayList<Enemy> enemyList = new ArrayList<Enemy>();
    Random rand = new Random();

    public static void main(String[] args) {
        Main app = new Main();
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {

        flyCam.setMoveSpeed(75);
        bulletNode = new Node("BulletNode");
        
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
        long time = System.currentTimeMillis();
        for(Spatial a: bulletNode.getChildren())
        {
            boolean hasCollisionBullet = true;
            a.move(0,0,tpf*30);
             do {
                hasCollisionBullet = hasCollisionBullet(a);
                if (hasCollisionBullet) {
                    
                    
                    bulletNode.detachChild(a);
                    rootNode.detachChild(a);
                    
                    
                }

            } while (hasCollisionBullet);   
        }

        if (time > delayEnemy + 3000) {
            boolean hasCollision = true;

            Enemy enemy = createEnemy(time);
            do {
                hasCollision = hasCollision(enemy);
                if (hasCollision) {
                    enemy.getSpatial().setLocalTranslation(((rand.nextFloat() * 70) - 40), -3, 30);
                }

            } while (hasCollision);

            enemyList.add(enemy);
            enemy.getControl().getAnimationNames();
            rootNode.attachChild(enemy.getSpatial());

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
                System.out.println("Acertou!!!!");
                rootNode.detachChild(e.getSpatial());
                enemyList.remove(e);
                return true;                        
            }
        }
        return false;
    }
    
    
    public Geometry createBullet(){
        Box b = new Box(0.1f, 0.1f, 0.8f);
        Geometry geom = new Geometry("Box", b);
        geom.setLocalTranslation(tank.getLocalTranslation());
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        
        return geom;  
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        //implementar
    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        //unused
    }

    private void initKeys() {
        inputManager.addMapping("Shoot", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Front", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Back", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addListener(actionListener, "Shoot");
        inputManager.addListener(analogListener, "Front");
        inputManager.addListener(analogListener, "Back");
        inputManager.addListener(analogListener, "Right");
        inputManager.addListener(analogListener, "Left");
    }

    /**
     * Use ActionListener to respond to pressed/released inputs (key presses,
     * mouse clicks)
     */
    private ActionListener actionListener = new ActionListener() {

        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("Shoot") && !keyPressed) {
                System.out.println("Atirou");
                bulletNode.attachChild(createBullet());
                rootNode.attachChild(bulletNode);
                    
            }
            
            
        }
    };
    
    private AnalogListener analogListener = new AnalogListener() {
        
        public void onAnalog(String name, float value, float tpf) {
            
            if(name.equals("Front"))
            {   
                tank.move(0, 0, 0.02f);
            }
            
            if(name.equals("Back"))
            {   
                tank.move(0, 0, -0.02f);
            }
            
            if(name.equals("Right"))
            {   
                tank.move(-0.02f, 0, 0);
            }
            
            if(name.equals("Left"))
            {   
                tank.move(0.02f, 0, 0);
            }
        }
    };

}
