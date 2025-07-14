package com.cyao.animatedLogo.util;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import java.util.concurrent.atomic.AtomicBoolean;

public class DoneLineListen implements LineListener {
    private final AtomicBoolean playing;

    public DoneLineListen(AtomicBoolean playing) {
        this.playing = playing;
    }

    @Override
    public void update(LineEvent event) {
        if(event.getType() == LineEvent.Type.STOP) {
            playing.set(false);
        }
    }
}
