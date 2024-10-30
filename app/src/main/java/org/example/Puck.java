/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Random;

/**
 *
 * @author almacro
 */
public class Puck {

    final Component rink;
    final Image image;
    final int maxVelocity;
    final Rectangle edges;
    final Random random;
    
    boolean doNotMove, outOfAction;
    Point velocity;
    Rectangle rectangle;
    
    public Puck(Image image, int type, int maxVelocity, 
                Rectangle edges, Component rink) {
        this.rink = rink;
        this.image = image;
        if (type > 0) {
            doNotMove = true;
        }
        this.maxVelocity = maxVelocity;
        this.edges = edges;
        
        random = new Random(System.currentTimeMillis());
        
        Point location = null;
        if(type == 0) {
            location = new Point(100 + (Math.abs(random.nextInt())
                    % 300), 100 + (Math.abs(100 + random.nextInt()) % 100));
            
            this.velocity = new Point(random.nextInt() % maxVelocity, 
                    random.nextInt() % maxVelocity);
        
            while(velocity.x == 0) {
                velocity.x = random.nextInt(maxVelocity / 2)
                        - maxVelocity / 2;
            }
        }
        
        if(type == 1) {
            location = new Point(((Slapshot)rink).backgroundImage.getWidth(rink) - 18,
                    ((Slapshot)rink).backgroundImage.getHeight(rink)/2);
            this.velocity = new Point(0, 0);
        }
        
        if(type == 2) {
            location = new Point(10, ((Slapshot)rink).backgroundImage.getHeight(rink)/2);
            this.velocity = new Point(0, 0);
        }
        
        this.rectangle = new Rectangle(location.x, location.y,
            image.getWidth(rink), image.getHeight(rink));
    }
    
    public int slide(Rectangle blocker, Rectangle blocker2) {
        Point position = new Point(rectangle.x, rectangle.y);
        int returnValue = 0;
        
        if(doNotMove) {
            return returnValue;
        }
        
        if(random.nextInt(100) <= 1) {
            velocity.x += random.nextInt() % maxVelocity;
            
            velocity.x = Math.min(velocity.x, maxVelocity);
            velocity.x = Math.max(velocity.x, -maxVelocity);
            
            while(velocity.x == 0) {
                velocity.x = random.nextInt(maxVelocity / 2) - maxVelocity / 2;
            }
            
            velocity.y += random.nextInt() % maxVelocity / 2;
            
            velocity.y = Math.min(velocity.y, maxVelocity / 2);
            velocity.y = Math.max(velocity.y, -(maxVelocity / 2));
        }
        
        position.x += velocity.x;
        position.y += velocity.y;
        
        if (position.x < edges.x + 5) {
            if (position.y > 120 && position.y < 225) {
                if(!rectangle.intersects(blocker)) {
                    returnValue = 1;
                    outOfAction = true;
                    return returnValue;
                }
            }
            position.x = edges.x;
            
            if(velocity.x > 0) {
               velocity.x = -6; 
            } else {
               velocity.x = 6; 
            }
        } else if ((position.x + rectangle.width)
                > (edges.x + edges.width - 5)) {
            if(position.y > 120 && position.y < 225) {
                if(!rectangle.intersects(blocker2)) {
                    returnValue = -1;
                    outOfAction = true;
                    return returnValue;
                }
            }
            position.x = edges.x + edges.width - rectangle.width;
            
            if(velocity.x > 0) {
                velocity.x = -6; 
            } else {
               velocity.x = 6; 
            }
        }
        
        if (position.y < edges.y) {
            position.y = edges.y;
            velocity.y = -velocity.y;
        } else if((position.y + rectangle.height) 
                > (edges.y + edges.height)) {
            position.y = edges.y + edges.height - rectangle.height;
            velocity.y = -velocity.y;
        }
        
        this.rectangle = new Rectangle(position.x, position.y, 
            image.getWidth(rink), image.getHeight(rink));
        
        return returnValue;
    }
    
    public void slide(int y) {
        rectangle.y = y;
    }
    
    public boolean gone() {
        return outOfAction;
    }
    
    public boolean immovable() {
        return doNotMove;
    }
    
    public void drawPuckImage(Graphics g) {
        if(!outOfAction) {
            g.drawImage(image, rectangle.x, rectangle.y, rink);
        }
    }
}
