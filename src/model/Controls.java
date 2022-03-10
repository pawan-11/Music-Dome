package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.EqualizerBand;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.MediaPlayer;
import util.Lambda.Function;
import util.Observable;
import util.Util;
import util.View;
import util.View.SubView;
import util.View.cText;

public class Controls extends Observable {
	
	
	private boolean shuffle, repeat, play;
	private Map<String, MediaProperty> properties;
	private List<EqualizerBand> bands;
	public List<String> control_names = Arrays.asList("volume", "rate", 
			"balance", "threshold", "interval", "bands");

	
	private boolean tmp_play; //made for pause and resume by events
	
	public Controls() {
		
		initControls();
		setShuffle(true);
		setPlay(false);
		setRepeat(false);
	}
	
	private void initControls() {
		this.bands = new ArrayList<EqualizerBand>();
		for (int i = 0; i < 10; i++)
			bands.add(new EqualizerBand(0, 0, 0));
		resetBands();
		
		this.properties = new HashMap<String, MediaProperty>();
		properties.put("volume", new MediaProperty(0,1,0.5));
		properties.put("balance", new MediaProperty(-1,1,0));
		properties.put("rate", new MediaProperty(0, 3, 1));
		properties.put("threshold", new MediaProperty(-1000, 0, -60));
		properties.put("interval", new MediaProperty(0, 1, 0.1));
		properties.put("bands", new MediaProperty(2, 1000, 128));
		
		setShuffle(true);
		setRepeat(false);
	}
	
	public void resetBands() {
		List<Integer> freqs = Arrays.asList(32, 64, 125, 250, 500, 1000,
				2000, 4000, 8000, 16000);
		List<Integer> bws = Arrays.asList(19, 39, 78, 156, 312, 625,
				1250, 2500, 5000, 10000);
		for (int i = 0; i < bands.size(); i++) {
			EqualizerBand band = bands.get(i);
			band.setCenterFrequency(freqs.get(i));
			band.setBandwidth(bws.get(i));
			band.setGain(0);
		}
	}
	
	public void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
		changed("shuffle");
	}
	
	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
		changed("repeat");
	}
	
	synchronized public void setPlay(boolean play) {
	//	if (this.play != play) {
	//	Util.print("set play "+play);
			this.play = play;
			changed("play");
	//	}
	}
	
	public List<EqualizerBand> getBands() {
		return bands;
	}
	
	public boolean isPlaying() {
		return play;
	}
	
	public void savePlayState() {
		this.tmp_play = play;
	}
	
	public boolean getOldPlayState() {
		boolean r = tmp_play;
		this.tmp_play = false;
		return r;
	}
	
	public void show(boolean show) {
		show = true;
		changed("show");
	}
	
	public MediaProperty get(String name) {
		return properties.get(name);
	}

	public boolean isRepeat() {
		return repeat;
	}
	
	public boolean isShuffle() {
		return shuffle;
	}
	
	@Override
	public void create_lists() {
		super.create_list("view", 1);
		super.create_list("media player", 1);
	}

	
	public class MediaProperty extends SimpleDoubleProperty {
		
		private double min, max;
		public MediaProperty(double min, double max, double val) {
			super(val);
			this.min = min;
			this.max = max;
			setValue(val);
		}
		
		public void setValue(Number val) {
			if (val.longValue() > max)
				val = max;
			else if (val.longValue() < min)
				val = min;
			super.setValue(val);
		}
		
		public double getMin() {
			return min;
		}
		
		public double getMax() {
			return max;
		}
	}

}
