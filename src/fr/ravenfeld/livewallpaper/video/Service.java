package fr.ravenfeld.livewallpaper.video;

import rajawali.wallpaper.Wallpaper;
import android.content.Context;

public class Service extends Wallpaper {
	private Renderer mRenderer;

	@Override
	public Engine onCreateEngine() {
		mRenderer = new Renderer(this);
		return new WallpaperEngine(this.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE), getBaseContext(), mRenderer, false);
	}

	@Override
	public void onDestroy() {
		mRenderer.onSurfaceDestroyed();
		super.onDestroy();
	}
}