package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import util.Observable;
import util.PMedia;
import util.Util;
import view.Menu;

public class MediaList extends Observable {
	
	private List<PMedia> medias;
	private List<Integer> selected_media_idxes;
	private int highlighted_media_idx = -1;
	private int view_media_idx = 0;
	private List<PMedia> playlist;
	private File media_dir;
	
	public MediaList(List<File> music_files, File media_dir) {	
		this.media_dir = media_dir;
		this.medias = new ArrayList<PMedia>();
		this.selected_media_idxes = new ArrayList<Integer>() {
			private static final long serialVersionUID = 1L;
			public boolean contains(Object o) {
				Integer i = (Integer)o;
				for (Integer idx: this)
					if (idx.intValue() == i)
						return true;
				return false;
			}
		};
		setPlaylist(medias);
		importMedia(music_files, true);
	}

	public MediaList(File media_dir) {
		this(new ArrayList<File>(), media_dir);
	}
	
	public void importMedia(List<File> media_files, boolean sort) {
		for (File music_file: media_files) {
			addMedia(music_file, false);
		}
		
		if (sort) {
			PMedia[] sorted_arr = new PMedia[]{};		
			sorted_arr = medias.toArray(sorted_arr);
			Arrays.sort(sorted_arr);
			this.medias = new ArrayList<PMedia>(Arrays.asList(sorted_arr));
		}
		//if (selected_media_idxes.size() < 2)
		//	setPlaylist(medias); //update playlist to new medias in music player
		changed("imported files"); //for view
	}

	/**
	 * Import media file, or media files from directory into medias list
	 * @param media_file media: directory or file
	 */
	public void importMedia(File media_file) {
		if (media_file == null || !media_file.exists()) return;
		if (media_file.isDirectory()) {
			importMedia(Util.getFiles(media_file, Menu.media_regexes), true);
		}
		else {
			int pos = addMedia(media_file, true);
			if (pos != -1) {	
				view(medias.get(pos));
				changed("added media at "+pos);
			}
		}
	}
	
	private int addMedia(File media_file, boolean sort) {	
		//not checking if the file already exists, allows duplicate name files
		PMedia media = new PMedia(media_file);
		media.save(media_dir);

		if (media.getLength() != 0) {
			int pos = medias.size();
			if (sort)
				pos = getPosition(media.toString());
			medias.add(pos, media);
			return pos;
		}
		return -1;
	}

	public void deleteMusic(int...idxes) {

	}

	public void deleteMusic(File...files) {

	}
	
	public void clearMedia() {
		medias.clear();
		selected_media_idxes.clear();
		changed("cleared files");
	}
	
	/**
	 * For sorted list of medias
	 * @param prefix
	 * @return index of media that's name is greater than prefix
	 */
	private int getPosition(String prefix) {
		prefix = prefix.toLowerCase();
		int i;
		for (i = 0; i < medias.size(); i++) {
			if (medias.get(i).toString().toLowerCase().compareTo(prefix) > 0)
				break;
		}
		return i;
	}
	
	public void highlightMedia(PMedia media) { //for song playing
		if (highlighted_media_idx != -1)
			changed("unhighlighted media");
		highlighted_media_idx = medias.indexOf(media);
		changed("highlighted media");
	}
	
	public void setPlaylist(List<PMedia> playlist) {
		this.playlist = playlist;
		changed("playlist"); //signal mediaplayer
	}
	
	public int getHighlightedMediaIdx() {
		return highlighted_media_idx;
	}
	
	public void view(PMedia media) {
		view_media_idx = medias.indexOf(media);
		changed("view media");
	}
	
	public void view(String prefix) {
		prefix.toLowerCase();
		view_media_idx = getPosition(prefix);
		changed("view media");
	}
	
	public void clearSelectedMedias() {
		while (!selected_media_idxes.isEmpty())
			selectMedia(selected_media_idxes.get(0), false);
	}
	
	public void selectMedia(Integer idx, boolean select) {
		if (select) {
			selected_media_idxes.add(idx);
			changed("selected "+idx);
		}
		else {
			selected_media_idxes.remove(idx);
			changed("unselected "+idx);
		}
	}

	public List<PMedia> getPlaylist() {
		return playlist;
	}

	public List<Integer> getSelectedMediaIdxes() {
		return selected_media_idxes;
	}
	
	public int getViewMediaIdx() {
		return view_media_idx;
	}
	
	public List<PMedia> getSelectedMedias() {
		List<PMedia> selected_medias = new ArrayList<PMedia>();
		for (int idx: getSelectedMediaIdxes())
			selected_medias.add(medias.get(idx));
		return selected_medias;
	}
	
	public List<PMedia> getMedias() {
		return medias;
	}

	@Override
	public void create_lists() {
		this.create_list("media player", 1);
		this.create_list("view", 1);
	}
	
	public void exit() {

	}
	
}
