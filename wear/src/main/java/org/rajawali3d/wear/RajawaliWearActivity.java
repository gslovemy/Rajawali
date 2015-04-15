package org.rajawali3d.wear;

import android.app.Activity;
import android.os.Bundle;

import org.rajawali3d.IRajawaliDisplay;
import org.rajawali3d.surface.IRajawaliSurfaceRenderer;
import org.rajawali3d.surface.RajawaliSurfaceView;

public abstract class RajawaliWearActivity extends Activity implements IRajawaliDisplay {

    protected RajawaliSurfaceView mRajawaliSurfaceView;
    protected IRajawaliSurfaceRenderer mRenderer;

    /**
     * Initialize the activity view content here. It is expected that {@link #mRajawaliSurfaceView} will
     * not be null after this method returns.
     */
    protected abstract void initializeViewContent();

    /**
     * Called before applying {@link #mRenderer} to {@link #mRajawaliSurfaceView}. Anything you wish to occur
     * before final initialization of the renderer and surface must happen here. Most of the time an
     * empty implementation is sufficient.
     */
    protected abstract void onBeforeApplyRenderer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeViewContent();
        if (mRajawaliSurfaceView == null) throw new RuntimeException("mCardboardView must not be null after initializeViewContent() returns!");
        addContentView(mRajawaliSurfaceView, null);

        // Create the renderer
        mRenderer = createRenderer();
        onBeforeApplyRenderer();
        applyRenderer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRajawaliSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRajawaliSurfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRenderer.onRenderSurfaceDestroyed(null);
    }

    /**
     * Applies {@link #mRenderer} to {@link #mRajawaliSurfaceView}.
     */
    private void applyRenderer() {
        mRajawaliSurfaceView.setSurfaceRenderer(mRenderer);
    }
}
