package view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Controls;
import model.Controls.MediaProperty;
import model.MediaList;
import model.MediaPlayer;
import resources.Images;
import util.Observer;
import util.PMedia;
import util.Util;
import util.View;
import util.View.SubView;
import util.View.cText;


public class MediaPlayerView implements SubView, Observer {

	private MediaPlayer media_player;
	private MediaList media_list;
	private Controls controls;
	
	private HBox hbox, control_hbox;
	private ImageView play_iv, pause_iv;
	private Button play_btn, next_btn, back_btn, repeat_btn, shuffle_btn;
	private Label song_lbl;
	private VBox song_info, queue_view;
	private VBox volume_view;
	private Slider vol_slider;
	private Slider seeker;
	private HBox seeker_view;
	
	public MediaPlayerView(MediaPlayer media_player, MediaList media_list, Controls controls, Slider vol_slider) {
		this.media_player = media_player;
		this.media_list = media_list;
		this.controls = controls;
		this.vol_slider = vol_slider;
		
		addContent();
		addEvents();
		addLayout();
		
		updateSong();
		updatePlay();
		updateControlBtn(shuffle_btn, controls.isShuffle());
		updateControlBtn(repeat_btn,  controls.isRepeat());
		//updateSong();	
		
		media_player.addObserver("view", this);
	}

	@Override
	public void addContent() {
		initControlBtns();
		initSongInfo();
		
		volume_view = ControlsView.getControlBox("volume", vol_slider);
		queue_view = new VBox(); //TODO

		hbox = new cHBox(control_hbox, song_info, volume_view);	
	}

	private void initControlBtns() {
		play_iv = new ImageView();
		pause_iv = new ImageView();
		play_btn = new Button();
		next_btn = new Button();
		back_btn = new Button();
		shuffle_btn = new Button();
		repeat_btn = new Button();
		control_hbox = new cHBox(shuffle_btn, back_btn, play_btn, next_btn, repeat_btn);	
	}

	private void initSongInfo() {
		song_lbl = new Label();
		seeker = new Slider();
		seeker_view = new cHBox(seeker, new Text());
		
		Text curr_time_view = new Text("0");
		
		curr_time_view.translateXProperty().bind(Bindings.createDoubleBinding(
				()->(seeker.getValue()/seeker.getMax())*seeker.getLayoutBounds().getWidth(),
				seeker.valueProperty()));
		curr_time_view.textProperty().bind(Bindings.createStringBinding(
				()->String.format("%d:%02d", (int)(seeker.getValue()/60), (int)(seeker.getValue()%60))
				,seeker.valueProperty()));
		
		song_info = new VBox(song_lbl, seeker_view, curr_time_view);
		song_info.getStyleClass().add("song_info_box");
		song_info.setOnMouseClicked(m->{
			if (!media_player.getQueue().isEmpty())
				media_list.view(media_player.getQueue().getFirst());
		});
	}

	int seek_counter = 0;
	
	@Override
	public void addEvents() {
		play_btn.setOnAction(a->{
			controls.setPlay(!controls.isPlaying());
		});
		repeat_btn.setOnAction(a->{
			controls.setRepeat(!controls.isRepeat());
		});
		shuffle_btn.setOnAction(a->{
			controls.setShuffle(!controls.isShuffle());
		});
		back_btn.setOnAction(a->{
			media_player.goBack();
		});
		next_btn.setOnAction(a->{
			media_player.goNext();
		});
		seeker.setOnMousePressed(m->{
			seeker.setValueChanging(true);
		});
		seeker.setOnMouseReleased(m->{
			seeker.setValueChanging(false);
		});
		seeker.setOnKeyPressed(m->{
			if (m.getCode() == KeyCode.RIGHT || m.getCode() == KeyCode.LEFT)
				seeker.setValueChanging(true);
		});
		seeker.setOnKeyReleased(m->{
			if (m.getCode() == KeyCode.RIGHT || m.getCode() == KeyCode.LEFT)
				seeker.setValueChanging(false);
		});
		seeker.valueChangingProperty().addListener((c,o,n)->{
			//Util.print("seeker value changing "+o+"->"+n);
			if (!n) { //stopped changing, so seek to new position
				media_player.seek(seeker.getValue()/seeker.getMax());
				controls.setPlay(controls.getOldPlayState());
			}
			else {
				controls.savePlayState();
				controls.setPlay(false);
			}
		});
	}

	public void handle(KeyEvent k) {
		if (k.getCode() == KeyCode.F9)
			next_btn.getOnAction().handle(null);
		else if (k.getCode() == KeyCode.F7)
			back_btn.getOnAction().handle(null);
		else if (k.getCode() == KeyCode.F8)
			play_btn.getOnAction().handle(null);
	}

	@Override
	public void addLayout() {
		song_lbl.getStyleClass().add("song_lbl");
		song_lbl.setWrapText(false);
		song_lbl.setTextAlignment(TextAlignment.CENTER);
		song_lbl.setAlignment(Pos.CENTER);
		
		play_iv.setImage(Images.play);
		pause_iv.setImage(Images.pause);
		play_btn.setGraphic(play_iv);
		repeat_btn.setGraphic(new ImageView(Images.repeat));
		shuffle_btn.setGraphic(new ImageView(Images.shuffle));
		back_btn.setGraphic(new ImageView(Images.back));
		next_btn.setGraphic(new ImageView(Images.next));
		repeat_btn.setContentDisplay(ContentDisplay.CENTER);
		shuffle_btn.setContentDisplay(ContentDisplay.CENTER);
		play_btn.setContentDisplay(ContentDisplay.CENTER);
		back_btn.setContentDisplay(ContentDisplay.CENTER);
		next_btn.setContentDisplay(ContentDisplay.CENTER);

		play_btn.getStyleClass().add("control_btn");
		back_btn.getStyleClass().add("control_btn");
		next_btn.getStyleClass().add("control_btn");
		
		volume_view.getStyleClass().add("control_hbox");
		//hbox.getStyleClass().add("media_player");
		control_hbox.getStyleClass().add("control_hbox");
		hbox.getStyleClass().add("control_hbox");
	}

	@Override
	public void my_resize(double width, double height) {
		double k = width/3;
		double k2 = k/15; //40; //k/10;
		
		volume_view.setMaxSize(k, height);
	//	control_hbox.setMaxSize(k, height);
		song_info.setMaxSize(k, height);		
		song_lbl.setMaxWidth(k);
		
		ImageView iv = play_iv;
		iv.setFitWidth(k2);
		iv.setFitHeight(k2);
		iv = pause_iv;
		iv.setFitWidth(k2);
		iv.setFitHeight(k2);
		iv = (ImageView)next_btn.getGraphic();
		iv.setFitWidth(k2);
		iv.setFitHeight(k2);
		iv = (ImageView)back_btn.getGraphic();
		iv.setFitWidth(k2);
		iv.setFitHeight(k2);
		iv = (ImageView)shuffle_btn.getGraphic();
		iv.setFitWidth(k2);
		iv.setFitHeight(k2);
		iv = (ImageView)repeat_btn.getGraphic();
		iv.setFitWidth(k2);
		iv.setFitHeight(k2);
		this.getContent().getParent().layout();
		
		//Util.print(play_btn.getLayoutBounds().getWidth()+","+play_btn.getLayoutBounds().getHeight());
		HBox.setMargin(back_btn, new Insets(k2/2));
		HBox.setMargin(next_btn, new Insets(k2/2));
		HBox.setMargin(song_info, new Insets(k2/2));
		hbox.setPrefSize(width, height);
		HBox.setHgrow(control_hbox, Priority.ALWAYS);
		HBox.setMargin(volume_view, new Insets(10));
	
	}

	@Override
	public Node getContent() {
		return hbox;
	}
	
	@Override
	public void update(String msg) {
		if (msg.equals("seek"))
			updateSeek();
		else if (msg.contains("shuffle"))
			updateControlBtn(shuffle_btn, controls.isShuffle());
		else if (msg.contains("repeat"))
			updateControlBtn(repeat_btn, controls.isRepeat());
		else if (msg.equals("song"))
			updateSong();
		else if (msg.contains("play") || msg.contains("pause"))
			updatePlay();
	}
	
	private void updateSeek() {
		//javafx.scene.media.MediaPlayer mp = media_player.getMp();
		if (!seeker.isValueChanging())
			seeker.setValue(media_player.getCurrentSeconds());
	}
	
	private void updateSong() { //assumes view is notified after mediaplayer is ready/knows song duration
		PMedia media = media_player.getMedia();
		Text length = (Text)seeker_view.getChildren().get(1);
		if (media != null) {
			seeker.setDisable(false);
			song_lbl.setText(media.toString());
			seeker.setMax(media.getLength());
			seeker.setMin(0);
			seeker.setValue(0);
			length.setText((int)(media.getLength()/60)+":"+String.format("%02d", (int)(media.getLength()%60)));
		}
		else {
			song_lbl.setText("");
			length.setText("");
			seeker.setDisable(true);
		}
	}
	
	private void updatePlay() {
		play_btn.setGraphic(controls.isPlaying()?pause_iv:play_iv);
	//	if (controls.isPlaying()) song_lbl_timer.play();
	//	else song_lbl_timer.pause();
		
	}
	
	private void updateControlBtn(Button control_btn, boolean on) {
		if (on) {
			control_btn.getStyleClass().remove("control_btn_off");
			control_btn.getStyleClass().add("control_btn_on");
		}
		else {
			control_btn.getStyleClass().remove("control_btn_on");
			control_btn.getStyleClass().add("control_btn_off");
		}
	}
	
}
