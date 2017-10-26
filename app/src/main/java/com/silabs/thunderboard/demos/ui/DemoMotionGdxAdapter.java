package com.silabs.thunderboard.demos.ui;

import static com.silabs.thunderboard.common.data.model.ThunderBoardPreferences.MODEL_TYPE_BOARD;

import android.graphics.Color;
import android.support.annotation.ColorInt;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Matrix4;
import com.silabs.thunderboard.common.app.ThunderBoardType;

import java.util.ArrayList;
import java.util.List;

public class DemoMotionGdxAdapter extends ApplicationAdapter {

    private final ArrayList<ModelInstance> instances;
    private ModelType modelType = ModelType.CAR;
    private ModelBatch modelBatch;
    private Environment environment;
    private Camera cam;
    private CameraInputController camController;
    private boolean swipeCamera;

    Model model;

    private float x;
    private float y;
    private float z;
    private AssetManager assets;
    private boolean loading;
    private int backgroundColor;
    private float backgroundColorR;
    private float backgroundColorG;
    private float backgroundColorB;
    private Matrix4 initMatrix;
    private ModelInstance instance;
    private OnSceneLoadedListener onSceneLoadedListener;
    private com.badlogic.gdx.graphics.Color ledColor = com.badlogic.gdx.graphics.Color.CLEAR;

    private List<String> materialIds = new ArrayList<String>() {{
        add("thunderboardsense_lowpoly_007:lambert28sg");
        add("thunderboardsense_lowpoly_007:lambert32sg");
        add("lambert25sg");
        add("lambert26sg");
    }};

    private enum ModelType {
        REACT,
        SENSE,
        CAR
    }

    public interface OnSceneLoadedListener {
        void onSceneLoaded();
    }

    public DemoMotionGdxAdapter(int backgroundColor, int assetType,
                                ThunderBoardType thunderBoardType) {
        super();
        this.backgroundColor = backgroundColor;
        initMatrix = new Matrix4();
        assets = new AssetManager();
        this.instances = new ArrayList<>();

        switch (thunderBoardType) {
            case THUNDERBOARD_SENSE:
                this.modelType = ModelType.SENSE;
                break;
            case THUNDERBOARD_REACT:
            default:
                if (assetType == MODEL_TYPE_BOARD) {
                    this.modelType = ModelType.REACT;
                } else {
                    this.modelType = ModelType.CAR;
                }
                break;
        }
    }

    /**
     * create
     * <p/>
     * Creates the resources for the 3D display: camera, background color and object
     */
    @Override
    public void create() {

        // TODO The pinewood model is not lit correctly using the EmissiveShader - need to
        // troubleshoot so we can use the same shader for all models
        if (ModelType.SENSE.equals(modelType)) {
            DefaultShader.Config config = new EmissiveShader.Config(
                    EmissiveShader.getDefaultVertexShader(),
                    EmissiveShader.getDefaultFragmentShader());
            modelBatch = new ModelBatch(new EmissiveShaderProvider(config));
        } else {
            modelBatch = new ModelBatch();
        }
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1.0f, 1.0f, 1.0f, 1f));
        environment.add(new DirectionalLight().set(1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 10.0f));


        float fieldOfView = (ModelType.REACT.equals(modelType) || ModelType.SENSE.equals(
                modelType)) ? 1.5f : 67f;
        cam = new PerspectiveCamera(fieldOfView, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0, 0, 175f);

        cam.lookAt(0, 0, 0);
        cam.near = 10f;
        cam.far = 300f;
        cam.update();

        backgroundColorR = Color.red(backgroundColor) / 255f;
        backgroundColorG = Color.green(backgroundColor) / 255f;
        backgroundColorB = Color.blue(backgroundColor) / 255f;

        // uncomment to enable touch control of 3dview
//        swipeCamera = true;
        if (swipeCamera) {
            camController = new CameraInputController(cam);
            Gdx.input.setInputProcessor(camController);
        } else {
            Gdx.input.setInputProcessor(new NullInputProcessor());
        }

        initModel();
    }

    private String getModelFilename() {
        switch (this.modelType) {
            case REACT:
                return "data/Thunderboard_React.g3dj";
            case SENSE:
                return "data/TBSense_Rev_Lowpoly.g3dj";
            default:
                return "data/pinewood_car.g3dj";
        }
    }

    private void initOrientation() {
        switch (this.modelType) {
            case REACT:
                initMatrix.setToRotation(0, 1, 0, -90);
                break;
            case SENSE:
                initMatrix.setToRotation(1, 0, 0, 90);
                initMatrix.scale(0.4f, 0.4f, 0.4f);
                break;
            default:
                initMatrix.setToRotation(1, 0, 0, 90);
                break;
        }
    }

    public void initModel() {
        // initMatrix is our starting position, it has to compensate for any transforms
        // in the model file we load
        assets.load(getModelFilename(), Model.class);
        initOrientation();
        loading = true;
    }

    List<Material> lightMaterials;

    private void doneLoading() {
        lightMaterials = new ArrayList<>();
        model = assets.get(getModelFilename(), Model.class);
        instance = new ModelInstance(model);
// Example of adding parts to which will get toggled when the light comes on
        if (ModelType.SENSE.equals(modelType)) {
            for (Node node : instance.nodes) {
                for (Node child : node.getChildren()) {
                    for (NodePart part : child.parts) {
                        if (part.material.id != null && materialIds.contains(part.material.id
                                .toLowerCase
                                        ())) {
                            Material material = part.material;
                            lightMaterials.add(material);
                        }
                    }
                }
            }
        }
        instances.add(instance);

        loading = false;
        if (this.onSceneLoadedListener != null) {
            this.onSceneLoadedListener.onSceneLoaded();
        }
    }

    public void setOnSceneLoadedListener(OnSceneLoadedListener onSceneLoadedListener) {
        this.onSceneLoadedListener = onSceneLoadedListener;
    }

    public void setLEDColor(@ColorInt int colorint) {
        this.ledColor = new com.badlogic.gdx.graphics.Color();
        com.badlogic.gdx.graphics.Color.argb8888ToColor(this.ledColor, colorint);
    }

    @Override
    public void render() {
        if (loading && assets.update()) {
            doneLoading();
        }
        if (camController != null) {
            camController.update();
        }

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(backgroundColorR, backgroundColorG, backgroundColorB, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);
        if (instance != null) {
            instance.transform.set(initMatrix);
//            instance.transform.translate(-100, -2, 2);
            if (ModelType.REACT.equals(modelType) || ModelType.SENSE.equals(modelType)) {
                instance.transform.rotate(0, 1, 0, z);
                instance.transform.rotate(0, 0, 1, y);
                instance.transform.rotate(1, 0, 0, -x);

            } else {

                instance.transform.rotate(0, 0, 1, -z);
                instance.transform.rotate(0, 1, 0, y);
                instance.transform.rotate(1, 0, 0, -x);
            }
        }

        modelBatch.render(instances, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        if (model != null) {
            model.dispose();
        }
        instances.clear();
        instance = null;
    }

    /**
     * setOrientation
     * <p/>
     * Sets the 3D object's orientation around the x, y, and z axes.
     * <p/>
     * Parameters are in degrees.
     *
     * @param x
     * @param y
     * @param z
     */

    public void setOrientation(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void turnOnLights() {
        for (Material mat : lightMaterials) {
            mat.set(new ColorAttribute(ColorAttribute.Emissive, this.ledColor));
        }
    }

    public void turnOffLights() {
        for (Material mat : lightMaterials) {
            if (mat.has(ColorAttribute.Emissive)) {
                mat.remove(ColorAttribute.Emissive);
            }
        }
    }


    // An input processor that doesn't do anything
    class NullInputProcessor implements InputProcessor {
        @Override
        public boolean keyDown(int i) {
            return false;
        }

        @Override
        public boolean keyUp(int i) {
            return false;
        }

        @Override
        public boolean keyTyped(char c) {
            return false;
        }

        @Override
        public boolean touchDown(int i, int i1, int i2, int i3) {
            return false;
        }

        @Override
        public boolean touchUp(int i, int i1, int i2, int i3) {
            return false;
        }

        @Override
        public boolean touchDragged(int i, int i1, int i2) {
            return false;
        }

        @Override
        public boolean mouseMoved(int i, int i1) {
            return false;
        }

        @Override
        public boolean scrolled(int i) {
            return false;
        }
    }

    class EmissiveShaderProvider extends BaseShaderProvider {
        public final EmissiveShader.Config config;

        public EmissiveShaderProvider(EmissiveShader.Config config) {
            this.config = config == null ? new EmissiveShader.Config() : config;
        }

        public EmissiveShaderProvider(String vertexShader, String fragmentShader) {
            this(new EmissiveShader.Config(vertexShader, fragmentShader));
        }

        public EmissiveShaderProvider(FileHandle vertexShader, FileHandle fragmentShader) {
            this(vertexShader.readString(), fragmentShader.readString());
        }

        public EmissiveShaderProvider() {
            this((EmissiveShader.Config) null);
        }

        protected Shader createShader(Renderable renderable) {
            return new EmissiveShader(renderable, this.config);
        }
    }
}
