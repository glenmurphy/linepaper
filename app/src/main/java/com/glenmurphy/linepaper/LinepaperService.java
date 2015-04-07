package com.glenmurphy.linepaper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by glen on 4/4/15.
 * Mostly derived from http://code.tutsplus.com/tutorials/create-a-live-wallpaper-on-android-using-an-animated-gif--cms-23088
 */
public class LinepaperService extends WallpaperService {
  @Override
  public WallpaperService.Engine onCreateEngine() {
    return new LinepaperEngine();
  }

  private class LinepaperEngine extends WallpaperService.Engine {
    private final int FRAME_DURATION = 200;
    static final int BACKGROUND_COLOR = 0xff263238;
    static final int BAR_DIVISION = 14; // division by screen width (higher is smaller).
    static final float OFFSET_INCREMENT = -0.5f;

    private SurfaceHolder holder;
    private boolean visible;
    private Handler handler;

    private Paint foreground;
    private float offset = 0;

    public LinepaperEngine() {
      handler = new Handler();

      foreground = new Paint();
      foreground.setColor(0xff37474F);
      foreground.setAntiAlias(true);
    }

    @Override
    public void onCreate(SurfaceHolder surfaceHolder) {
      super.onCreate(surfaceHolder);
      this.holder = surfaceHolder;
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
      this.visible = visible;
      if (visible)
        handler.post(drawLines);
      else
        handler.removeCallbacks(drawLines);
    }

    @Override
    public void onDestroy() {
      super.onDestroy();
      handler.removeCallbacks(drawLines);
    }

    private Runnable drawLines = new Runnable() {
      public void run() {
        draw();
      }
    };

    private void draw() {
      if (!visible) return;
      Canvas canvas = holder.lockCanvas();
      canvas.drawColor(BACKGROUND_COLOR);

      int width = canvas.getWidth();
      int height = canvas.getHeight();
      int maxDimension = Math.max(width, height);
      int barWidth = Math.min(width, height) / BAR_DIVISION;
      int numBars = (int)(maxDimension * 1.4f / barWidth / 2f);
      int barLength = (int)(maxDimension * 1.4f);

      Calendar cal = GregorianCalendar.getInstance();
      cal.setTime(new Date());
      /*
      float angle = cal.get(Calendar.HOUR_OF_DAY) * 30f +  // 12 * 30 = 360  (degrees per day)
                    cal.get(Calendar.MINUTE) / 2f +        // 60 / 2 = 30    (degrees per hour)
                    cal.get(Calendar.SECOND) / 120f;       // 60 / 120 = 0.5 (degrees per minute)
       */
      float angle = cal.get(Calendar.MINUTE) * 6f +         // 60 * 6 = 360       (degrees per hour)
                    cal.get(Calendar.SECOND) / 10f +        // 60 / 10 = 6        (degrees per minute)
                    cal.get(Calendar.MILLISECOND) / 10000f; // 1000 / 10000 = 0.1 (degrees per second)

      offset = (offset + OFFSET_INCREMENT) % (barWidth * 2f);

      canvas.save();
      canvas.translate(width / 2, height / 2);
      canvas.rotate(angle);

      for (int i = -numBars / 2; i < numBars / 2; i++) {
        canvas.save();
        canvas.translate(i * barWidth * 2 + offset, -barLength / 2);
        float percentage = (i + numBars / 2) / (float)numBars;
        //foreground.setAlpha(96 + (int)((i + numBars / 2) / (float)numBars * 128));
        foreground.setARGB(255,
           (int)(0x26 + (0x37 - 0x26) * (0.25 + percentage * 0.75)),
           (int)(0x32 + (0x47 - 0x32) * (0.25 + percentage * 0.75)),
           (int)(0x38 + (0x4F - 0x38) * (0.25 + percentage * 0.75)));
        canvas.drawRect(0, 0, barWidth, barLength, foreground);
        canvas.restore();
      }
      canvas.restore();

      holder.unlockCanvasAndPost(canvas);
      handler.removeCallbacks(drawLines);
      handler.postDelayed(drawLines, FRAME_DURATION);
    }
  }
}
