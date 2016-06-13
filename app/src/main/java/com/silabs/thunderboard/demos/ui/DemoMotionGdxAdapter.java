package com.silabs.thunderboard.demos.ui;

import android.graphics.Color;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Matrix4;
import com.silabs.thunderboard.common.data.model.ThunderBoardPreferences;

public class DemoMotionGdxAdapter extends ApplicationAdapter {

    ModelBatch modelBatch;
    Environment environment;
    Camera cam;
    CameraInputController camController;
    NullInputProcessor nullInputProcessor;
    boolean swipeCamera;
    ModelInstance instance;
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
    private int assetType;

    public DemoMotionGdxAdapter(int backgroundColor, int assetType) {
        super();

        this.backgroundColor = backgroundColor;
        initMatrix = new Matrix4();
        assets = new AssetManager();
        this.assetType = assetType;

    }

    /**
     * create
     *
     * Creates the resources for the 3D display: camera, background color and object
     *
     */
    @Override
    public void create() {
        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        float fieldOfView = assetType == ThunderBoardPreferences.MODEL_TYPE_BOARD ? 1.5f : 67f;
        cam = new PerspectiveCamera(fieldOfView, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0, 0, 175f);

        cam.lookAt(0, 0, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        backgroundColorR = Color.red(backgroundColor) / 255f;
        backgroundColorG = Color.green(backgroundColor) / 255f;
        backgroundColorB = Color.blue(backgroundColor) / 255f;

        if (swipeCamera) {
            camController = new CameraInputController(cam);
            Gdx.input.setInputProcessor(camController);
        } else {
            nullInputProcessor = new NullInputProcessor();
            Gdx.input.setInputProcessor(nullInputProcessor);
        }

        initModel();
    }

    public void initModel() {

        // initMatrix is our starting position, it has to compensate for any transforms
        // in the model file we load
        if (assetType == ThunderBoardPreferences.MODEL_TYPE_BOARD) {

            initMatrix.setToRotation(0, 0, 1, 0);
            initMatrix.setToRotation(1, 0, 0, 0);
            initMatrix.setToRotation(0, 1, 0, -90);

            assets.load("data/Thunderboard_React.g3dj", Model.class);

        } else {

            initMatrix.setToRotation(0, 0, 1, 0);
            initMatrix.setToRotation(0, 1, 0, 0);
            initMatrix.setToRotation(1, 0, 0, 90);

            assets.load("data/pinewood_car.g3dj", Model.class);
        }

        loading = true;
    }

    private void doneLoading() {

        if (assetType == ThunderBoardPreferences.MODEL_TYPE_BOARD) {
            model = assets.get("data/Thunderboard_React.g3dj", Model.class);
        } else {
            model = assets.get("data/pinewood_car.g3dj", Model.class);
        }

        instance = new ModelInstance(model);
        loading = false;
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

            if (assetType == ThunderBoardPreferences.MODEL_TYPE_BOARD) {

                instance.transform.rotate(0, 1, 0, z);
                instance.transform.rotate(0, 0, 1, y);
                instance.transform.rotate(1, 0, 0, -x);

            } else {

                instance.transform.rotate(0, 0, 1, -z);
                instance.transform.rotate(0, 1, 0, y);
                instance.transform.rotate(1, 0, 0, -x);
            }
            //instance.transform.setFromEulerAngles(x, y, z);
            modelBatch.render(instance, environment);
        }
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        if (model != null) {
            model.dispose();
        }
    }

    /**
     * setOrientation
     *
     * Sets the 3D object's orientation around the x, y, and z axes.
     *
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

    ;
}
