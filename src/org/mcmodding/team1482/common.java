/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mcmodding.team1482;

import edu.wpi.first.wpilibj.Solenoid;

/**
 *
 * @author Nicholas McCurry
 */
public class common {
    public static boolean liftSet(boolean state, Solenoid set, Solenoid retract) {
        //If retracted, extend
        if (state == false) {
            set.set(true);
            retract.set(false);
            return true;
        } //If iextended, retract
        else {
            set.set(false);
            retract.set(true);
            return false;
        }
    }
    public static void cycle(Solenoid set, Solenoid retract, int count) {
        switch (count) {
            case 0:
                set.set(false);
                retract.set(true);
                break;
            case 200:
                set.set(true);
                retract.set(false);
                break;
            case 400:
                count = 0;
        }

    }    
    
    
    
    
    
}
