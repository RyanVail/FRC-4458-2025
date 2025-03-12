package frc.robot.commands;

import java.util.List;

import org.littletonrobotics.junction.Logger;

import com.pathplanner.lib.util.FlippingUtil;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import frc.robot.Constants.FieldConstants;
import frc.robot.subsystems.drive.Drive;

public class AlignFeed extends AlignPose {
    public AlignFeed(Drive drive) {
        super(drive, null, AlignCamera.Front);
        super.setName("AlignFeed");
    }

    @Override
    public void initialize() {
        Pose2d pose = (DriverStation.getAlliance().orElse(Alliance.Red) == Alliance.Red)
                ? drive.getPose()
                : FlippingUtil.flipFieldPose(drive.getPose());

        Pose2d target = pose.nearest(List.of(FieldConstants.FEEDER_POSES));

        // TODO: Make consisten with the others.
        if (DriverStation.getAlliance().orElse(Alliance.Red) == Alliance.Red) {
            target = FlippingUtil.flipFieldPose(target);
        }

        Logger.recordOutput("_Pose", pose);
        Logger.recordOutput("_Nearest", target);

        setTarget(target);
        super.initialize();
    }
}
