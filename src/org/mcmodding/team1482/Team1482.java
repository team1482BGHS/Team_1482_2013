/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.mcmodding.team1482;


import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Team1482 extends IterativeRobot {
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    //timing ints
    int m_disabledPeriodicLoops;
    int m_autoPeriodicLoops;
    int m_telePeriodicLoops;
    int m_teleEnabledLoops;
    int m_dsPacketsReceivedInCurrentSecond;    
    int cyclecount;
    
    //Set up demo mode variables
    boolean m_demoMode = false;
    boolean m_shooterEnabled = true;
    double m_driveSpeedModifier = 100;
    
    
    //setup talons
    Talon drive_left_back = new Talon(1);
    Talon drive_right_back = new Talon(2);
    Talon drive_left_front = new Talon(3);
    Talon drive_right_front = new Talon(4);
    
    RobotDrive drive = new RobotDrive(drive_left_front, drive_left_back, drive_right_front, drive_right_back);
    
    //Joystick setup
    Joystick drivestick = new Joystick(1);
    Joystick shootstick = new Joystick(2);
    public static int NUM_JOYSTICK_BUTTONS = 16;
    //joystick buttons
    boolean[] m_driveStickButtonState = new boolean[(NUM_JOYSTICK_BUTTONS+1)];
    boolean[] m_shootStickButtonState = new boolean[(NUM_JOYSTICK_BUTTONS+1)];    
    //Pressed or heald
    String[] driveButtons = new String[16];
    String[] shootButtons = new String[16];
    
    //Setup compressor
    Compressor airCompressor      = new Compressor(1,1);
    public Solenoid Shoot         = new Solenoid(1);
    public Solenoid ShootReset    = new Solenoid(2);
    public Solenoid Lift          = new Solenoid(3);
    public Solenoid LiftReset     = new Solenoid(4);
    
    
    public Team1482() {
        System.out.println("Starting constructor!");

        for (int buttonNum = 1; buttonNum <= NUM_JOYSTICK_BUTTONS; buttonNum++) {
            m_driveStickButtonState[buttonNum] = false;
            m_shootStickButtonState[buttonNum] = false;  
            driveButtons[buttonNum] = null;
            shootButtons[buttonNum] = null;
        }        
    }
    
    public void robotInit() {
        System.out.println("Starting RobotInit");
        //get smartdashboard variables
        SmartDashboard.putBoolean("Lift State", false);
        SmartDashboard.getBoolean("Demo Mode", false);
        SmartDashboard.getBoolean("Enable shooter?" , true);
        
        System.out.println("RobotInit compleated!");
        
        
        
    }

    /**
     * This function is called at the start of autonomous
     */
    public void autonomousInit() {
        System.out.println("There is no autonomous code!");
    }

    public void teleopInit() {
        System.out.println("Starting Teleop!");
        //Reset loop counters
        m_teleEnabledLoops = 0;
        m_telePeriodicLoops = 0;
        getWatchdog().setEnabled(true);
        getWatchdog().setExpiration(0.05);
        airCompressor.start();
        
        //Get smartdashboard variables
        m_demoMode = SmartDashboard.getBoolean("Demo Mode");
        this.checkDemoMode(m_teleEnabledLoops, true);
    }
    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        double speedModifier;
        //Get joystck values
        double drivestick_x = drivestick.getRawAxis(1);
        double drivestick_y = drivestick.getRawAxis(2);
        
        if(this.checkDemoMode(m_telePeriodicLoops, false)){
            //If is in demo mode apply speed modifier
            speedModifier = m_driveSpeedModifier / 100;
            drivestick_x = drivestick_x * speedModifier;
            drivestick_y = drivestick_y * speedModifier;
            SmartDashboard.putNumber("drivestick_x", drivestick_x);
            SmartDashboard.putNumber("drivestick_y", drivestick_y);
        }
        drive.arcadeDrive(drivestick_x, drivestick_y);
        getWatchdog().feed();
        Timer.delay(0.01);
        
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }



    public boolean checkDemoMode(int loops, boolean force) {
        //See if robot is in dissabled mode every 40 loops
        if(loops % 40 == 0 || force){
            m_demoMode = SmartDashboard.getBoolean("Demo Mode");
        }
        if (m_demoMode) {
            //excute once every 40 calls
            if (loops % 40 == 0 || force) {
                
                //DEMO MODE CODE HERE!
                m_shooterEnabled = SmartDashboard.getBoolean("Enable shooter?");
                if (!m_shooterEnabled) {
                    System.out.println("Shooter dissabled!");
                }
                m_driveSpeedModifier = (int) SmartDashboard.getNumber("Drive speed modifier");

            }
            return true;
        } else {
            return false;
        }

    }
}
