package jlu.evyde;

import me.tongfei.progressbar.ProgressBar;

import java.math.BigInteger;

public class ProgressBarWrapper {
    private final ProgressBar bar;
    public ProgressBarWrapper(String name, long initMax, boolean detail) {
        if (detail) {
            bar = new ProgressBar(name, initMax);
        } else {
            bar = null;
        }
    }

    public ProgressBarWrapper(String getting_frequency, Bits contentLength, boolean detail) {
        this(getting_frequency, new BigInteger(contentLength.toString(), 2).longValue(), detail);
    }

    public void step() {
        if (this.bar != null) {
            bar.step();
        }
    }

    public void step(long n) {
        if (this.bar != null) {
            bar.stepTo(n);
        }
    }

    public void stepBy(long n) {
        if (this.bar != null) {
            bar.stepBy(n);
        }
    }

    public void reset() {
        if (bar != null) {
            bar.reset();
        }
    }

    public void close() {
        if (bar != null) {
            bar.close();
        }
    }

    public void pause() {
        if (bar != null) {
            bar.pause();
        }
    }

    public void resume() {
        if (bar != null) {
            bar.resume();
        }
    }

    public void maxHint(long n) {
        if (bar != null) {
            bar.maxHint(n);
        }
    }

    public void setExtraMessage(String message) {
        if (bar != null) {
            bar.setExtraMessage(message);
        }
    }

    public void refresh() {
        if (this.bar != null) {
            bar.refresh();
        }
    }
}
