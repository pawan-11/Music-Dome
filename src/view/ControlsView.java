package view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Controls;
import model.Controls.MediaProperty;
import util.Lambda.Function;
import util.Util;
import util.View;
import util.View.cText;
import util.View.cVBox;
import util.View.cHBox;

public class ControlsView extends Stage {
	
	private Controls controls;
	private List<MySlider> sliders;
	
	public ControlsView(Controls controls) {
		this.controls = controls;
		initSliders();
	}
	
	private void initSliders() {		
		sliders = new ArrayList<MySlider>();
		
		VBox control_box = new cVBox(new cText("Controls"));
		control_box.setSpacing(10);
		control_box.setPrefSize(400, 400);
				
		List<Function<Double, Double>> p_to_s = Arrays.asList(x->x*100, x->x*100, x->x, x->x, x->x, x->x);
		List<Function<Double, Double>> s_to_p = Arrays.asList(x->x/100, x->x/100, x->x, x->x, x->x, x->x);
		List<String> names = controls.control_names;
		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			MySlider slider = new MySlider(controls.get(name), p_to_s.get(i), s_to_p.get(i));
			sliders.add(slider);
			
			VBox control_vbox = getControlBox(name, slider);
			control_box.getChildren().add(control_vbox); 
		}
		
		Slider rate_slider = sliders.get(names.indexOf("rate"));
		rate_slider.setOnMousePressed(m->{
			controls.savePlayState();
			controls.setPlay(false);
		});
		rate_slider.setOnMouseReleased(m->{
			controls.setPlay(controls.getOldPlayState());
		});
		setScene(new Scene(control_box, control_box.getPrefWidth(), control_box.getPrefHeight()));
		hide();
	}
	
	public Slider getVolSlider() {
		return sliders.get(0);
	}
	
	public static VBox getControlBox(String name, Slider control) {
		HBox control_hbox = new cHBox(new cText(control.getMin()+""), control, new cText(control.getMax()+""));
		control_hbox.setSpacing(5);
		Text lbl = new cText(Util.toCamelCase(name));
		lbl.textProperty().bind(Bindings.createStringBinding(()->
		Util.toCamelCase(name)+": "+String.format("%.2f", control.getValue()),
			control.valueProperty()));
		VBox control_vbox = new cVBox(lbl, control_hbox);
		return control_vbox;
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
	
	public void resetProperties() {
		List<Number> init_vals = Arrays.asList(0.5, 1, 0, -60, 0.1, 128);
		for (int i = 0; i < sliders.size(); i++) {
			sliders.get(i).setPropValue(init_vals.get(i).doubleValue());
		}		
	}
	
	public static class MySlider extends Slider {
			
		private Function<Double, Double> p_to_s = x->x;
		public MySlider(double min, double max, double val) {
			super(min, max, val);
			super.setBlockIncrement((max-min)/50);
			super.setSnapToTicks(false);
			super.setMajorTickUnit((max-min)/4);	
			super.setShowTickLabels(true);
			
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
			this.p_to_s = p_to_s;
		}
		
		public void setPropValue(double p_val) {
			super.setValue(p_to_s.apply(p_val));
		}
	}
}
