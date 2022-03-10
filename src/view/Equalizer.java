package view;

import model.Controls;
import java.util.Arrays;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.EqualizerBand;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import model.Controls.MediaProperty;
import util.Lambda.Function;
import util.Util;
import util.View;
import util.View.cText;
import util.View.cVBox;
import util.View.cHBox;

public class Equalizer extends Stage {

	private Controls controls;
	
	public Equalizer(Controls controls) {
		this.controls = controls;
		initSliders();
	}
	
	private void initSliders() {	
		HBox gain_box = new cHBox();
		gain_box.setSpacing(10);
		gain_box.setPrefSize(450, 150);
		HBox freq_box = new cHBox();
		freq_box.setSpacing(10);
		freq_box.setPrefSize(450, 150);
		HBox bw_box = new cHBox();
		bw_box.setSpacing(10);
		bw_box.setPrefSize(450, 150);
		
		List<EqualizerBand> bands = controls.getBands();
		for (int i = 0; i < bands.size(); i++) {
			EqualizerBand band = bands.get(i);
			Slider gain_slider = new MySlider(EqualizerBand.MIN_GAIN, EqualizerBand.MAX_GAIN, band.getGain());
			gain_slider.setOrientation(Orientation.VERTICAL);
			band.gainProperty().bindBidirectional(gain_slider.valueProperty());
			VBox gain = getControlBox(gain_slider);
			gain_box.getChildren().add(gain);
			
			Slider freq_slider = new MySlider(0, band.getCenterFrequency()*5, band.getCenterFrequency());	
			band.centerFrequencyProperty().bindBidirectional(freq_slider.valueProperty());
			VBox freq = getControlBox(freq_slider);
			freq_box.getChildren().add(freq);
			
			Slider bw_slider = new MySlider(0, band.getBandwidth()*5, band.getBandwidth());
			bw_slider.setOrientation(Orientation.VERTICAL);
			band.bandwidthProperty().bindBidirectional(bw_slider.valueProperty());
			VBox bw = getControlBox(bw_slider);
			bw_box.getChildren().add(bw);
		}
		VBox vbox = new cVBox(new cVBox(new cText("Gain"), gain_box), new cVBox(new cText("Frequency"), freq_box),
				new cVBox(new cText("Bandwidth"), bw_box));
		vbox.setSpacing(15);	
		setScene(new Scene(vbox));
		hide();
	}
	
	public void show(boolean show) {
		if (show) {
			show();
			toFront();
			requestFocus();
		}
		else {
			hide();
		}
	}
	
	public static VBox getControlBox(Slider slider) {
		Text lbl = new cText();
		lbl.textProperty().bind(Bindings.createStringBinding(()->String.format("%.2f", slider.getValue()),
		slider.valueProperty()));
		VBox control_vbox = new cVBox(new cText(slider.getMax()+""), slider, new cText(slider.getMin()+""), lbl);
		control_vbox.setSpacing(5);
		return control_vbox;
	}
	
	
	public static class MySlider extends Slider {
			
		public MySlider(double min, double max, double val) {
			super(min, max, val);
			super.setBlockIncrement((max-min)/1000f);
			super.setSnapToTicks(false);
			super.setMajorTickUnit((max-min)/4f);	
			super.setShowTickLabels(false);	
			super.setOrientation(Orientation.VERTICAL);
		}
		
		public MySlider(MediaProperty p, ObservableValue<? extends Number> val) {
			this(p.getMin(), p.getMax(), p.getValue());
			p.bind(val);
		}
		/**
		 * @param p
		 * @param f : The mapping from property p value to slider value
		 * @param f_rev : Mapping from slider value to p
		 */
		public MySlider(MediaProperty p, Function<Double, Double> p_to_s, Function<Double, Double> s_to_p) {
			this(p_to_s.apply(p.getMin()), p_to_s.apply(p.getMax()), p_to_s.apply(p.getValue()));
			p.bind(Bindings.createDoubleBinding(()->
			s_to_p.apply(this.getValue()), this.valueProperty()));
			
		}
	}
	
}
