package com.silabs.thunderboard.demos.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.AmbientCubemap;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import timber.log.Timber;

/**
 * Created by james.ayvaz on 5/17/16.
 */
public class EmissiveShader extends DefaultShader {

    public EmissiveShader(Renderable renderable) {
        super(renderable);
    }

    public EmissiveShader(Renderable renderable, Config config) {
        super(renderable, config);
    }

    public EmissiveShader(Renderable renderable, Config config, String prefix) {
        super(renderable, config, prefix);
    }

    public EmissiveShader(Renderable renderable, Config config, String prefix, String vertexShader, String fragmentShader) {
        super(renderable, config, prefix, vertexShader, fragmentShader);
    }

    public EmissiveShader(Renderable renderable, Config config, ShaderProgram shaderProgram) {
        super(renderable, config, shaderProgram);
    }


    private static String defaultVertexShader = null;
    public final static String getDefaultVertexShader () {
        if (defaultVertexShader == null) {
            defaultVertexShader = Gdx.files.internal("data/default.vertex.glsl").readString();
        }
        return defaultVertexShader;
    }

    private static String defaultFragmentShader = null;
    public final static String getDefaultFragmentShader () {
        if (defaultFragmentShader == null) {
            defaultFragmentShader = Gdx.files.internal("data/default.fragment.glsl").readString();
        }
        return defaultFragmentShader;
    }
}

