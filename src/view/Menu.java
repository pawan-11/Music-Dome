package view;

import resources.Images;
import util.PMedia;
import util.Util;
import util.View;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application.Parameters;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import model.Controls;
import model.MediaList;
import model.MediaPlayer;

public class Menu extends Pane implements View {

	private Window w;
	public static final String font = "Times New Roman";
	private MediaList ml;
	private MediaPlayer mp;
	private MediaListView mlv;
	private MediaPlayerView mpv;
	private Controls c;
	private ControlsView cv;
	private Equalizer ev;
	private Yt yt;
	private ImageView yt_logo;
	
	public static Text error;

	public static File home_dir = new File("/Users/paw/Downloads/Music");
	public static String[] media_regexes = {".*\\.mp3", ".*\\.m4a", ".*\\.mov", ".*\\.mp4"};

	public Menu(Window w) {	
		this.w = w;

		addContent();
		addEvents();
		addLayout();
	}

	public void addContent() {	
		error = new Text();
		
		c = new Controls();
		cv = new ControlsView(c);
		ev = new Equalizer(c);

		cv.initOwner(w.getStage());
		cv.initStyle(StageStyle.UTILITY);
		ev.initOwner(w.getStage());
		ev.initStyle(StageStyle.UTILITY);
		
		Slider cv_vol_slider = cv.getVolSlider();
		Slider mp_vol_slider = new Slider();
		mp_vol_slider.valueProperty().bindBidirectional(cv_vol_slider.valueProperty());

		ml = new MediaList(home_dir);
		mp = new MediaPlayer(ml, c);
		mlv = new MediaListView(ml, c);
		mpv = new MediaPlayerView(mp, ml, c, mp_vol_slider);

		yt = new Yt(home_dir, ml);
		yt_logo = new ImageView(Images.yt);
		
		MenuBar menu_bar = new MenuBar();

		javafx.scene.control.Menu file_menu = new javafx.scene.control.Menu("File");
		MenuItem home_item = new MenuItem("Open Home");
		home_item.setOnAction(a->{
			Util.openDir(home_dir);
		});
		MenuItem change_item = new MenuItem("Change Home");
		change_item.setOnAction(a->{
			File file = Util.askDir("Select Home Directory for Media files");
			if (file != null) {
				home_dir = file;
				ml.clearMedia();
				ml.importMedia(home_dir);
			}
		});
		MenuItem import_item = new MenuItem("Import");
		import_item.setOnAction(a->{
			File file = Util.askFile("Choose Media", "Audio or Video files", Menu.media_regexes);
			ml.importMedia(file);
		});
		MenuItem refresh_item = new MenuItem("Refresh Home");
		refresh_item.setOnAction(a->{
			ml.clearSelectedMedias();
			ml.clearMedia();
			ml.importMedia(home_dir);
		});		
		file_menu.getItems().addAll(home_item, import_item, change_item, refresh_item);

		
		javafx.scene.control.Menu control_menu = new javafx.scene.control.Menu("Controls");
		MenuItem controls_item = new MenuItem("Edit");
		controls_item.setOnAction(a->{
			cv.show(true);
		});
		MenuItem ev_item = new MenuItem("Equalizer");
		ev_item.setOnAction(a->{

			ev.show(true);
		});
		MenuItem reset = new MenuItem("Reset");
		reset.setOnAction(a->{
			boolean tmp_play = c.isPlaying();
			c.setPlay(false);
			c.resetBands();
			cv.resetProperties(); //since these properties are bound to sliders, change sliders
			c.setPlay(tmp_play);
		});
		control_menu.getItems().addAll(controls_item, ev_item, reset);
		menu_bar.getMenus().addAll(file_menu, control_menu);
		if (Util.isOs("mac"))
			menu_bar.setUseSystemMenuBar(true); //
		else
			menu_bar.setUseSystemMenuBar(false);
		
		this.getChildren().addAll(mlv.getContent(), mpv.getContent(), 
				 yt.getContent(), yt_logo, error, menu_bar);	
	}

	public void addEvents() {
		this.setOnKeyPressed(k->{
			if (k.getCode() == KeyCode.E && k.isShiftDown()) {
				error.setVisible(!error.isVisible());
			}
			else if (!k.isShiftDown()) {
				mpv.handle(k);
				mlv.handle(k);
			}
		});
		
		yt_logo.setOnMouseClicked(m->{
			this.requestFocus();
			yt.show(!yt.isVisible());
		});
		this.setOnMouseClicked(m->{
			this.requestFocus();
		});
	}

	public void addLayout() {	
		this.getStyleClass().add("menubg");   
		error.setVisible(false);
		error.setFill(Color.BLACK);
		yt.show(false);
	//	yt.getContent().setVisible(false); //TODO
	}

	public void my_resize(double width, double height) {		
		double k = getK(width, height);
		Node mlv_c = mlv.getContent();
		Node mpv_c = mpv.getContent();
		Node yt_c = yt.getContent();
		
		yt_logo.setFitHeight(40);
		yt_logo.setFitWidth(40);
		
		mpv.my_resize(width-50, 50);
		this.layout();
		mlv.my_resize(width-50, height-mpv_c.getLayoutBounds().getHeight()); 
		yt.my_resize(width/2, 200);
		this.layout();	
		
		mlv_c.setLayoutX(width-mlv_c.getLayoutBounds().getWidth());
		mlv_c.setLayoutY(height-mlv_c.getLayoutBounds().getHeight());

		mpv_c.setLayoutX(mlv_c.getLayoutX());
		mpv_c.setLayoutY(0);
		
		yt_logo.setLayoutX(mpv_c.getLayoutX()/2-yt_logo.getFitWidth()/2);
		yt_logo.setLayoutY(mpv_c.getLayoutY()+(mpv_c.getLayoutBounds().getHeight()/2)-yt_logo.getFitHeight()/2);
		yt_c.setLayoutX(yt_logo.getLayoutX());
		yt_c.setLayoutY(yt_c.getLayoutX()+yt_logo.getFitHeight()+5);
	}

	public void show() {
		this.requestFocus();
	}

	public static double getK(double width, double height) {
		return height < width? height/22+1:width/22+1;
	}

	public void exit() {
		ml.exit(); //cache preferences, not used yet
		mp.exit(); //stop media player
		cv.close();
		ev.close();
	}
	
	public static void clearErrors() {
		error.setText("");
	}
	
	public static void error(String msg) {
		error.setText(error.getText()+"\n"+msg);
	}

	public void handleParameters(Parameters p) {
		//Util.print("args "+ p.getRaw());
		error(p.getRaw().toString());
				
		List<PMedia> req_medias = new ArrayList<PMedia>();
		for (String arg: p.getRaw()) {
			PMedia media = new PMedia(new File(arg));
			req_medias.add(media);
		}
		ml.setPlaylist(req_medias); //could be empty
	}
	
	public void doLongTasks(Parameters p) {
		Thread t = new Thread() {
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						ml.importMedia(home_dir);
						handleParameters(p);
					}					
				});	
			}
		};
		t.start();
	}
}