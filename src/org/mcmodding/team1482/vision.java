/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mcmodding.team1482;

import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.camera.AxisCameraException;
import edu.wpi.first.wpilibj.image.BinaryImage;
import edu.wpi.first.wpilibj.image.ColorImage;
import edu.wpi.first.wpilibj.image.CriteriaCollection;
import edu.wpi.first.wpilibj.image.NIVision;
import edu.wpi.first.wpilibj.image.NIVisionException;

/**
 *
 * @author student
 */
public class vision {

    AxisCamera camera;
    CriteriaCollection cc; 

    vision() {
        camera = AxisCamera.getInstance();
        cc = new CriteriaCollection();      // create the criteria for the particle filter
        cc.addCriteria(NIVision.MeasurementType.IMAQ_MT_AREA, 500, 65535, false);

    }

    public void getImage() {
        try {
            ColorImage image = camera.getImage();
            image.write("/Camera_Image.bmp");
            //BinaryImage thresholdImage = image.thresholdHSV(60, 100, 90, 255, 20, 255);
            BinaryImage thresholdImage = image.thresholdHSV(10, 20, 10, 20, 10, 20);   
            thresholdImage.write("/threshold.bmp");
            BinaryImage RGBImage = image.thresholdRGB(60, 255, 0, 50, 0, 50);
            RGBImage.write("/rgbImage.bmp");
        } catch (NIVisionException ex) {
            ex.printStackTrace();
        } catch (AxisCameraException ex) {
            ex.printStackTrace();
        }

    }
}
   
