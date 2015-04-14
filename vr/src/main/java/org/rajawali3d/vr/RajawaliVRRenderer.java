package org.rajawali3d.vr;

import android.content.Context;
import android.opengl.GLES20;
import android.view.MotionEvent;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.util.ArrayUtils;
import org.rajawali3d.util.RajLog;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author Jared Woolston (jwoolston@tenkiv.com)
 * @author Dennis Ipple (dennis.ipple@gmail.com)
 */
public abstract class RajawaliVRRenderer extends RajawaliRenderer implements CardboardView.Renderer {
    protected final float[] mFHeadViewMatrix;
    protected final Matrix4 mHeadViewMatrix;
    private final Quaternion mCameraOrientation;
    private Eye mLastLeftEye;
    private Eye mLastRightEye;

    private GL10 mGL;
    private EGLConfig mEGLConfig;

    private final Matrix4 mScratchMatrix;

    public RajawaliVRRenderer(Context context) {
        super(context);
        mHeadViewMatrix = new Matrix4();
        mFHeadViewMatrix = new float[16];
        mCameraOrientation = new Quaternion();
        mScratchMatrix = new Matrix4();
    }

    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
        super.onRenderSurfaceCreated(config, gl, width, height);
        mGL = gl;
        mEGLConfig = config;
    }

    @Override
    protected void render(long ellapsedRealtime, double deltaTime) {
        // Render Left Eye
        RajLog.i("Left Eye View Matrix: " + Arrays.toString(mLastLeftEye.getEyeView()));
        // Calculate the left eye model-view matrix
        ArrayUtils.convertFloatsToDoubles(mLastLeftEye.getPerspective((float) getCurrentCamera().getNearPlane(),
                (float) getCurrentCamera().getFarPlane()), mScratchMatrix.getDoubleValues());
        getCurrentCamera().setProjectionMatrix(mScratchMatrix);
        getCurrentScene().render(ellapsedRealtime, deltaTime, mCurrentRenderTarget);

        // Render Right Eye
        if (mLastRightEye != null) {
            // We are in stereoscopic rendering
            RajLog.i("Right Eye View Matrix: " + Arrays.toString(mLastRightEye.getEyeView()));
            ArrayUtils.convertFloatsToDoubles(mLastRightEye.getPerspective((float) getCurrentCamera().getNearPlane(),
                (float) getCurrentCamera().getFarPlane()), mScratchMatrix.getDoubleValues());
            getCurrentCamera().setProjectionMatrix(mScratchMatrix);
            getCurrentScene().render(ellapsedRealtime, deltaTime, mCurrentRenderTarget);
        }
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
        // This is for wallpapers, so we shouldnt ever need to deal with it.
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        // We wont be receiving touch events, so do nothing.
    }

    @Override
    public void onDrawFrame(HeadTransform headTransform, Eye leftEye, Eye rightEye) {
        headTransform.getHeadView(mFHeadViewMatrix, 0);
        mHeadViewMatrix.setAll(mFHeadViewMatrix);
        mCameraOrientation.fromMatrix(mHeadViewMatrix);

        mLastLeftEye = leftEye;
        mLastRightEye = rightEye;

        // Update the camera orientation
        getCurrentCamera().setOrientation(mCameraOrientation);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Call the frame render so we can update the animations/tasks
        onRenderFrame(mGL);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        RajLog.w("CardboardView.StereoRenderer.onSurfaceChanged() called.");
        onRenderSurfaceSizeChanged(mGL, width, height);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        RajLog.w("CardboardView.StereoRenderer.onSurfaceCreated() called.");
        mEGLConfig = eglConfig;
        mGL = (GL10) ((EGL10) EGLContext.getEGL()).eglGetCurrentContext().getGL();
        onRenderSurfaceCreated(eglConfig, mGL, -1, -1);
    }

    @Override
    public void onRendererShutdown() {

    }
}
