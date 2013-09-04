package fr.ravenfeld.livewallpaper.video;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import rajawali.Camera2D;
import rajawali.materials.Material;
import rajawali.materials.textures.ATexture.TextureException;
import rajawali.materials.textures.VideoTexture;
import rajawali.primitives.Plane;
import rajawali.renderer.RajawaliRenderer;
import rajawali.wallpaper.Wallpaper;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Display;
import android.view.WindowManager;

public class Renderer extends RajawaliRenderer implements SharedPreferences.OnSharedPreferenceChangeListener {
	private MediaPlayer mMediaPlayer;
	private VideoTexture mVideoTexture;
	private final int mDisplayWidth;
	private final int mDisplayHeight;
	private final SharedPreferences mSharedPreferences;
	private Plane screen;
	private int position;
	private float widthPlane;
	private Material material;

	public Renderer(Context context) {
		super(context);
		Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		mSharedPreferences = context.getSharedPreferences(Wallpaper.SHARED_PREFS_NAME, 0);
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
		mDisplayWidth = display.getWidth();
		mDisplayHeight = display.getHeight();

	}

	@Override
	protected void initScene() {
		Camera2D cam = new Camera2D();
		this.replaceAndSwitchCamera(getCurrentCamera(), cam);
		getCurrentScene().setBackgroundColor(0xff040404);
		getCurrentCamera().setLookAt(0, 0, 0);
		mMediaPlayer = new MediaPlayer();

		mVideoTexture = new VideoTexture("sintelTrailer", mMediaPlayer);
		material = new Material();
		try {
			material.addTexture(mVideoTexture);
		} catch (TextureException e) {
			e.printStackTrace();
		}
		screen = new Plane(1f, 1f, 1, 1);
		initVideo();
		addChild(screen);
		if (mMediaPlayer != null) {
			mMediaPlayer.start();
		}
	}

	private void initVideo() {
		if (mMediaPlayer != null) {
			String file = mSharedPreferences.getString("uri", "");
			boolean mute = mSharedPreferences.getBoolean("mute", false);
			String renderer = mSharedPreferences.getString("rendererMode", "classic");

			mMediaPlayer.stop();
			mMediaPlayer.reset();
			if (mute) {
				mMediaPlayer.setVolume(0, 0);
			} else {
				mMediaPlayer.setVolume(1, 1);
			}
			try {
				if (!file.equalsIgnoreCase("")) {

					mMediaPlayer.setDataSource(getContext(), Uri.parse(file));

				} else {

					String fileName = "android.resource://" + getContext().getPackageName() + "/" + R.raw.empty;
					mMediaPlayer.setDataSource(getContext(), Uri.parse(fileName));
				}

				mMediaPlayer.setLooping(true);
				mMediaPlayer.prepare();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (renderer.equalsIgnoreCase("letter_boxed")) {
				rendererModeLetterBox();
			} else if (renderer.equalsIgnoreCase("stretched")) {
				rendererModeStretched();
			} else {
				rendererModeClassic();
			}

			screen.setMaterial(material);
			screen.setX(0f);
			screen.setY(0f);
			screen.setZ(0);
		}
	}

	private void rendererModeClassic() {
		float ratioDisplay = (float) mDisplayHeight / (float) mDisplayWidth;
		float ratioVideo = (float) mMediaPlayer.getVideoHeight() / mMediaPlayer.getVideoWidth();

		if (ratioDisplay == ratioVideo) {

			screen.setScaleX(1f);
			screen.setScaleY(1f);
			widthPlane = 1f;
		} else {
			float ratioSize = 1f / mMediaPlayer.getVideoHeight();
			widthPlane = mMediaPlayer.getVideoWidth() * ratioSize * ratioDisplay;
			screen.setScaleX(widthPlane);
			screen.setScaleY(1);
		}
	}

	private void rendererModeLetterBox() {

		float ratioDisplay = (float) mDisplayWidth / (float) mDisplayHeight;
		float ratioVideo = (float) mMediaPlayer.getVideoWidth() / mMediaPlayer.getVideoHeight();

		if (ratioDisplay == ratioVideo) {

			screen.setScaleX(1f);
			screen.setScaleY(1f);
			widthPlane = 1f;
		} else {
			float ratioSize = 1f / mMediaPlayer.getVideoWidth();
			screen.setScaleY(mMediaPlayer.getVideoHeight() * ratioSize * ratioDisplay);
			screen.setScaleX(1f);
			widthPlane = 1f;

		}
	}

	private void rendererModeStretched() {
		float ratioDisplay = (float) mDisplayHeight / (float) mDisplayWidth;
		float ratioVideo = (float) mMediaPlayer.getVideoHeight() / mMediaPlayer.getVideoWidth();

		if (ratioDisplay == ratioVideo) {

			screen.setScaleX(1f);
			screen.setScaleY(1f);
			widthPlane = 1f;
		} else {
			float ratioSize = 1f / mMediaPlayer.getVideoHeight();
			screen.setScaleX(mMediaPlayer.getVideoWidth() * ratioSize * ratioDisplay);
			screen.setScaleY(1f);
			widthPlane = 1f;

		}
	}

	@Override
	public void onDrawFrame(GL10 glUnused) {
		super.onDrawFrame(glUnused);
		mVideoTexture.update();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		super.onSurfaceCreated(gl, config);
	}

	@Override
	public void onVisibilityChanged(boolean visible) {
		super.onVisibilityChanged(visible);
		if (!visible) {
			if (mMediaPlayer != null) {
				mMediaPlayer.pause();
			}

		} else if (mMediaPlayer != null) {
			mMediaPlayer.start();
		}
	}

	@Override
	public void onSurfaceDestroyed() {
		super.onSurfaceDestroyed();
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
				mMediaPlayer.release();
			}
		}

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		initVideo();
	}

	@Override
	public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

		if (screen != null) {
			screen.setX((1 - widthPlane) * (xOffset - 0.5));
		}
	}
}
