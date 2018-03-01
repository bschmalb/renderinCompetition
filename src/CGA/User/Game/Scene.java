package CGA.User.Game;

import CGA.Framework.GameWindow;
import CGA.Framework.OBJLoader;
import CGA.User.DataStructures.*;
import CGA.User.DataStructures.Camera.FlyCamera;
import CGA.User.DataStructures.Geometry.Mesh;
import CGA.User.DataStructures.Geometry.Renderable;
import CGA.User.DataStructures.Geometry.Transformable;
import CGA.User.DataStructures.Geometry.VertexAttribute;
import CGA.User.DataStructures.Light.PointLight;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Fabian on 16.09.2017.
 */
public class Scene {
    //Mesh mesh;
    private Shader shader;
    private GameWindow window;

    private Renderable orb, ground, ruin;

    //Lights
    private PointLight orb_light;

    private Texture2D orb_diff, orb_spec, orb_emit;
    private Texture2D ground_diff, ground_spec, ground_emit;
    private Texture2D ruin_diff, ruin_spec, ruin_emit;
    private Texture2D flashlighttex;

    //camera
    private FlyCamera camera;
    private double oldMouseX, oldMouseY;
    private boolean firstMouseMove;

    private float flashlight;

    public Scene(GameWindow window) {
        this.window = window;
        firstMouseMove = true;
    }

    //scene setup
    public boolean init() {
        try {
            //Load shader
            shader = new Shader("assets/shaders/vertex.glsl", "assets/shaders/fragment.glsl");

            //load textures
            orb_diff = new Texture2D("assets/textures/orb_diff.png", true);
            orb_diff.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
            orb_spec = new Texture2D("assets/textures/orb_spec.png", true);
            orb_spec.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
            orb_emit = new Texture2D("assets/textures/orb_emit.png", true);
            orb_emit.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);

            ground_diff = new Texture2D("assets/textures/ground_diff.png", true);
            ground_diff.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
            ground_spec = new Texture2D("assets/textures/ground_spec.png", true);
            ground_spec.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
            ground_emit = new Texture2D("assets/textures/ground_emit.png", true);
            ground_emit.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);

            ruin_diff = new Texture2D("assets/textures/ruin_diff.png", true);
            ruin_diff.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
            ruin_spec = new Texture2D("assets/textures/ruin_spec.png", true);
            ruin_spec.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
            ruin_emit = new Texture2D("assets/textures/ruin_emit.png", true);
            ruin_emit.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);

            flashlighttex = new Texture2D("assets/textures/flashlight.png", true);
            flashlighttex.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
            flashlight = 0.0f;


            //load an object and create a mesh
            OBJLoader.OBJResult res = OBJLoader.loadOBJ("assets/models/sphere.obj", true, true);
            OBJLoader.OBJResult gres = OBJLoader.loadOBJ("assets/models/ground.obj", false, false);
            OBJLoader.OBJResult rres = OBJLoader.loadOBJ("assets/models/ruin.obj", false, false);


            //Create the mesh
            VertexAttribute[] vertexAttributes = new VertexAttribute[3];
            int stride = 8 * 4;
            vertexAttributes[0] = new VertexAttribute(3, GL_FLOAT, stride, 0);      //position attribute
            vertexAttributes[1] = new VertexAttribute(2, GL_FLOAT, stride, 3 * 4);  //texture coordinate attribut
            vertexAttributes[2] = new VertexAttribute(3, GL_FLOAT, stride, 5 * 4);  //normal attribute

            //Create renderable
            orb = new Renderable();

            for (OBJLoader.OBJMesh m : res.objects.get(0).meshes) {
                Mesh mesh = new Mesh(m.getVertexData(), m.getIndexData(), vertexAttributes, orb_diff, orb_spec, orb_emit, 20.0f);
                orb.meshes.add(mesh);
            }

            orb.scaleLocal(new Vector3f(0.2f));

            ground = new Renderable();

            for (OBJLoader.OBJMesh m : gres.objects.get(0).meshes) {
                Mesh mesh = new Mesh(m.getVertexData(), m.getIndexData(), vertexAttributes, ground_diff, ground_spec, ground_emit, 20.0f);
                ground.meshes.add(mesh);
            }

            ruin = new Renderable();

            for (OBJLoader.OBJMesh m : rres.objects.get(0).meshes) {
                OBJLoader.recalculateNormals(m);
                Mesh mesh = new Mesh(m.getVertexData(), m.getIndexData(), vertexAttributes, ruin_diff, ruin_spec, ruin_emit, 20.0f);
                ruin.meshes.add(mesh);
            }

            //ruin.scaleLocal(new Vector3f(0.1f));
            ground.scaleLocal(new Vector3f(0.1f));
            ground.rotateLocal(new Vector3f(0.0f, 1.0f, 0.0f), (float) Math.toRadians(-90.0f));

            //light setup

            orb_light = new PointLight(new Vector3f(1.0f, 1.0f, 160.0f / 255.0f), new Vector3f(0.3f, 1.7f, 1.6f));

            //setup camera
            camera = new FlyCamera(
                    window.getFramebufferWidth(),
                    window.getFramebufferHeight(),
                    (float) Math.toRadians(90.0),
                    0.1f,
                    100.0f
            );

            //move camera a little bit in z direction
            camera.translateGlobal(new Vector3f(0.0f, 2.0f, 6.0f));


            //initial opengl state
            shader.use();
            glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

            glEnable(GL_CULL_FACE);
            glFrontFace(GL_CCW);
            glCullFace(GL_BACK);

            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LESS);
            // glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            return true;
        } catch (Exception ex) {
            System.err.println("Scene initialization failed:\n" + ex.getMessage() + "\n");
            ex.printStackTrace();
            return false;
        }

    }

public void render(float dt) {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    shader.use();
    flashlighttex.bind(3);
    shader.setUniform("flashlightTex", 3);
    shader.setUniform("flashlightFactor", flashlight);
    shader.setUniform("screensize", new Vector2f((float) window.getFramebufferWidth(), (float) window.getFramebufferHeight()));
    shader.setUniform("view_matrix", camera.getViewMatrix(), false);
    shader.setUniform("proj_matrix", camera.getProjectionMatrix(), false);
    orb_light.bind(shader, "light");
    shader.setUniform("uvMultiplier", 1.0f);
    orb.render(shader, new Transformable[]{orb_light});
    shader.setUniform("uvMultiplier", 10.0f);
    ground.render(shader);
    shader.setUniform("uvMultiplier", 5.0f);
    ruin.render(shader, new Transformable[]{ground});
}

    public void update(float dt) {
        //camera update
        float movemul = 0.75f;
        if (window.getKeyState(GLFW_KEY_W)) {
            camera.forward(movemul * dt);
        }
        if (window.getKeyState(GLFW_KEY_A)) {
            camera.left(movemul * dt);
        }
        if (window.getKeyState(GLFW_KEY_S)) {
            camera.backward(movemul * dt);
        }
        if (window.getKeyState(GLFW_KEY_D)) {
            camera.right(movemul * dt);
        }
        if (window.getKeyState(GLFW_KEY_C)) {
            camera.down(movemul * dt);
        }
        if (window.getKeyState(GLFW_KEY_SPACE)) {
            camera.up(movemul * dt);
        }

        if (window.getKeyState(GLFW_KEY_UP)) {
            orb_light.translateGlobal(new Vector3f(0.0f, 0.0f, -1.0f * dt));
        }
        if (window.getKeyState(GLFW_KEY_DOWN)) {
            orb_light.translateGlobal(new Vector3f(0.0f, 0.0f, 1.0f * dt));
        }
        if (window.getKeyState(GLFW_KEY_LEFT)) {
            orb_light.translateGlobal(new Vector3f(-1.0f * dt, 0.0f, 0.0f));
        }
        if (window.getKeyState(GLFW_KEY_RIGHT)) {
            orb_light.translateGlobal(new Vector3f(1.0f * dt, 0.0f, 0.0f));
        }

        if (window.getKeyState(GLFW_KEY_I)) {
            orb_light.translateGlobal(new Vector3f(0.0f, 1.0f * dt, 0.0f));
        }
        if (window.getKeyState(GLFW_KEY_K)) {
            orb_light.translateGlobal(new Vector3f(0.0f, -1.0f * dt, 0.0f));
        }
    }

    public void onKey(int key, int scancode, int action, int mode) {
        if (key == GLFW_KEY_L && action == GLFW_PRESS) {
            flashlight = flashlight == 0.0f ? 1.0f : 0.0f;
        }
    }

    public void onMouseMove(double xpos, double ypos) {
        if (!firstMouseMove) {
            float yawangle = (float) (xpos - oldMouseX) * 0.002f;
            float pitchangle = (float) (ypos - oldMouseY) * 0.002f;
            camera.rotateView(-yawangle, -pitchangle);
        } else
            firstMouseMove = false;
        oldMouseX = xpos;
        oldMouseY = ypos;
    }

    public void cleanup() {}
}
