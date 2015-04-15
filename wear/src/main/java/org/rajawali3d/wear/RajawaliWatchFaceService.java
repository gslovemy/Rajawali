package org.rajawali3d.wear;

import android.support.wearable.watchface.Gles2WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.SurfaceHolder;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

public abstract class RajawaliWatchFaceService extends Gles2WatchFaceService {

    @Override
    public final Engine onCreateEngine() {
        return getRajawaliWatchEngine();
    }

    protected abstract Engine getRajawaliWatchEngine();

    protected abstract class RajawaliWatchEngine extends Gles2WatchFaceService.Engine {

        private GL10 gl10;
        private RajawaliWatchRenderer renderer;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setWatchFaceStyle(getWatchFaceStyle());
        }

        protected abstract WatchFaceStyle getWatchFaceStyle();

        protected abstract RajawaliWatchRenderer getRenderer();

        @Override
        public void onGlContextCreated() {
            super.onGlContextCreated();

            renderer = getRenderer();
        }

        @Override
        public void onGlSurfaceCreated(int width, int height) {
            super.onGlSurfaceCreated(width, height);
            gl10 = (GL10) ((EGL10) EGLContext.getEGL()).eglGetCurrentContext().getGL();
            renderer.create();
            renderer.onRenderSurfaceSizeChanged(gl10, width, height);
        }

        @Override
        public void onDraw() {
            super.onDraw();
            renderer.onRenderFrame(gl10);

            // Draw every frame as long as we're visible and in interactive mode.
            if (isVisible() && !isInAmbientMode()) {
                invalidate();
            }
        }

        @Override
        public void onDestroy() {
            renderer.onRenderSurfaceDestroyed(null);
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                renderer.onResume();
            } else {
                renderer.onPause();
            }
            invalidate();
        }
    }

}
