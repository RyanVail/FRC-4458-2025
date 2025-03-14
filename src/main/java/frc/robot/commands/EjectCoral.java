package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.Robot;
import frc.robot.Constants.IntakeConstants;
import frc.robot.subsystems.intake.Intake;

public class EjectCoral extends Command {
    Intake intake;
    double senseTime;

    public EjectCoral(Intake intake) {
        super.addRequirements(intake);
        this.intake = intake;
    }

    @Override
    public void initialize() {
        senseTime = 0;
        intake.setVoltage(IntakeConstants.CORAL_SCORE_VOLTAGE);

        Commands.print("Eject Coral inited").schedule();
    }

    @Override
    public void execute() {
        if (senseTime == 0 && !intake.hasCoral()) {
            senseTime = System.currentTimeMillis();
        }
    }

    @Override
    public boolean isFinished() {
        if (senseTime == 0)
            return false;

        return Robot.isSimulation()
                ? true
                : ((System.currentTimeMillis() - senseTime) * 0.001) >= IntakeConstants.CORAL_EJECT_TIME;
    }

    @Override
    public void end(boolean interrupted) {
        intake.setVoltage(0);
    }
}
