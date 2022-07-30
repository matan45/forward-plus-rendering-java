package boot;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    public enum CameraMovement {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT
    }

    public static final float YAW = 0.0f;
    public static final float PITCH = 0.0f;
    public static final float SPEED = 20.0f;
    public static final float SENSITIVTY = 0.25f;
    public static final float ZOOM = 45.0f;

    // Camera Attributes
    Vector3f position;
    Vector3f front;
    Vector3f up;
    Vector3f right;
    Vector3f worldUp;
    // Eular Angles

    float yaw;
    float pitch;

    // Camera options
    float movementSpeed;
    float mouseSensitivity;
    float zoom;

    public Camera(Vector3f position, Vector3f up, float yaw, float pitch) {
        this.position = position;
        this.up = up;
        this.yaw = yaw;
        this.pitch = pitch;
        update();
    }

    // Returns the view matrix calculated using Eular Angles and the LookAt Matrix
    public Matrix4f getViewMatrix() {
        return null;
    }

    // Processes input received from any keyboard-like input system. Accepts input parameter in the form of camera defined ENUM (to abstract it from windowing systems)
   public void ProcessKeyboard(CameraMovement direction, float deltaTime) {
    }

    // Processes input received from a mouse input system. Expects the offset value in both the x and y direction.
    public void ProcessMouseMovement(float xOffset, float yOffset, boolean constrainPitch) {

    }

    private void update() {

    }
}
