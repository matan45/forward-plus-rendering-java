package boot;

import org.joml.Random;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {
    public static final Vector2f SCREEN_SIZE = new Vector2f(1920, 1080);
    public static final int NUM_LIGHTS = 1024;
    public static final float LIGHT_RADIUS = 30.0f;
    public static final float NEAR_PLANE = 0.1f;
    public static final float FAR_PLANE = 300.0f;

    // Defines exposure level for HDR lighting
    public static final float exposure = 1.0f;

    // Constants for light animations
    public static final Vector3f LIGHT_MIN_BOUNDS = new Vector3f(-135.0f, -20.0f, -60.0f);
    public static final Vector3f LIGHT_MAX_BOUNDS = new Vector3f(135.0f, 170.0f, 60.0f);
    public static final float LIGHT_DELTA_TIME = -0.6f;

    long gWindowPtr;

    // For drawing our 1 x 1 quad
    int quadVAO = 0;
    int quadVBO;

    float lastX = 400.0f, lastY = 300.0f;
    float deltaTime = 0.0f;
    float lastFrame = 0.0f;

    // Used for storage buffer objects to hold light data and visible light indices data
    int lightBuffer = 0;
    int visibleLightIndicesBuffer = 0;

    // structures defining the data of both buffers
    public static class PointLight {
        Vector4f color;
        Vector4f position;
        Vector4f paddingAndRadius;
    }

    public static class VisibleIndex {
        int index;
    }

    // X and Y work group dimension variables for compute shader
    int workGroupsX = 0;
    int workGroupsY = 0;

    // Camera object
    Camera camera = new Camera(new Vector3f(-40.0f, 10.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f));

    // Creates window and initializes GLFW
    void InitGLFW() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);

        gWindowPtr = glfwCreateWindow((int) SCREEN_SIZE.x, (int) SCREEN_SIZE.y, "Forward+ Renderer", NULL, NULL);
        if (gWindowPtr == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwMakeContextCurrent(gWindowPtr);
        GL.createCapabilities();

        // Enable any OpenGL features we want to use
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glEnable(GL_CULL_FACE);
        glEnable(GL_MULTISAMPLE);

        // Set mouse and keyboard callback functions
        glfwSetKeyCallback(gWindowPtr, this::KeyCallback);
        glfwSetCursorPosCallback(gWindowPtr, this::MouseCallback);
        glfwSetInputMode(gWindowPtr, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    // Initializes buffers and scene data
    void InitScene() {
        // Define work group sizes in x and y direction based off screen size and tile size (in pixels)
        workGroupsX = (int) ((SCREEN_SIZE.x + (SCREEN_SIZE.x % 16)) / 16);
        workGroupsY = (int) ((SCREEN_SIZE.y + (SCREEN_SIZE.y % 16)) / 16);
        int numberOfTiles = workGroupsX * workGroupsY;

        // Generate our shader storage buffers
        int lightBuffer = glGenBuffers();
        int visibleLightIndicesBuffer = glGenBuffers();

        // Bind light buffer
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, lightBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, NUM_LIGHTS * 12L * Float.BYTES, GL_DYNAMIC_DRAW);

        // Bind visible light indices buffer
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, visibleLightIndicesBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, numberOfTiles * Integer.BYTES * 1024L, GL_STATIC_DRAW);

        // Set the default values for the light buffer
        SetupLights();

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    // Returns a random position in the scene confined to the lightMinBounds and lightMaxBounds
    Vector3f RandomPosition() {
        Random rand = new Random();
        Vector3f position = new Vector3f();
        for (int i = 0; i < 3; i++) {
            float min = LIGHT_MIN_BOUNDS.get(i);
            float max = LIGHT_MAX_BOUNDS.get(i);
            position.setComponent(i, rand.nextFloat() * (max - min) + min);
        }
        return position;
    }

    // Fills the lightBuffer with lights in random positions and colors
    void SetupLights() {
    }

    // Updates light position based on lightDeltaTime. Called each frame
    void UpdateLights() {
    }

    // Mouse and keyboard callback functions
    void Movement() {
    }

    void KeyCallback(long window, int key, int scanCode, int action, int mods) {
    }

    void MouseCallback(long window, double x, double y) {
    }

    // Based on function from LearnOpenGL: http://www.learnopengl.com
// Draw a 1 x 1 quad in NDC. We use it to render framebuffer color targets and post-processing effects
    void DrawQuad() {
    }
}
