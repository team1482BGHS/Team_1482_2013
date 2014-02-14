/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package org.mcmodding.team1482;

//Robo-Sempai please work
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
import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.buttons.DigitalIOButton;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.PIDController;
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
    Talon drive_left_back   = new Talon(1);
    Talon drive_right_back  = new Talon(2);
    Talon drive_left_front  = new Talon(3);
    Talon drive_right_front = new Talon(4);
    Talon punch             = new Talon(7);
    Talon pickup            = new Talon(10);
    // Enabled 4 motor drive, switched Talon 2/4 to 1/2 for less confusing connection, also considering reordering to LLRR instead or LRLR
    //Create drive object
    RobotDrive drive = new RobotDrive(drive_left_front, drive_left_back, drive_right_front, drive_right_back);
    //RobotDrive drive = new RobotDrive(drive_left_back, drive_right_back);
    

    //Create new encoders
    Encoder encoderRight = new Encoder(1, 2);
    Encoder encoderLeft  = new Encoder(3, 4);
    //Encoder related variables
    double LeftSpeed; //Encoder speed
    double LeftAbsSpeed; //Absolute encoder speed
    double RightSpeed;
    double RightAbsSpeed;
    int gear;  //Current gear
    DigitalInput button5 = new DigitalInput(5);
    DigitalInput PunchLimit = new DigitalInput(6);
    DigitalInput button7 = new DigitalInput(7);
    //Use DigitalInput,not DigitalIOButton
    //http://www.chiefdelphi.com/forums/showthread.php?t=104012&highlight=digitaliobutton
    DigitalInput button8 = new DigitalInput(8); //Spins the  motor to pull launcher until it is pressed
    boolean shooterCharged = false;
    
    double modifyJoystickSpeed;
    boolean rpmMatching;
    boolean[] dioButton = new boolean[14];
    boolean pickuptoggle;
    boolean pickuptracking;
    
    //Joystick setup
    Joystick driveStick = new Joystick(1);
    Joystick armStick = new Joystick(2);
    double drivestick_x = 0;
    double drivestick_y = 0;
    double camJoystick_x = 0;
    double camJoystick_y = 0;
    boolean manual = false;
    //Number of joystick buttons
    public static int NUM_JOYSTICK_BUTTONS = 16;
    //Array to set if button was pressed last itteration
    boolean[] m_driveStickButtonState = new boolean[(NUM_JOYSTICK_BUTTONS+1)];
    boolean[] m_armStickButtonState = new boolean[(NUM_JOYSTICK_BUTTONS+1)];    
    //Array to set value of button
    boolean[] driveButtons = new boolean[(NUM_JOYSTICK_BUTTONS+1)]; 
    boolean[] armButtons = new boolean[(NUM_JOYSTICK_BUTTONS+1)];
    //Arrays are one longer than number of buttons to avoid indexing confusion
    //(ie: the number of the button is the same as its index in the array. The
    // first space, index 0, is never used.
    
    double joystickYaw;
    
    //Setup compressor
    Compressor airCompressor      = new Compressor(9,1);
    
    //Setup solenoids
    public Solenoid WheelLift         = new Solenoid (1); //Piston 1 Extended
    public Solenoid WheelLiftReset    = new Solenoid (2); //Piston 1 Retracted
    public Solenoid TipperKickStart   = new Solenoid (3);  //Piston 2 Extended
    public Solenoid TipperKickStartReset = new Solenoid (4); //Piston 2 Retracted
    public Solenoid Tipper            = new Solenoid(5); //Piston 3 Extended
    public Solenoid TipperReset       = new Solenoid(6); //Piston 3 Retracted
    public Solenoid PunchGear         = new Solenoid (7); //Piston 4 Extended
    public Solenoid PunchNeutral      = new Solenoid (8); //Piston 4 Retracted
    public Solenoid ShooterLock       = new Solenoid(2,1); //Piston 5 Extended
    public Solenoid ShooterLockReset  = new Solenoid(2,2); //Piston 5 Retracted
    public Solenoid DriveGear1          = new Solenoid (2,3); //Piston 6 - Drive 1st Gear
    public Solenoid DriveGear2          = new Solenoid (2,4); //Piston 6 - Drive 2nd Gear
    
    //States for pistons
    public boolean m_wheellift_state = false;
    public boolean m_tipper_state = false;
    public boolean m_charge_punch = false;
    public boolean m_kickStart    = true;
    public boolean m_wheel_pickup_state = false;
    //add more arm solenoids

    double combinedSpeed;
        
    Servo camPan  = new Servo(5);
    Servo camTilt = new Servo(6);
    
    double panAngle;
    double tiltAngle;
    
    //vision V = new vision();
    
    double distance;
    
    public Team1482() {
        System.out.println("Starting constructor!");
       
        for (int buttonNum = 1; buttonNum > NUM_JOYSTICK_BUTTONS; buttonNum++) {
            //Set default vales for jpystick button arrays
            m_driveStickButtonState[buttonNum] = false;
            m_armStickButtonState[buttonNum] = false;  
            driveButtons[buttonNum] = false;
            armButtons[buttonNum] = false;
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
        SmartDashboard.putNumber("distance", 0);
        
        /* Uncomment code to invert motor*/
        drive.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
        //drive.setInvertedMotor(RobotDrive.MotorType.kFrontRight, true);
        drive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);        
        //drive.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);    
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
        
        //WheelList 1 (reset)
        //Tipper Kick Start (2) extends
        //Tipper (3) retracted
        //PunchGear (4) extended
        //Shooter lock (5)extends
        //DriveGear1 (extended)
        
        
//        System.out.println("Auto run!");
//        our_camera.getImage();
//        
        getWatchdog().setEnabled(true);
        System.out.println("Finished Autonomous");
    }
    
    public void autonomousPeriodic() {
        //smartdashboard.number
        
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
        DriveGear1.set(true);
        DriveGear2.set(false);
        gear = 1;
        pickuptoggle = true;
        m_wheel_pickup_state = false;
        m_wheellift_state =  false;
        common.liftSet(true, WheelLift, WheelLiftReset);
        m_tipper_state = false;
        common.liftSet(true, Tipper, TipperReset);
        m_kickStart = true;
        common.liftSet(true, TipperKickStart, TipperKickStartReset);
        //Get smartdashboard variables
        //m_demoMode = SmartDashboard.getBoolean("Demo Mode");
        //this.checkDemoMode(m_teleEnabledLoops, true);
    }
    public void disabledInit() { //Called when disabled
        
        System.out.println("Disabled!");
        airCompressor.stop();
    }
    public void disabledPeriodic() { //called throughout when disabled
        getWatchdog().feed();
        Timer.delay(0.07);
    }
    public void joystick1(){
            SmartDashboard.putNumber("X Input", drivestick_x);
            SmartDashboard.putNumber("Y Input", drivestick_y);
            SmartDashboard.putNumber("Cam Pan", panAngle);
            
            //Get joystick1 values
            drivestick_x = driveStick.getRawAxis(1);
            drivestick_y = driveStick.getRawAxis(2);
            double trigger       = driveStick.getRawAxis(3);
            //camJoystick_x = driveStick.getRawAxis(4); //Not used in 2014 bot
            camJoystick_y = driveStick.getRawAxis(5); 
            
            //panAngle = camPan.get();//Not used for 2014 bot
            tiltAngle = camTilt.get();
            
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
            
            /* Encoders */
            LeftSpeed = encoderLeft.getRate();
            RightSpeed = encoderRight.getRate();
            LeftAbsSpeed = Math.abs(LeftSpeed); //Get the absolute value of encoder value
            RightAbsSpeed = Math.abs(RightSpeed);
            combinedSpeed = LeftAbsSpeed + RightAbsSpeed;
            joystickYaw = Math.abs(drivestick_x);
            
             //Gear and speed debug
            SmartDashboard.putNumber("Left speed", LeftSpeed); //Display speed on dashboard
            SmartDashboard.putNumber("Right speed", RightSpeed); //Display speed on dashboard
            SmartDashboard.putNumber("Gear", gear);
            SmartDashboard.putBoolean("rpm matching", rpmMatching);
            SmartDashboard.putNumber("Modifier", modifyJoystickSpeed);
            SmartDashboard.putNumber("combinedSpeed", combinedSpeed);  
            
            if(rpmMatching){ //If is RPM is changing
                if(gear == 2){ //See if robot is in gear 2
                    //Speed matching code here
                    if(modifyJoystickSpeed >= 1){ //Finished speeding up
                        modifyJoystickSpeed = 1;  //Set back to normal
                        System.out.println("Finished speeding up!");
                        rpmMatching = false;
                        
                    }else{
                        modifyJoystickSpeed = modifyJoystickSpeed + Config.GEARUPDELAY;
                        drivestick_x = drivestick_x * modifyJoystickSpeed; //Set speed based on modifier
                        drivestick_y = drivestick_y * modifyJoystickSpeed; //Set speed based on modifier
                    }
                }else{ //Robot is no longer in gear 2, switch back to normal
                    System.out.println("Error! Not in gear 2 anymore!");
                    rpmMatching = false;
                    modifyJoystickSpeed = 1;
                }
            }
                        
            if(trigger >= .5){
                //If the right trigger is pressed switch to gear 2
                manual = true;
                DriveGear1.set(false);
                DriveGear2.set(true);
                gear = 2;
            }else if(trigger <= -.5){
                //If the left trigger is pressed switch to gear 1;
                System.out.println("Gearing Down");    
                manual = true;
                DriveGear1.set(true);
                DriveGear2.set(false);
                gear = 1;
            }else{
                manual = false;
            }
            
            
            
            
            
             if(gear == 1 && combinedSpeed > Config.GEARUP && !manual){ //Switch gear up if is in gear one and is above configured speed
                //Switch to gear 2
                DriveGear1.set(false);
                DriveGear2.set(true);
                System.out.println("Swited to gear 2! at speed of " + LeftAbsSpeed);
                gear = 2;
                //Code to slow motor when switching gears.
                modifyJoystickSpeed = Config.GEARCHANGESPEEDDEFAULT;
                rpmMatching = true;
                
            }else if(gear ==2 && combinedSpeed < Config.GEARDOWN && !manual){  //Gear down if is in gear 2 and is below configured speed
                //Switch to gear 1
                DriveGear1.set(true);
                DriveGear2.set(false);
                System.out.println("Switched to gear 1! at a speed of " + LeftAbsSpeed);
                gear = 1;
            }
                        
                        
            drive.arcadeDrive(drivestick_x, drivestick_y);
            
    }
    public void joystick2(){
        double trigger       = armStick.getRawAxis(3);
        dioButton[6] = PunchLimit.get(); //Used to stop the punch from over retracting
        SmartDashboard.putBoolean("Punch limit", dioButton[6]);
        //armstick (Joystick2)
        armButtons[1] = armStick.getRawButton(1);   //Lift arm up/down  (and DriveGear1 pickup motor)
        armButtons[2] = armStick.getRawButton(2);   //charge shooter
        armButtons[3] = armStick.getRawButton(3);   //start/stop pick up motor
        armButtons[4] = armStick.getRawButton(4);   //move to start position
        armButtons[5] = armStick.getRawButton(5); 
        armButtons[6] = armStick.getRawButton(6);   //shoot              (and lift pickup motor)
        armButtons[7] = armStick.getRawButton(7);
       
        
        if (trigger >= .5 || trigger <= -.5) {
            //If the right trigger is pressed Shoot the ball
            common.liftSet(true, ShooterLock, ShooterLockReset);
        } else {
            common.liftSet(false, ShooterLock, ShooterLockReset);
        }  
        
        
       /* BUTTON 1(A) Code */
        if (armButtons[1] && !m_armStickButtonState[1]) {
            m_armStickButtonState[1] = true;
            //Toggle piston
            //If the kick starter is not extended, toggle the tipper 
            if (!m_kickStart) {
                m_tipper_state = common.liftSet(m_tipper_state, Tipper, TipperReset);
                System.out.println("Switching tipper " + m_tipper_state);
            } else {
                System.out.println("Kick start not in right position " + m_kickStart);
            }
        } else if (!armButtons[1] && m_armStickButtonState[1]) {
            m_armStickButtonState[1] = false;
        }
        
        /*BUTTON 2(B) code*/    //Added toggle code for pickup motor & piston
        if(armButtons[2] && pickuptoggle){
            pickuptoggle = false;
            m_wheellift_state = common.liftSet(m_wheellift_state, WheelLift, WheelLiftReset);
//Un comment following code to toggle pistion and pickup motor at once
            //            if(!m_kickStart){
//                m_tipper_state = common.liftSet(m_tipper_state, Tipper, TipperReset);
//                System.out.println("Switching tipper " + m_tipper_state);
//            }else{
//                System.out.println("Kick start not in right position " + m_kickStart);
//            }
            m_wheel_pickup_state = common.motor(m_wheel_pickup_state, pickup, .7);
    
        }else if(!armButtons[2]){
            pickuptoggle = true;         
        }
        
        /* BUTTON 3(X) Code */
        if(armButtons[3] && !m_armStickButtonState[3]){
            //Change shooter
            m_charge_punch=true;
            m_armStickButtonState[3] = true;
         }else if(!armButtons[3] && m_armStickButtonState[3]){
             m_armStickButtonState[3] = false;
         }
        
        /* BUTTON 7(back) code */
        if(armButtons[7] && !m_armStickButtonState[7]){
            //toggle tipper kick start
            if(!m_tipper_state){
                
                m_kickStart = common.liftSet(m_kickStart, TipperKickStart, TipperKickStartReset);
                System.out.println("Tipper state : " + m_kickStart);
            }else{
                System.out.println("Tipper not in te right state " + m_tipper_state);
            }
            m_armStickButtonState[7] = true;
            
        }else if(!armButtons[7] && m_armStickButtonState[7]){
            m_armStickButtonState[7] = false;
        }
        if(m_charge_punch && dioButton[6]){ 
            //Charge shooter
            System.out.println("charging shooter");
            common.liftSet(false, PunchGear, PunchNeutral);
            punch.set(1); //Uncomment once direction and speed has been determined
        }else if(m_charge_punch && !dioButton[6]){
            //Shooter is fully charged
            System.out.println("Fully charged");
            punch.set(0);
            common.liftSet(true, PunchGear, PunchNeutral);
            m_charge_punch = false;
        }
    }
    public void teleopPeriodic() {  //Called durring operated control
        if (isEnabled()) {  //If the robot is enabled
            
            
            //Testing robo realm
            try{
                distance = SmartDashboard.getNumber("distance");
                SmartDashboard.putNumber("Numb from robo realm", distance);
            } catch(Exception ex) {
                ex.printStackTrace();
            }

            /* DRIVE BUTTON ONE CODE 
            if(driveButtons[1] && !m_driveStickButtonState[1]){
                System.out.println("Pressed!");
                //Set the sate of the button
                m_driveStickButtonState[1] = true;
                //Extend/Retract lifter
                state_lift = common.liftSet(state_lift, Lift, LiftReset);
                gear = 1;
            }else if(!driveButtons[1] && m_driveStickButtonState[1]){
                System.out.println("Reseting button!");
                m_driveStickButtonState[1] = false;
            }
            */
            //commented out obsolete lift code
            
            /* DRIVE BUTTON TWO CODE */
//            if(driveButtons[2]&& !m_driveStickButtonState[2]){
//                if(shooterCharged){
//                    System.out.println("Shooting ball!");
//                    System.out.println("Pressed 2");
//                    //toggle piston
//                    common.liftSet(false, ShooterLock, ShooterLockReset); //Release the piston holding back ball
//                    m_driveStickButtonState[2] = true;
//                    shooterCharged = false;
//                }else{
//                    System.out.println("SHOOTER IS NOT CHARGED!");
//                }
//            }else if(!driveButtons[2] && m_driveStickButtonState[2]){
//                System.out.println("Reset 2");
//                //Reset variable
//                m_driveStickButtonState[2] = false;
//            }
//            
//            
//            /* DRIVE BUTTON THREE CODE */
//            if(driveButtons[3]&& !m_driveStickButtonState[3]){
//                System.out.println("Pressed 3");
//                //Turn motor on/off
//                state_motor = common.motor(state_motor, pickup);
//                m_driveStickButtonState[3] = true;
//            }else if(!driveButtons[3] && m_driveStickButtonState[3]){
//                System.out.println("Reset 3");
//                //Reset variable
//                m_driveStickButtonState[3] = false;
//            }
//            /* DRIVE BUTTON FOUR CODE */
// 
//            /* DRIVE BUTTON FIVE CODE */
//            if(driveButtons[5]){
//                System.out.println("Pressed 5");
//                
//                if(!dioButton[6]){ //If the punch is not fully back
//                    System.out.println("Charging punch!!!!");
//                    PunchGear.set(true); //Switch into gear
//                    PunchNeutral.set(false);
//                    punch.set(0.6); //Turns the motor on at 60% power
//                    common.liftSet(true, ShooterLock, ShooterLockReset); //Lock the shooter in place
//                }else{
//                    System.out.println("Limit switch hit!");
//
//                    PunchGear.set(false); //Switch to nutural
//                    PunchNeutral.set(true);
//                    punch.set(0);
//                    shooterCharged = true;
//
//                }
//
//                m_driveStickButtonState[5] = true;
//            }else if(!driveButtons[5] && m_driveStickButtonState[5]){
//                System.out.println("Reset 5");
//                punch.set(0);
//                
//                //Reset variable
//                m_driveStickButtonState[5] = false;
//            }            
            
            joystick1();
            joystick2();
            SmartDashboard.putBoolean("Tipper state", m_tipper_state);
            SmartDashboard.putBoolean("Kick start state", m_kickStart);
            SmartDashboard.putBoolean("Compressor Switch", airCompressor.getPressureSwitchValue());
            SmartDashboard.putBoolean("compressor enabled", airCompressor.enabled());
            airCompressor.start();
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