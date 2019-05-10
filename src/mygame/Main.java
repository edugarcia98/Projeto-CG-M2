package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
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
public class Main extends SimpleApplication {
    
    private CameraNode camNode;
    private Vector3f posinit;
    private Spatial tank;
    private int cont = 1;
    private long delayEnemy;
    private ArrayList<Enemy> enemyList = new ArrayList();
    
    public static void main(String[] args) {
        Main app = new Main();
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {

        flyCam.setMoveSpeed(75);

        /**
         * A white, directional light source
         */
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        
        tank = createTank("MyTank");
        rootNode.attachChild(tank);
        
        camNode = new CameraNode("CamNode", cam);
        //camNode.setControlDir(ControlDirection.SpatialToCamera);
        rootNode.attachChild(camNode);  
        
        Vector3f pos = tank.getLocalTranslation().clone();
        
        posinit = tank.getLocalTranslation().clone();
        posinit.y +=40;
        
        
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
        
         
        if(time > delayEnemy+3000)
        {
            Enemy enemy = createEnemy(time);
            enemyList.add(enemy);
            rootNode.attachChild(enemy.getSpatial());
            if(Enemy e : enemyList.ge)
            {
                e.get
                
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
        tank.scale(0.35f);
        tank.setName(name);

        return tank;
    }
    
    public Enemy createEnemy(long timeEnemy)
    {
        delayEnemy = timeEnemy;
        
        Random rand = new Random();
        
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
}
