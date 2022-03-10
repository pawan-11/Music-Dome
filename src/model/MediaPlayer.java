package model;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.media.EqualizerBand;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;
import util.Observable;
import util.Observer;
import util.PMedia;
import util.Util;
import view.Menu;


public class MediaPlayer extends Observable implements Observer {

	private static final int max_queue_size = 10, max_history_size = 50;
	private javafx.scene.media.MediaPlayer mp;
	private MediaList media_list;
	private LinkedList<PMedia> queue;
	private LinkedList<PMedia> history;

	private Controls controls;
	//private Timeline update_seek;
	private Timeline update_seek;
	private ChangeListener<Status> status_l;
	ChangeListener<? super Duration> cycle_l;
	private Runnable onEnd, onReady, onError; //end of media and when mediaplayer is ready
	
	
	public MediaPlayer(MediaList media_list, Controls controls) {
		this.controls = controls;
		this.media_list = media_list;
		this.queue = new LinkedList<PMedia>();
		this.history = new LinkedList<PMedia>();
		
		media_list.addObserver("media player", this);  //to observe selected items to make playlist
		controls.addObserver("media player", this);
		
		addEvents();
	}

	private int errors = 0;
	private void addEvents() {
		update_seek = new Timeline();
		update_seek.setCycleCount(Animation.INDEFINITE);
		update_seek.getKeyFrames().add(new KeyFrame(Duration.millis(1000), k->{	
			changed("seek"); //update view	
		}));	
		update_seek.rateProperty().bind(controls.get("rate"));
		
		status_l = (c,o,n)->{
			//Util.print(i+" mp "+ mp +", status:"+o+"->"+n);
		};
		cycle_l = (c,o,n)->{
			Util.print("cycle duration change "+o+"->"+n);
		};
		onEnd = ()->{  //gets triggered after media finishes
			goNext();
		};
		onError = ()->{
			Util.print("MP ERRORRR");
		};
		
		onReady = ()->{		
			PMedia pmedia = this.getMedia();
			
			try {
				Thread.sleep(50);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			double expected_dur = pmedia.getLength();
			mp.setStopTime(Duration.seconds(expected_dur));
			double media_dur = mp.getTotalDuration().toSeconds();
			
			if (Math.abs(media_dur-expected_dur) < 2) {
				if (controls.getOldPlayState() || controls.isPlaying()) //was playing before, then resume
					controls.setPlay(true);
			}
			else {
				errors++;
				Menu.clearErrors();
				Menu.error("Errors: "+errors);
				//Util.print("mp ready but error, media duration: "+media_dur+" expected "+expected_dur+" RETRY "+queue.getFirst());	
				Util.print("mp "+mp+" ready "+queue.getFirst()+","+mp.getTotalDuration());
				this.newMedia(); //attempt to load media player for queue.getfirst
				//Util.print("new mp "+mp);
			}
		};
	}
	
	/**
	 * Create queue with these medias only
	 * @param medias - medias in order to be played in queue
	 */
	private void initQueue(List<PMedia> medias) {
		if (medias.size() == 0) return;
		queue.clear();
		int i = 0;
		while (i < medias.size() && i < max_queue_size) {
			queue.add(medias.get(i));
			i++;
		}		
		newMedia();
	}

	/**
	 * fill Queue with media files selected from medias
	 * @param medias
	 * TODO: remove duplicates from being added in queue, use doubly linked list and remove the chosen media
	 */
	private void fillQueue(List<PMedia> medias) {
		if (medias.size() == 0) return;

		boolean empty = queue.isEmpty();
		int start_idx = empty?0:medias.indexOf(queue.get(queue.size()-1)); //only needed if repeat and shuffle are off
		int i = empty?0:1;

		while (queue.size() < max_queue_size) {
			if (controls.isRepeat())
				queue.add(medias.get(start_idx));
			else if (controls.isShuffle()) {
				PMedia media = medias.get(Util.rand_int(0, medias.size()-1));
				//while (queue.contains(media = medias.get(rand_int(0, medias.size()-1)))); 
				queue.add(media);
			}
			else
				queue.add(medias.get((start_idx+i)%medias.size()));
			i++;
		}
		if (empty)
			newMedia(); //new song at head position of queue
	}

	public void goNext() { //go to the next media in queue
		if (!queue.isEmpty()) {
			history.addFirst(queue.poll());
			//add a new song to end of queue since the head has been played/skipped
			fillQueue(media_list.getPlaylist());
			if (history.size() > max_history_size) {
				history.removeLast();
			}
			controls.savePlayState();
			newMedia(); 
			
			//media_list.view(queue.getFirst()); 
		}
		else { //at start of ptunes, queue is empty
			media_list.setPlaylist(media_list.getMedias());
		}
	}

	public void goBack() {
		if (!history.isEmpty()) {
			queue.removeLast();
			queue.addFirst(history.poll()); //add the last played song to head of queue

			controls.savePlayState();
			newMedia(); //always new song is queue.getFirst()
			
			//media_list.view(queue.getFirst());
			//go to the new head of queue, which is last played song
		}
	}

	
	public void play() { //called after newSong, or if media was paused
		if (!controls.isPlaying()) {
			return; //not allowed to play
		}
		if (mp != null) { 
			mp.play(); 
			update_seek.play();
			
			changed("play");
			return;
		}
		else if (queue.isEmpty()) {
			controls.savePlayState();
			media_list.setPlaylist(media_list.getMedias());	
			
		}

		if (!controls.isPlaying()) { //could not play any song, //empty playlist?
			//error
		}
	}

	public void pause() {
		if (controls.isPlaying()) {
			return; //not allowed to pause || mp.getStatus() != Status.READY TODO
		}
		if (mp != null) {
			update_seek.stop();
			mp.pause();
			changed("paused");
		}
	}

	/*
	 * Called when a new song is queued (head of queue)
	 */
	private synchronized void newMedia() {
		
		if (mp != null) { //handle past media
			unbind();
			mp = new javafx.scene.media.MediaPlayer(queue.getFirst().getMedia());
			bind();
		}
		else {
			mp = new javafx.scene.media.MediaPlayer(queue.getFirst().getMedia());
			bind();
		}	
		media_list.view(queue.getFirst());
		media_list.highlightMedia(queue.getFirst()); //to highlight it in music list

		changed("song");
	}

	public void seek(double percentage) {
		if (mp != null) {	
			Thread t = new Thread(()->{
				mp.seek(Duration.seconds(percentage*getMedia().getLength()));
			});
			t.start();
		}
	}

	private void bind() {
		mp.balanceProperty().bind(controls.get("balance"));
		mp.volumeProperty().bind(controls.get("volume"));
		mp.rateProperty().bind(controls.get("rate"));
		mp.audioSpectrumThresholdProperty().bind(controls.get("threshold"));
		mp.audioSpectrumIntervalProperty().bind(controls.get("interval"));
		mp.audioSpectrumNumBandsProperty().bind(controls.get("bands"));
		mp.getAudioEqualizer().getBands().clear();
		mp.getAudioEqualizer().getBands().addAll(controls.getBands());
		//mp.setCycleCount(1);
		mp.setOnReady(onReady);	
		mp.setOnEndOfMedia(onEnd);
		mp.setOnError(onError);
		mp.cycleDurationProperty().addListener(cycle_l);
	//	mp.statusProperty().addListener(status_l);
		
	}

	private void unbind() {
		//update_seek.stop(); //NEW TOOO: testing
		//mp.stop();
	/*	mp.balanceProperty().unbind();
		mp.volumeProperty().unbind();
		mp.rateProperty().unbind();
		mp.audioSpectrumIntervalProperty().unbind();
		mp.audioSpectrumNumBandsProperty().unbind();
		mp.audioSpectrumThresholdProperty().unbind();
		mp.getAudioEqualizer().getBands().clear();
		
		mp.setOnEndOfMedia(null);
		mp.setOnReady(null);
		mp.setOnPaused(null);
		mp.setOnPlaying(null);
		mp.setOnStopped(null);
		mp.setOnError(null); 
		mp.statusProperty().removeListener(status_l); */
		mp.dispose();
		//mp = null;
	}

	public int getCurrentSeconds() {
		if (mp == null) return 0;
		return (int)Math.round(mp.getCurrentTime().toSeconds());
	}
	
	public javafx.scene.media.MediaPlayer getMp() {
		return mp;
	}

	public LinkedList<PMedia> getQueue() {
		return queue;
	}

	@Override
	public void create_lists() {
		super.create_list("view", 1);
	}

	public Controls getControls() {
		return controls;
	}

	public PMedia getMedia() {	
		return !queue.isEmpty()?queue.getFirst():null;
	}

	@Override
	public void update(String msg) { //message from controls or media list
		if (msg.contains("imported files")) {
			//fillQueue(media_list.getMedias());
		}
		else if (msg.contains("playlist")) { //medialist event, new song(s) requested. 
			if (!queue.isEmpty())  
				history.addFirst(queue.poll()); //skip the current song
			initQueue(media_list.getSelectedMedias());
			fillQueue(media_list.getPlaylist());	
		}
		else if (msg.equals("play")) { //msg by controls
			if (controls.isPlaying()) {
				play();
			}
			else {
				pause();
			}
		}
		else if (msg.equals("repeat")) {
			updateRepeat();
		}
		else if (msg.equals("shuffle")) {
			updateShuffle();
		}
	}


	public void updateShuffle() {
		changed("shuffle");
		while (queue.size() > 1)
			queue.remove(1);
		fillQueue(media_list.getPlaylist());
	}

	public void updateRepeat() {
		changed("repeat");

		if (!queue.isEmpty()) {
			while (queue.size() > 1)
				queue.remove(1);
			if (controls.isRepeat()) {
				fillQueue(queue); //fill with same song
			}
			else {
				fillQueue(media_list.getPlaylist()); //fill the next songs in order after first
			}
		}
	}
	
	public void exit() {
		if (mp != null)
			unbind();
	}
}
