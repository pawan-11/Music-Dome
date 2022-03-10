package view;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

//import com.github.axet.vget.VGet;
//import com.github.axet.vget.info.VideoInfo;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.MediaList;
import resources.Images;
import util.PMedia;
import util.Util;
import util.View;
import util.View.SubView;
import util.View.cText;
import util.View.cVBox;


public class Yt implements SubView {

	private MediaList ml;
	private Button convert, clear;
	private TextField name, url_field;
	private Text result;
	private ScrollPane result_sp;
	private VBox vbox;
	private File save_dir;

	public Yt(File save_dir, MediaList ml) {
		this.ml = ml;
		this.save_dir = save_dir;

		addContent();
		addEvents();
		addLayout();
	}

	@Override
	public void addContent() {		
		url_field = new TextField();
		url_field.setPromptText("Enter Youtube Url here");
		name = new TextField();
		name.setPromptText("Enter file name here");

		convert = new Button("Convert");
		clear = new Button("Clear");
		result = new Text("");

		result_sp = new ScrollPane();
		result_sp.setContent(result);

		HBox hbox = new cHBox(clear, convert);
		hbox.setSpacing(10);

		vbox = new cVBox(url_field, name, hbox, result_sp);
		vbox.setSpacing(10);
	}

	@Override
	public void addEvents() {
		convert.setOnAction(a->{
			this.result.setText("Converting...");
			Thread t = new Thread(()->{
				downloadAudio(url_field.getText()); 
			});
			t.start();
		});
		clear.setOnAction(a->{
			result.setText("");
			name.setText("");
			url_field.setText("");
		});
		vbox.setOnMouseClicked(m->{
			vbox.requestFocus();
		});
	}

	@Override
	public void addLayout() {
		result_sp.setPannable(true);
		result_sp.setFitToWidth(true);
		result_sp.setVbarPolicy(ScrollBarPolicy.ALWAYS);

		vbox.setPickOnBounds(false); //wow i got scammed by doc
		vbox.getStyleClass().add("yt_vbox");
	}

	@Override
	public void my_resize(double width, double height) {
		convert.resize(100, 40);

		url_field.setPrefSize(width-convert.getLayoutBounds().getWidth(), 30);
		name.setMaxWidth(url_field.getPrefWidth()/2);
		result.setWrappingWidth(width);
		result_sp.setMaxHeight(200);
		//	hbox.setLayoutX(width/2-hbox.getLayoutBounds().getWidth()/2);
		//	hbox.setLayoutY(height/2-hbox.getLayoutBounds().getHeight()/2);
	}

	@Override
	public Node getContent() {
		return vbox;
	}

	public void show(boolean show) {
		vbox.setVisible(show);
	}

	public boolean isVisible() {
		return vbox.isVisible();
	}

	public void changeSaveDir(File save_dir) {
		this.save_dir = save_dir;
	}

	private void downloadAudio(String yt_url) {		
		String name = "";
		try {
			//%(title)s [%(id)s].%(ext)s
			//--output 'D:/Downloads/youtube-dl/%(extractor)s/%(title)s [%(resolution)s] [%(id)s] [f%(format_id)s].%(ext)s'
			if (this.name.getText().length() != 0) //a name is chosen by user
				name = this.name.getText()+".%(ext)s";	
			else
				name = "%(title)s.%(ext)s";
			name = Paths.get(save_dir.getAbsolutePath().toString(), name).toString();

			String bash_cmd = "";
			String result = "";

			bash_cmd = "/usr/local/bin/yt-dlp -f 140 --ffmpeg-location /usr/local/bin "
					+ "--extract-audio --audio-format mp3 \""+yt_url+"\" -o \""+name+"\"";

			result += "Downloading m4a file and converting to mp3\n";
			result+="Running command: "+bash_cmd+"\n\n";
			result += Util.exec(new String[]{"/bin/sh", "-c", bash_cmd})+"\n\n";

			String dst = "Destination: ";
			int start_idx = result.indexOf(dst)+dst.length();
			int end_idx = start_idx-1;
			while ((++end_idx) < result.length() && result.charAt(end_idx) != ' ');
			String output_path = result.substring(start_idx, end_idx);
			Util.print("out file: "+output_path);

			//convert m4a to mp3
			//p = Runtime.getRuntime().exec("ffmpeg -i input_file -codec:a libmp3lame -qscale:a 1");
			this.result.setText(result);

		}
		catch (Exception e) {
			result.setText("Error converting");
			Util.print("error downloading "+yt_url);
			Menu.error("error downloading "+yt_url);
			e.printStackTrace();
		}
	}

	/*
	private void downloadVideo(String yt_url) {
		try {
			Thread t = new Thread() {
				public void run() {
					try {
						VGet v = new VGet(new URL(yt_url), save_dir);
						VideoInfo info = v.getVideo();
						Util.print(info.getTitle());
						v.download();
						Util.print(info.getTitle());
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
			};
			t.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		//return new PMedia(file);
	}
	 */
}
