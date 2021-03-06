/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.scene.Spatial;

/**
 *
 * @author Rafael
 */
public class Enemy {
    
    private Spatial spatial;
    private AnimChannel channel;
    private AnimControl control;
    private String name;
    private long delayEnemyBullet;
    private int life = 100;

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }
    

    public Spatial getSpatial() {
        return spatial;
    }

    public void setSpatial(Spatial spatial) {
        this.spatial = spatial;
    }

    public AnimChannel getChannel() {
        return channel;
    }

    public void setChannel(AnimChannel channel) {
        this.channel = channel;
    }

    public AnimControl getControl() {
        return control;
    }

    public void setControl(AnimControl control) {
        this.control = control;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDelayEnemyBullet() {
        return delayEnemyBullet;
    }

    public void setDelayEnemyBullet(long delayEnemyBullet) {
        this.delayEnemyBullet = delayEnemyBullet;
    }
}
