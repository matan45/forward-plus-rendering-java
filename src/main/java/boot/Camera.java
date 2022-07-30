package boot;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static boot.Camera.CameraMovement.*;

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
    private final Vector3f position;
    private Vector3f front;
    private Vector3f up;
    private Vector3f right;
    private final Vector3f worldUp;
    // Eular Angles

    private float yaw;
    private float pitch;

    // Camera options
    private final float movementSpeed;
    private final float mouseSensitivity;
    public float zoom;

    public Camera(Vector3f position, Vector3f up, float yaw, float pitch) {
        this.position = position;
        this.up = up;
        this.yaw = yaw;
        this.pitch = pitch;
        front = new Vector3f(0.0f, 0.0f, -1.0f);
        right = new Vector3f();
        worldUp = new Vector3f();
        movementSpeed = SPEED;
        mouseSensitivity = SENSITIVTY;
        zoom = ZOOM;
        update();
    }

    // Returns the view matrix calculated using Eular Angles and the LookAt Matrix
    public Matrix4f getViewMatrix() {
        return null;
    }

    // Processes input received from any keyboard-like input system. Accepts input parameter in the form of camera defined ENUM (to abstract it from windowing systems)
    public void ProcessKeyboard(CameraMovement direction, float deltaTime) {
        float velocity = movementSpeed * deltaTime;

        if (direction == FORWARD) {
            position.add(front.x * velocity, front.y * velocity, front.z * velocity);
        }
        if (direction == BACKWARD) {
            position.sub(front.x * velocity, front.y * velocity, front.z * velocity);
        }
        if (direction == LEFT) {
            position.sub(right.x * velocity, right.y * velocity, right.z * velocity);
        }
        if (direction == RIGHT) {
            position.add(right.x * velocity, right.y * velocity, right.z * velocity);
        }
    }

    // Processes input received from a mouse input system. Expects the offset value in both the x and y direction.
    public void ProcessMouseMovement(float xOffset, float yOffset, boolean constrainPitch) {
        float xOff = xOffset * mouseSensitivity;
        float yOff = yOffset * mouseSensitivity;

        yaw += xOff;
        pitch += yOff;

        // Make sure when pitch is out of bounds, screen doesn't get flipped
        if (constrainPitch) {
            if (pitch > 89.0f) {
                pitch = 89.0f;
            }
            if (pitch < -89.0f) {
                pitch = -89.0f;
            }
        }

        // Update the camera
        update();
    }

    private void update() {
        front.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        front.y = (float) Math.sin(Math.toRadians(pitch));
        front.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        front = front.normalize();
        right = front.cross(worldUp).normalize();
        up = right.cross(front).normalize();
    }
}
