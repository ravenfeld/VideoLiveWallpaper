package fr.ravenfeld.livewallpaper.video;

import java.io.File;
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

import com.ipaulpro.afilechooser.utils.FileUtils;

public class Renderer extends RajawaliRenderer implements
		SharedPreferences.OnSharedPreferenceChangeListener {
	private MediaPlayer mMediaPlayer;
	private VideoTexture mVideoTexture;
	private final SharedPreferences mSharedPreferences;
	private Plane screen;
	private float widthPlane;
	private Material material;

	private enum ModeRenderer {
		CLASSIC, LETTER_BOXED, STRETCHED
	}

	public Renderer(Context context) {
		super(context);

		mSharedPreferences = context.getSharedPreferences(
				Wallpaper.SHARED_PREFS_NAME, 0);
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	protected void initScene() {
		Camera2D cam = new Camera2D();
		this.replaceAndSwitchCamera(getCurrentCamera(), cam);
		getCurrentScene().setBackgroundColor(0xff040404);
		getCurrentCamera().setLookAt(0, 0, 0);
		mMediaPlayer = new MediaPlayer();

		mVideoTexture = new VideoTexture("VideoLiveWallpaper", mMediaPlayer);
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
			initMute();
			initMedia();
			initPlane();
		}
	}

	private void initMute() {
		boolean mute = mSharedPreferences.getBoolean("mute", false);
		if (mute) {
			mMediaPlayer.setVolume(0, 0);
		} else {
			mMediaPlayer.setVolume(1, 1);
		}
	}

	private void initMedia() {
		Uri uri = Uri.parse(mSharedPreferences.getString("uri", ""));

		mMediaPlayer.stop();
		mMediaPlayer.reset();

		try {
			File file = FileUtils.getFile(uri);
			if (file != null && file.isFile()) {
				mMediaPlayer.setDataSource(getContext(), uri);
			} else {
				String fileName = "android.resource://"
						+ getContext().getPackageName() + "/" + R.raw.empty;
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
	}

	private void initPlane() {
		String renderer = mSharedPreferences.getString("rendererMode",
				"classic");
		if (renderer.equalsIgnoreCase("letter_boxed")) {
			rendererMode(ModeRenderer.LETTER_BOXED);
		} else if (renderer.equalsIgnoreCase("stretched")) {
			rendererMode(ModeRenderer.STRETCHED);
		} else {
			rendererMode(ModeRenderer.CLASSIC);
		}

		screen.setMaterial(material);
		screen.setX(0f);
		screen.setY(0f);
		screen.setZ(0);
	}

	private void rendererMode(ModeRenderer modeRenderer) {
		float ratioDisplay = (float) mViewportHeight / (float) mViewportWidth;
		float ratioVideo = (float) mMediaPlayer.getVideoHeight()
				/ mMediaPlayer.getVideoWidth();

		if (ratioDisplay == ratioVideo) {
			screen.setScaleX(1f);
			screen.setScaleY(1f);
			widthPlane = 1f;
		} else if (ratioDisplay >= 1) {
			// PORTRAIT
			switch (modeRenderer) {
			case STRETCHED:
				rendererModeStretched();
				break;
			case LETTER_BOXED:
				rendererModeLetterBox();
				break;
			default:
				rendererModeClassic();
				break;
			}
		} else {
			// LANDSCAPE
			switch (modeRenderer) {
			case STRETCHED:
				rendererModeStretched();
				break;
			case LETTER_BOXED:
				rendererModeStretched();
				break;
			default:
				rendererModeStretched();
				break;
			}
		}
	}

	private void rendererModeClassic() {
		float ratioDisplay = (float) mViewportHeight / (float) mViewportWidth;
		float ratioSize = 1f / mMediaPlayer.getVideoHeight();
		widthPlane = mMediaPlayer.getVideoWidth() * ratioSize * ratioDisplay;
		screen.setScaleX(widthPlane);
		screen.setScaleY(1);
	}

	private void rendererModeLetterBox() {
		float ratioDisplay = (float) mViewportWidth / (float) mViewportHeight;
		float ratioSize = 1f / mMediaPlayer.getVideoWidth();
		screen.setScaleY(mMediaPlayer.getVideoHeight() * ratioSize
				* ratioDisplay);
		screen.setScaleX(1f);
		widthPlane = 1f;

	}

	private void rendererModeStretched() {
		float ratioDisplay = (float) mViewportHeight / (float) mViewportWidth;
		float ratioSize = 1f / mMediaPlayer.getVideoHeight();
		screen.setScaleX(mMediaPlayer.getVideoWidth() * ratioSize
				* ratioDisplay);
		screen.setScaleY(1f);
		widthPlane = 1f;
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
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		super.onSurfaceChanged(gl, width, height);
		initPlane();
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
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		initVideo();
	}

	@Override
	public void onOffsetsChanged(float xOffset, float yOffset,
			float xOffsetStep, float yOffsetStep, int xPixelOffset,
			int yPixelOffset) {

		if (screen != null) {
			screen.setX((1 - widthPlane) * (xOffset - 0.5));
		}
	}
}
