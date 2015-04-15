package org.rajawali3d.vr;

import android.os.Bundle;

import com.google.vrtoolkit.cardboard.CardboardActivity;

import org.rajawali3d.IRajawaliDisplay;
import org.rajawali3d.surface.IRajawaliSurfaceRenderer;

/**
 * Activity implementation for VR applications using Rajawali. This is a wrapper of {@link CardboardActivity}
 * which adds the necessary functionality for interfacing to Rajawali and its rendering system.
 *
 * To use, extend and implement {@link #initializeViewContent()} to load a {@link RajawaliCardboardView}. You
 * may load it either via XML or by creating it programatically.
 *
 * You must also implement {@link #createRenderer()}, in which you create an {@link IRajawaliSurfaceRenderer}
 * instance.
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public abstract class RajawaliVRActivity extends CardboardActivity implements IRajawaliDisplay {

    protected RajawaliCardboardView mCardboardView;
    protected IRajawaliSurfaceRenderer mRenderer;

    /**
     * Initialize the activity view content here. It is expected that {@link #mCardboardView} will
     * not be null after this method returns.
     */
    protected abstract void initializeViewContent();

    /**
     * Called before applying {@link #mRenderer} to {@link #mCardboardView}. Anything you wish to occur
     * before final initialization of the renderer and surface must happen here. Most of the time an
     * empty implementation is sufficient.
     */
    protected abstract void onBeforeApplyRenderer();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutID());
        initializeViewContent();
        if (mCardboardView == null) throw new RuntimeException("mCardboardView must not be null after initializeViewContent() returns!");

        // Create the renderer
        mRenderer = createRenderer();
        //TODO: We should probably set the frame rate to 0 and force RENDER_WHEN_DIRTY here because of how the Cardboard API works.
        onBeforeApplyRenderer();
        applyRenderer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardboardView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCardboardView.onPause();
    }

    /**
     * Applies {@link #mRenderer} to {@link #mCardboardView} and passes {@link #mCardboardView} to
     * the Cardboard API.
     */
    private void applyRenderer() {
        mCardboardView.setSurfaceRenderer(mRenderer);
        setCardboardView(mCardboardView);
    }
}

