package org.rajawali3d.vr;

import android.content.Context;
import android.view.MotionEvent;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import org.rajawali3d.math.Matrix4;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.util.RajLog;

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
    private Eye mLastLeftEye;
    private Eye mLastRightEye;

    private GL10 mGL;
    private EGLConfig mEGLConfig;

    public RajawaliVRRenderer(Context context) {
        super(context);
        mHeadViewMatrix = new Matrix4();
        mFHeadViewMatrix = new float[16];
        // We need to use a custom camera
        getCurrentScene().replaceAndSwitchCamera(new RajawaliVRCamera(), 0);
    }

    @Override
    public RajawaliVRCamera getCurrentCamera() {
        return (RajawaliVRCamera) super.getCurrentCamera();
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
        // Calculate the left eye model-view matrix
        getCurrentCamera().setCurrentEye(mLastLeftEye.getType());
        getCurrentScene().render(ellapsedRealtime, deltaTime, mCurrentRenderTarget);

        // Render Right Eye
        if (mLastRightEye != null) {
            // We are in stereoscopic rendering
            getCurrentCamera().setCurrentEye(mLastRightEye.getType());
            getCurrentScene().render(ellapsedRealtime, deltaTime, mCurrentRenderTarget);
        }

        mLastLeftEye = null;
        mLastRightEye = null;
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
        //mCameraOrientation.fromMatrix(mHeadViewMatrix);
        //getCurrentCamera().setOrientation(mCameraOrientation);

        mLastLeftEye = leftEye;
        mLastRightEye = rightEye;

        // Update the camera orientation
        getCurrentCamera().setTransforms(mHeadViewMatrix, leftEye, rightEye);

        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

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
