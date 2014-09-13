/**
 * @(#)TimeSync.java
 *
 *
 * @author Mikael Murstam
 * @version 1.00 2011/10/16
 */

public class TimeSync {
	private long ms, start, loop, fps, sec, everyS;

    public TimeSync() {
    	start=System.currentTimeMillis();
    	ms=0;
    	loop=-1;
    	fps=0;
    	sec=0;
    	everyS=1;
    }

    public TimeSync(long l) {
    	start=System.currentTimeMillis();
    	ms=0;
    	loop=-1;
    	fps=0;
    	sec=0;
    	everyS=l;
    }

    public void begin() {
    	loop++;
    	sec+=ms=System.currentTimeMillis()-start;
    	if(sec>=(1000/everyS)){
    		fps=loop*everyS;
    		loop=0;
    		sec=0;
    	}
    	if(ms > 0) start=System.currentTimeMillis();
    }

    public double perSec(double value) {
    	return (double)ms*value/1000.0;
    }

    public double perMilliSec(double value) {
		return (double)ms*value;
    }

    public long getFPS() {
    	return fps;
    }

    public double getMS() {
    	return ms;
    }

    public double getFloatFPS() {
    	return 1000/ms;
    }
}