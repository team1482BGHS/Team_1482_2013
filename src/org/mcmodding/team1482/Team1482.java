/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.mcmodding.team1482;


import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.AnalogModule;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PWM;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.lang.*;

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
    AnalogChannel sonicRange = new AnalogChannel(7);
    double vpi = 0;
    
    int m_disabledPeriodicLoops;
    int m_autoPeriodicLoops;
    int m_telePeriodicLoops;
    int m_teleEnabledLoops;
    int m_dsPacketsReceivedInCurrentSecond;    
    int cyclecount;
    boolean state = false;
    boolean state_shoot = false;
    boolean state_motor = false;
    
    //Set up demo mode variables
    boolean m_demoMode = false;
    boolean m_shooterEnabled = true;
    double m_driveSpeedModifier = 100;
    
    
    //setup talons
    Talon drive_left_back = new Talon(1);
    Talon drive_right_back = new Talon(3);
    Talon drive_left_front = new Talon(2);
    Talon drive_right_front = new Talon(4);
    Talon shoot             = new Talon(10);
    
    RobotDrive drive = new RobotDrive(drive_left_front, drive_left_back, drive_right_front, drive_right_back);
    
    
    
    
    //Joystick setup
    Joystick drivestick = new Joystick(1);
    Joystick shootstick = new Joystick(2);
    public static int NUM_JOYSTICK_BUTTONS = 16;
    //joystick buttons
    boolean[] m_driveStickButtonState = new boolean[(NUM_JOYSTICK_BUTTONS+1)];
    boolean[] m_shootStickButtonState = new boolean[(NUM_JOYSTICK_BUTTONS+1)];    
    //Pressed or heald
    String[] driveButtons = new String[(NUM_JOYSTICK_BUTTONS+1)]; 
    String[] shootButtons = new String[(NUM_JOYSTICK_BUTTONS+1)];
    boolean m_button_1;
    boolean m_button_2;
    boolean m_button_3;
    
    //Setup compressor
    Compressor airCompressor      = new Compressor(8,1);
    public Solenoid Shoot         = new Solenoid(1);
    public Solenoid ShootReset    = new Solenoid(2);
    public Solenoid Lift          = new Solenoid(3);
    public Solenoid LiftReset     = new Solenoid(4);
    
    DriverStation driver_station = DriverStation.getInstance();
    
    private Servo camPan  = new Servo(5);
    private Servo camTilt = new Servo(6);
    
    double panAngle;
    double tiltAngle;
    
    public Team1482() {
        System.out.println("Starting constructor!");

        for (int buttonNum = 1; buttonNum <= NUM_JOYSTICK_BUTTONS; buttonNum++) {
            //Set default vales for jpystick button arrays
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
        
        /* Uncomment code to invert motor*/
//        drive.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
        drive.setInvertedMotor(RobotDrive.MotorType.kFrontRight, true);
//        drive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);        
        drive.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);       
        
        
        System.out.println("RobotInit compleated!");
        getWatchdog().setEnabled(false);
        
        
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
        //set experation and enable watchdog
        getWatchdog().setEnabled(true);
        getWatchdog().setExpiration(0.5);
        airCompressor.start();
        
        //Get smartdashboard variables
        //m_demoMode = SmartDashboard.getBoolean("Demo Mode");
        //this.checkDemoMode(m_teleEnabledLoops, true);
    }
    /**
     * This function is called periodically during operator control
     */
    public void disabledInit() {
        System.out.println("Dissabled!");
    }
    public void disabledPeriodic() {
        getWatchdog().feed();
        Timer.delay(0.05);
        int blueVal = DriverStation.Alliance.kBlue_val;
        int redVal  = DriverStation.Alliance.kRed_val;
        SmartDashboard.putNumber("BlueVal", blueVal);
        SmartDashboard.putNumber("RedVal", redVal);
        Alliance alliance = driver_station.getAlliance();
        SmartDashboard.putString("Alliance Name", alliance.name);
    }
    public void teleopPeriodic() {
        if (isEnabled()) {
        //testing ultrasonic
        SmartDashboard.putNumber("Range Channel", sonicRange.getChannel());
        SmartDashboard.putNumber("Range Module", sonicRange.getModuleNumber());
        SmartDashboard.putNumber("Range voltage", sonicRange.getVoltage());
        SmartDashboard.putNumber("Range value", sonicRange.getValue());
        vpi = 0.009765625;
        double range = sonicRange.getVoltage() / vpi;
        int irange = (int) (range * 100);
        range = irange / 100.0;
        SmartDashboard.putNumber("distance", range);
        
        //SmartDashboard.putNumber("RangeSpeed", sonicRange.getSpeed());            

            //Get joystick values
            double drivestick_x = drivestick.getRawAxis(1);
            double drivestick_y = drivestick.getRawAxis(2);
            double camJoystick_x = drivestick.getRawAxis(4);
            double camJoystick_y = drivestick.getRawAxis(5);  
            
            panAngle = camPan.get();
            tiltAngle = camTilt.get();
            SmartDashboard.putNumber("Cam Pan", panAngle);
            SmartDashboard.putNumber("Cam Tilt", tiltAngle);
            //System.out.println(camJoystick_x + " Y is " + camJoystick_y);
            
            if(camJoystick_x <= -.2){
                //Rotate the camera left
                camPan.set(panAngle - (camJoystick_x / 100)); 
                //System.out.println("turning left");
                
            }else if(camJoystick_x >= .2){
                //Rotate the camera right
                camPan.set(panAngle + (camJoystick_x / 100));
                //System.out.println("turning right");
            }
            
            if(camJoystick_y >= .2){
                //Roatate the camera down
                camTilt.set(tiltAngle - (camJoystick_y / 100));
                //System.out.println("turning down");
            }else if(camJoystick_y <= -.2){
                //Rotate the camera up
                camTilt.set(tiltAngle + (camJoystick_y / 100));  
                //System.out.println("turning up");
            }
            
            

//            if (this.checkDemoMode(m_telePeriodicLoops, false)) {
//                //If is in demo mode apply speed modifier
//                speedModifier = m_driveSpeedModifier / 100;
//                drivestick_x = drivestick_x * speedModifier;
//                drivestick_y = drivestick_y * speedModifier;
//                //Put joystick values for debugging.
//                SmartDashboard.putNumber("drivestick_x", drivestick_x);
//                SmartDashboard.putNumber("drivestick_y", drivestick_y);
//                //If error in code was made stop the robot and print out error message!
//                if (drivestick_x > 1 || drivestick_y > 1) {
//                    System.out.println("ERROR!!!!!!!! JOYSTICK VALUE IS GREATOR THAT 1 !!! BIG PROBLEM! DISSABLEING ROBOT ");
//                    drive.stopMotor();
//                    return;
//                }
//            }
            drive.arcadeDrive(drivestick_x, drivestick_y);
            
            m_button_1 = drivestick.getRawButton(1);
            m_button_2 = drivestick.getRawButton(2);
            m_button_3 = drivestick.getRawButton(3);
//            if (m_button_1.equalsIgnoreCase("pressed")) {
//                System.out.println("Button 1 pressed!");
//                //Reset cycle count
//                cyclecount = 0;
//            }
//            else if(m_button_1.equalsIgnoreCase("held"))
//            {
//                //Semi auto shooting
//                common.cycle(Shoot, ShootReset, cyclecount);
//                cyclecount++;
//            }
            //Increment cycle count
            
            //Button press and not pressed before
            if(m_button_1 && !m_driveStickButtonState[1]){
                System.out.println("Pressed!");
                //Set the sate of the button
                m_driveStickButtonState[1] = true;
                //Extend/Retract lifter
                state = common.liftSet(state, Lift, LiftReset);
            }else if(!m_button_1 && m_driveStickButtonState[1]){
                System.out.println("Reseting button!");
                m_driveStickButtonState[1] = false;
            }
            if(m_button_2&& !m_driveStickButtonState[2]){
                System.out.println("Pressed 2");
                state_shoot = common.liftSet(state_shoot, Shoot, ShootReset);
                m_driveStickButtonState[2] = true;
            }else if(!m_button_2 && m_driveStickButtonState[2]){
                System.out.println("Reset 2");
                m_driveStickButtonState[2] = false;
            }
            if(m_button_3&& !m_driveStickButtonState[3]){
                System.out.println("Pressed 3");
                state_motor = common.motor(state_motor, shoot);
                m_driveStickButtonState[3] = true;
            }else if(!m_button_3 && m_driveStickButtonState[3]){
                System.out.println("Reset 3");
                m_driveStickButtonState[3] = false;
            }
            //feed the watchdog
            getWatchdog().feed();
            Timer.delay(0.01);
        }else{
            //Feed the watchdog when dissabled
            getWatchdog().feed();
            Timer.delay(0.04);
        }
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
