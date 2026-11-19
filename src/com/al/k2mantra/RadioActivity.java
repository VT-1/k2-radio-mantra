package com.al.k2mantra;

import android.app.Activity;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class RadioActivity extends Activity {

    private static final String STREAM_URL = "https://c22.radioboss.fm:8291/stream";

    private MediaPlayer mp;
    private AudioManager am;
    private boolean wantPlay = false;
    private boolean ready = false;
    private boolean connecting = false;
    private boolean resumeOnGain = false;
    private int retries = 0;
    private ImageButton btn;
    private TextView status;
    private final Handler main = new Handler(Looper.getMainLooper());

    private final AudioManager.OnAudioFocusChangeListener focus =
            new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int change) {
                    if (change == AudioManager.AUDIOFOCUS_LOSS
                            || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        if (wantPlay) { resumeOnGain = true; stopAudio(); }
                    } else if (change == AudioManager.AUDIOFOCUS_GAIN) {
                        if (wantPlay) startAudio();
                        else if (resumeOnGain) { resumeOnGain = false; startAudio(); }
                    }
                }
            };

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.main);
        btn = (ImageButton) findViewById(R.id.btnPlay);
        status = (TextView) findViewById(R.id.status);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { toggle(); }
        });
        btn.requestFocus();
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        am.requestAudioFocus(focus, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        // TV (Xiaomi) quirk: after losing focus to another app and re-requesting it,
        // the GAIN callback may not be redelivered — resume ourselves.
        if (ready && (wantPlay || resumeOnGain)) {
            resumeOnGain = false;
            startAudio();
        }
    }

    @Override
    protected void onDestroy() {
        try { am.abandonAudioFocus(focus); } catch (Exception ignored) {}
        releaseMp();
        super.onDestroy();
    }

    private void connect() {
        releaseMp();
        connecting = true;
        wantPlay = true;
        ready = false;
        setIcon(false);
        status.setText(R.string.connecting);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final MediaPlayer[] ph = new MediaPlayer[1];
                try {
                    ph[0] = new MediaPlayer();
                    ph[0].setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build());
                    ph[0].setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer m) {
                            main.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mp != ph[0]) return;
                                    connecting = false;
                                    ready = true;
                                    retries = 0;
                                    startAudio();
                                }
                            });
                        }
                    });
                    ph[0].setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer m, int what, int extra) {
                            main.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mp != ph[0]) return;
                                    handleError();
                                }
                            });
                            return true;
                        }
                    });
                    ph[0].setDataSource(STREAM_URL);
                    ph[0].prepareAsync();
                    mp = ph[0];
                } catch (Exception e) {
                    main.post(new Runnable() {
                        @Override
                        public void run() {
                            if (ph[0] != null) { try { ph[0].release(); } catch (Exception ignored) {} }
                            handleError();
                        }
                    });
                }
            }
        }).start();
    }

    private void toggle() {
        if (connecting) return;
        if (mp == null) { retries = 0; connect(); return; }
        if (wantPlay) stopAudio(); else startAudio();
    }

    private void handleError() {
        connecting = false;
        releaseMp();
        setIcon(false);
        if (wantPlay && retries < 8) {
            retries++;
            status.setText(R.string.reconnecting);
            main.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (wantPlay && mp == null && !connecting) connect();
                }
            }, 1500);
        } else {
            wantPlay = false;
            status.setText(R.string.error);
        }
    }

    private void startAudio() {
        if (mp != null && ready) {
            try { mp.start(); } catch (Exception ignored) {}
        }
        wantPlay = true;
        setIcon(true);
        status.setText(R.string.playing);
    }

    private void stopAudio() {
        if (mp != null && ready) {
            try { mp.pause(); } catch (Exception ignored) {}
        }
        wantPlay = false;
        setIcon(false);
        status.setText(R.string.paused);
    }

    private void setIcon(boolean playing) {
        btn.setImageResource(playing ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    private void releaseMp() {
        ready = false;
        if (mp != null) {
            try { mp.reset(); } catch (Exception ignored) {}
            try { mp.release(); } catch (Exception ignored) {}
            mp = null;
        }
    }
}
