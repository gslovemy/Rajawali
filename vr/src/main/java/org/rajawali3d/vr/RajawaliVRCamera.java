/**
 * Copyright 2013 Dennis Ippel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d.vr;

import com.google.vrtoolkit.cardboard.Eye;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.util.ArrayUtils;

public class RajawaliVRCamera extends Camera {

	private final Matrix4 mLeftEyeView;
	private final Matrix4 mLeftEyePerspective;
	private final Matrix4 mRightEyeView;
	private final Matrix4 mRightEyePerspective;

	private int mCurrentEye;

	public RajawaliVRCamera() {
		super();
		mLeftEyeView = new Matrix4();
		mLeftEyePerspective = new Matrix4();
		mRightEyeView = new Matrix4();
		mRightEyePerspective = new Matrix4();
	}

	public void setTransforms(Matrix4 model, Eye left, Eye right) {
		// Copy the orientation from Cardboard
		setOrientation(mOrientation.fromMatrix(model));
		// Copy the left and right eye views
		ArrayUtils.convertFloatsToDoubles(left.getEyeView(), mLeftEyeView.getDoubleValues());
		ArrayUtils.convertFloatsToDoubles(left.getPerspective((float) mNearPlane, (float) mFarPlane), mLeftEyePerspective.getDoubleValues());
		if (right != null) {
			ArrayUtils.convertFloatsToDoubles(right.getEyeView(), mRightEyeView.getDoubleValues());
			ArrayUtils.convertFloatsToDoubles(right.getPerspective((float) mNearPlane, (float) mFarPlane), mRightEyePerspective.getDoubleValues());
		}
	}

	public void setCurrentEye(int type) {
		mCurrentEye = type;
	}

	public Matrix4 getViewMatrix() {
		synchronized (mFrustumLock) {
            mViewMatrix.setAll(getModelMatrix());
			switch (mCurrentEye) {
				case Eye.Type.LEFT:
				case Eye.Type.MONOCULAR:
					mViewMatrix.leftMultiply(mLeftEyeView);
					break;
				case Eye.Type.RIGHT:
					mViewMatrix.leftMultiply(mRightEyeView);
					break;
			}
			return mViewMatrix;
		}
	}

	public Matrix4 getProjectionMatrix() {
		synchronized (mFrustumLock) {
			switch (mCurrentEye) {
				case Eye.Type.LEFT:
				case Eye.Type.MONOCULAR:
					mProjMatrix.setAll(mLeftEyePerspective);
					break;
				case Eye.Type.RIGHT:
					mProjMatrix.setAll(mRightEyePerspective);
					break;
			}
			return mProjMatrix;
		}
	}

	public RajawaliVRCamera clone() {
		RajawaliVRCamera cam = new RajawaliVRCamera();
		cam.setFarPlane(mFarPlane);
		cam.setFieldOfView(mFieldOfView);
		cam.setGraphNode(mGraphNode, mInsideGraph);
		cam.setLookAt(mLookAt.clone());
		cam.setNearPlane(mNearPlane);
		cam.setOrientation(mOrientation.clone());
		cam.setPosition(mPosition.clone());
		cam.setProjectionMatrix(mLastWidth, mLastHeight);

		return cam;
	}
}
