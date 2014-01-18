/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package org.mcmodding.team1482;


import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PWM;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Watchdog;
import edu.wpi.first.wpilibj.buttons.DigitalIOButton;
import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.image.CriteriaCollection;
import edu.wpi.first.wpilibj.image.NIVision;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Team1482 extends IterativeRobot {
    //timing ints
    int m_disabledPeriodicLoops;
    int m_autoPeriodicLoops;
    int m_telePeriodicLoops;
    int m_teleEnabledLoops;
    int m_dsPacketsReceivedInCurrentSecond;    
    int cyclecount;
    //States for 
    boolean state_lift = false;
    boolean state_shoot = false;
    boolean state_motor = false;
    
    //Set up demo mode variables
    boolean m_demoMode = false;
    boolean m_shooterEnabled = true;
    double m_driveSpeedModifier = 100;
    
    
    //setup talons
    Talon drive_left_back = new Talon(6);
    Talon drive_right_back = new Talon(7);
    Talon drive_left_front = new Talon(8);
    Talon drive_right_front = new Talon(2);
    Talon shoot             = new Talon(10);
    // Enabled 4 motor drive, switched Talon 2/4 to 1/2 for less confusing connection, also considering reordering to LLRR instead or LRLR
    //Create drive object
    RobotDrive drive = new RobotDrive(drive_left_front, drive_left_back, drive_right_front, drive_right_back);
    //RobotDrive drive = new RobotDrive(drive_left_back, drive_right_back);
    
    //Ultrasonic sensor object
    PWM sonicRange = new PWM(5); //use rge Ultrasonic API!!!!! file:///C:/Users/student/sunspotfrcsdk/doc/javadoc/edu/wpi/first/wpilibj/Ultrasonic.html


    //Create new encoders
    Encoder encoderLeft = new Encoder(1, 2);
    Encoder encoderRight = new Encoder(3, 4);
    //Encoder related variables
    double LeftSpeed; //Encoder speed
    double LeftAbsSpeed; //Absolute encoder speed
    double RightSpeed;
    double RightAbsSpeed;
    int gear;  //Current gear
    DigitalIOButton button5 = new DigitalIOButton(5);
    
    double modifyJoystickSpeed;
    boolean rpmMatching;
    boolean[] dioButton = new boolean[14];
    
    //Joystick setup
    Joystick drivestick = new Joystick(1);
    Joystick shootstick = new Joystick(2);
    double drivestick_x = 0;
    double drivestick_y = 0;
    double camJoystick_x = 0;
    double camJoystick_y = 0;
    double trigger = 0;
    boolean manual = false;
    //Number of joystick buttons
    public static int NUM_JOYSTICK_BUTTONS = 16;
    //Array to set if button was pressed last itteration
    boolean[] m_driveStickButtonState = new boolean[(NUM_JOYSTICK_BUTTONS+1)];
    boolean[] m_shootStickButtonState = new boolean[(NUM_JOYSTICK_BUTTONS+1)];    
    //Array to set value of button
    boolean[] driveButtons = new boolean[(NUM_JOYSTICK_BUTTONS+1)]; 
    boolean[] shootButtons = new boolean[(NUM_JOYSTICK_BUTTONS+1)];
    //Arrays are one longer than number of buttons to avoid indexing confusion
    //(ie: the number of the button is the same as its index in the array. The
    // first space, index 0, is never used.
    
    double joystickYaw;
    
    //Setup compressor
    Compressor airCompressor      = new Compressor(8,1);
    //Setup solonides
    public Solenoid Shoot         = new Solenoid(1);
    public Solenoid ShootReset    = new Solenoid(2);
    public Solenoid Lift          = new Solenoid(3);
    public Solenoid LiftReset     = new Solenoid(4);
    //Our vision object

    //AxisCamera camera;          // the axis camera object (connected to the switch)
    //CriteriaCollection cc;      // the criteria for doing the particle filter operation
    double combinedSpeed;
    //vision our_camera = new vision();
    
    
    
    Servo camPan  = new Servo(4);
    Servo camTilt = new Servo(3);
    
    double panAngle;
    double tiltAngle;
    
    
    vision V = new vision();
    
    public Team1482() {
        System.out.println("Starting constructor!");
       
        for (int buttonNum = 1; buttonNum > NUM_JOYSTICK_BUTTONS; buttonNum++) {
            //Set default vales for jpystick button arrays
            m_driveStickButtonState[buttonNum] = false;
            m_shootStickButtonState[buttonNum] = false;  
            driveButtons[buttonNum] = false;
            shootButtons[buttonNum] = false;
            //first array entry (index 0) is never used, so that button numbers
            //correspond to index numbers
        }        
    }
    
    public void robotInit() {

        //cc = new CriteriaCollection();      // create the criteria for the particle filter
        //cc.addCriteria(NIVision.MeasurementType.IMAQ_MT_AREA, 500, 65535, false);
        System.out.println("Starting RobotInit");
        //get smartdashboard variables

        
        SmartDashboard.putBoolean("Lift State", false);
        SmartDashboard.getBoolean("Demo Mode", false);
        SmartDashboard.getBoolean("Enable shooter?" , true);

        
        /* Uncomment code to invert motor*/
        drive.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
        drive.setInvertedMotor(RobotDrive.MotorType.kFrontRight, true);
        drive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);        
        drive.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);    
        //Start encoders
        encoderLeft.start();
        encoderRight.start();
        //Set distance ratio
        encoderLeft.setDistancePerPulse(2);        
        encoderRight.setDistancePerPulse(2);
        System.out.println("RobotInit completed!");
        getWatchdog().setEnabled(false);
// Fixed a typo, used to say "compleated" and changed to "completed"
        //Similar note, replaced every instance of "dissabled" to "disabled". changes only occured in printout texts and comments.
        
    }

    /**
     * This function is called at the start of autonomous
     */
    public void autonomousInit() {
        getWatchdog().setEnabled(false);
        System.out.println("There is no autonomous code!");
        //TODO: Make automous code!
        
//        System.out.println("Auto run!");
//        our_camera.getImage();
//        
        getWatchdog().setEnabled(true);
        System.out.println("Finished autonomus");
    }

    public void teleopInit() {   //Called at the start of teleop
        System.out.println("Starting Teleop!");
        //Reset loop counters
        m_teleEnabledLoops = 0;
        m_telePeriodicLoops = 0;
        
        //set experation and enable watchdog
        getWatchdog().setEnabled(true);
        getWatchdog().setExpiration(0.5);
        //Start compresser
        airCompressor.start();
        //Set defualt values for gear shifter
        Lift.set(true);
        LiftReset.set(false);
        gear = 1;
        
        
        
        //Get smartdashboard variables
        //m_demoMode = SmartDashboard.getBoolean("Demo Mode");
        //this.checkDemoMode(m_teleEnabledLoops, true);
    }
    public void disabledInit() { //Called when disabled
        
        System.out.println("Disabled!");
    }
    public void disabledPeriodic() { //called throughout when disabled
        getWatchdog().feed();
        Timer.delay(0.07);
    }
    public void teleopPeriodic() {  //Called durring operated control
        if (isEnabled()) {  //If the robot is enabled
            //Get joystick values
            drivestick_x = drivestick.getRawAxis(1);
            drivestick_y = drivestick.getRawAxis(2);
            trigger       = drivestick.getRawAxis(3);
            
            
            //print controller drive inputs
            SmartDashboard.putNumber("X Input", drivestick_x);
            SmartDashboard.putNumber("Y Input", drivestick_y);
            camJoystick_x = drivestick.getRawAxis(4);
            camJoystick_y = drivestick.getRawAxis(5);  
            
            
            panAngle = camPan.get();
            tiltAngle = camTilt.get();
            SmartDashboard.putNumber("Cam Pan", panAngle);
            SmartDashboard.putNumber("Cam Tilt", tiltAngle);
            //System.out.println(camJoystick_x + " Y is " + camJoystick_y);
            
            if(camJoystick_x <= -.2){
                //Rotate the camera left
                camPan.set(panAngle + (camJoystick_x / 30)); 
                System.out.println("turning left");
                
            }else if(camJoystick_x >= .2){
                //Rotate the camera right
                camPan.set(panAngle + (camJoystick_x / 30));
                System.out.println("turning right");
            }
            
            if(camJoystick_y >= .2){
                //Roatate the camera down
                camTilt.set(tiltAngle - (camJoystick_y / 30));
                //System.out.println("turning down");
            }else if(camJoystick_y <= -.2){
                //Rotate the camera up
                camTilt.set(tiltAngle - (camJoystick_y / 30));  
                //System.out.println("turning up");
            }
            if(rpmMatching){ //If is RPM is changing
                if(gear == 2){ //See if robot is in gear 2
                    //Speed matching code here
                    if(modifyJoystickSpeed >= 1){ //Finished speeding up
                        modifyJoystickSpeed = 1;  //Set back to normal
                        System.out.println("Finished speeding up!");
                        SmartDashboard.putNumber("Speed modifier", modifyJoystickSpeed);
                        rpmMatching = false;
                        
                    }else{
                        modifyJoystickSpeed = modifyJoystickSpeed + Config.GEARUPDELAY;
                        SmartDashboard.putNumber("Speed modifier", modifyJoystickSpeed);
                        drivestick_x = drivestick_x * modifyJoystickSpeed; //Set speed based on modifier
                        drivestick_y = drivestick_y * modifyJoystickSpeed; //Set speed based on modifier
                    }
                }else{ //Robot is no longer in gear 2, switch back to normal
                    System.out.println("Error! Not in gear 2 anymore!");
                    rpmMatching = false;
                    modifyJoystickSpeed = 1;
                }
            }
            
            //Get speed from encoder
            LeftSpeed = encoderLeft.getRate();
            RightSpeed = encoderRight.getRate();
            LeftAbsSpeed = Math.abs(LeftSpeed); //Get the absolute value of encoder value
            RightAbsSpeed = Math.abs(RightSpeed);
            combinedSpeed = LeftAbsSpeed + RightAbsSpeed;
            joystickYaw = Math.abs(drivestick_x);

            
            SmartDashboard.putNumber("Left speed", LeftSpeed); //Display speed on dashboard
            SmartDashboard.putNumber("Right speed", RightSpeed); //Display speed on dashboard
            SmartDashboard.putNumber("Gear", gear);
            SmartDashboard.putBoolean("rpm matching", rpmMatching);
            SmartDashboard.putNumber("Modifier", modifyJoystickSpeed);
            SmartDashboard.putNumber("combinedSpeed", combinedSpeed);
            
                    
        //testing ultrasonic
        SmartDashboard.putNumber("Range Channel", sonicRange.getChannel());
        SmartDashboard.putNumber("Range Module", sonicRange.getModuleNumber());
        SmartDashboard.putNumber("Range Raw", sonicRange.getRaw());
        SmartDashboard.putNumber("Range Position", sonicRange.getPosition());
        SmartDashboard.putNumber("RangeSpeed", sonicRange.getSpeed());
        
            if(trigger >= .5){
                //If the right trigger is pressed switch to gear 2
                manual = true;
                Lift.set(false);
                LiftReset.set(true);
                gear = 2;
            }else if(trigger <= -.5){
                //If the left trigger is pressed switch to gear 1;
                System.out.println("Gearing Down");    
                manual = true;
                Lift.set(true);
                LiftReset.set(false);
                gear = 1;
            }else{
                manual = false;
            }
            
            if(gear == 1 && combinedSpeed > Config.GEARUP && !manual){ //Switch gear up if is in gear one and is above configured speed
                //Switch to gear 2
                Lift.set(false);
                LiftReset.set(true);
                System.out.println("Swited to gear 2! at speed of " + LeftAbsSpeed);
                gear = 2;
                //Code to slow motor when switching gears.
                modifyJoystickSpeed = Config.GEARCHANGESPEEDDEFAULT;
                rpmMatching = true;
                
            }else if(gear ==2 && combinedSpeed < Config.GEARDOWN && !manual){  //Gear down if is in gear 2 and is below configured speed
                //Switch to gear 1
                Lift.set(true);
                LiftReset.set(false);
                System.out.println("Switched to gear 1! at a speed of " + LeftAbsSpeed);
                gear = 1;
            }
            
            
            //Drive motors based on joystick values
            drive.arcadeDrive(drivestick_x, drivestick_y);
            
            //Get values of joystick buttons
            shootButtons[1] = drivestick.getRawButton(1);
            shootButtons[2] = drivestick.getRawButton(2);
            shootButtons[3] = drivestick.getRawButton(3);
            shootButtons[4] = drivestick.getRawButton(4);
            //DIO buttons
            dioButton[5] = button5.get();
            
            
            SmartDashboard.putBoolean("DIO button 5", dioButton[5]);            
            
            //Button press and not pressed before

            /* BUTTON ONE CODE */
            if(shootButtons[1] && !m_driveStickButtonState[1]){
                System.out.println("Pressed!");
                //Set the sate of the button
                m_driveStickButtonState[1] = true;
                //Extend/Retract lifter
                state_lift = common.liftSet(state_lift, Lift, LiftReset);
                gear = 1;
            }else if(!shootButtons[1] && m_driveStickButtonState[1]){
                System.out.println("Reseting button!");
                m_driveStickButtonState[1] = false;
            }
            
            
            /* BUTTON TWO CODE */
            if(shootButtons[2]&& !m_driveStickButtonState[2]){
                System.out.println("Pressed 2");
                //toggle piston
                state_shoot = common.liftSet(state_shoot, Shoot, ShootReset);
                m_driveStickButtonState[2] = true;
            }else if(!shootButtons[2] && m_driveStickButtonState[2]){
                System.out.println("Reset 2");
                //Reset variable
                m_driveStickButtonState[2] = false;
            }
            
            
            /* BUTTON THREE CODE */
            if(shootButtons[3]&& !m_driveStickButtonState[3]){
                System.out.println("Pressed 3");
                //Turn motor on/off
                state_motor = common.motor(state_motor, shoot);
                m_driveStickButtonState[3] = true;
            }else if(!shootButtons[3] && m_driveStickButtonState[3]){
                System.out.println("Reset 3");
                //Reset variable
                m_driveStickButtonState[3] = false;
            }
            /* BUTTON FOUR CODE */
            if(shootButtons[4]&& !m_driveStickButtonState[4]){
                System.out.println("Pressed 4");
                //Turn motor on/off
                V.getImage();
                m_driveStickButtonState[4] = true;
            }else if(!shootButtons[4] && m_driveStickButtonState[4]){
                System.out.println("Reset 4");
                //Reset variable
                m_driveStickButtonState[4] = false;
            }            
            
            
            //feed the watchdog
            getWatchdog().feed();
            Timer.delay(0.01);
        }else{
            //Feed the watchdog when disabled
            getWatchdog().feed();
            Timer.delay(0.04);
        }
    }
    
    
    public boolean checkDemoMode(int loops, boolean force) {
        //See if robot is in disabled mode every 40 loops
        if(loops % 40 == 0 || force){
            m_demoMode = SmartDashboard.getBoolean("Demo Mode");
        }
        if (m_demoMode) {
            //excute once every 40 calls
            if (loops % 40 == 0 || force) {
                
                //DEMO MODE CODE HERE!
                m_shooterEnabled = SmartDashboard.getBoolean("Enable shooter?");
                if (!m_shooterEnabled) {
                    System.out.println("Shooter disabled!");
                }
                m_driveSpeedModifier = (int) SmartDashboard.getNumber("Drive speed modifier");

            }
            return true;
        } else {
            return false;
        }

    }
}
