package com.jscheng.scamera.render;

import static com.jscheng.scamera.util.LogUtil.TAG;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.jscheng.scamera.util.GlesUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created By Chengjunsen on 2018/8/27
 * 使用GLSurfaceView非常简单，只要实现GLSurfaceView.Renderer接口就好了，
 * 然后通过 GLSurfaceView.setRenderer(GLSurfaceView.Render renderer)方法把实现的接口传到GLSurfaceView即可
 */
public class CameraSurfaceRender implements GLSurfaceView.Renderer {

    private CameraSufaceRenderCallback mCallback;
    private RenderDrawerGroups mRenderGroups;
    private int width, height;
    private int mCameraTextureId;
    private SurfaceTexture mCameraTexture;
    private float[] mTransformMatrix;
    private long timestamp;

    public CameraSurfaceRender(Context context) {
        this.mRenderGroups = new RenderDrawerGroups(context);
        mTransformMatrix = new float[16];
    }

    // 会在Surface第一次创建或重建时调用。
    // 在GLSurfaceView attach到父View的后，此方法会被调用。
    // 从这个回调方法名我们可以大概了解这个方法的用处，即在OpenGL surface被创建时的回调。
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        mCameraTextureId = GlesUtil.createCameraTexture();
        mRenderGroups.setInputTexture(mCameraTextureId);
        mRenderGroups.create();
        initCameraTexture();
        if (mCallback != null) {
            mCallback.onCreate();
        }
    }

    public void initCameraTexture() {
        mCameraTexture = new SurfaceTexture(mCameraTextureId);
        mCameraTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                if (mCallback != null) {
                    mCallback.onRequestRender();
                }
            }
        });
    }


    // 此方法在Surface大小变化时调用，例如横屏转为竖屏、GLSurfaceView大小变化等。在Surface第一次创建时也会调用。
    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        this.width = width;
        this.height = height;
        mRenderGroups.surfaceChangedSize(width, height);
        Log.d(TAG, "currentEGLContext: " + EGL14.eglGetCurrentContext().toString());
        if (mCallback != null) {
            mCallback.onChanged(width, height);
        }
    }

    // 此方法在渲染一帧图像时调用, 图像数据哪里来？
    @Override
    public void onDrawFrame(GL10 gl10) {
        if (mCameraTexture != null) {
            mCameraTexture.updateTexImage();
            timestamp = mCameraTexture.getTimestamp();
            mCameraTexture.getTransformMatrix(mTransformMatrix);
            mRenderGroups.draw(timestamp, mTransformMatrix);
        }
        if (mCallback != null) {
            mCallback.onDraw();
        }
    }

    public SurfaceTexture getCameraSurfaceTexture() {
        return mCameraTexture;
    }

    public void setCallback(CameraSufaceRenderCallback mCallback) {
        this.mCallback = mCallback;
    }

    public void releaseSurfaceTexture() {
        if (mCameraTexture != null) {
            mCameraTexture.release();
            mCameraTexture = null;
        }
    }

    public void resumeSurfaceTexture() {
        initCameraTexture();
    }

    public void startRecord() {
        mRenderGroups.startRecord();
    }

    public void stopRecord() {
        mRenderGroups.stopRecord();
    }

    public interface CameraSufaceRenderCallback {
        void onRequestRender();
        void onCreate();
        void onChanged(int width, int height);
        void onDraw();
    }
}
