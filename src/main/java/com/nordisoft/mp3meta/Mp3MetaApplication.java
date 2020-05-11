package com.nordisoft.mp3meta;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ibm.icu.text.Transliterator;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

@SpringBootApplication
public class Mp3MetaApplication implements CommandLineRunner {

	
	public static final String RUSSIAN_TO_LATIN = "Russian-Latin/BGN";
	public static final String UKRAINIAN_TO_LATIN = "Ukrainian-Latin/BGN";
	
	public static final Boolean VERBOSE = false;
	
	public static final String ROOT_DIRECTORY = "/media/igor/2141-200D/Rock UA";
	
	// Based on:
	// Uses https://github.com/mpatric/mp3agic
	// And https://stackoverflow.com/questions/16273318/transliteration-from-cyrillic-to-latin-icu4j-java
	// https://stackoverflow.com/questions/5818912/icu4j-cyrillic-to-latin
	// https://ru.stackoverflow.com/questions/633355/%D0%9F%D0%BE%D0%BA%D0%B0%D0%B7%D0%B0%D1%82%D1%8C-%D0%BF%D1%80%D0%B0%D0%B2%D0%B8%D0%BB%D1%8C%D0%BD%D1%8B%D0%B9-%D0%BF%D1%80%D0%B8%D0%BC%D0%B5%D1%80-%D1%82%D1%80%D0%B0%D0%BD%D1%81%D0%BB%D0%B8%D1%82%D0%B5%D1%80%D0%B0%D1%86%D0%B8%D0%B8-%D0%BD%D0%B0-java

	public static void main(String[] args) {
		SpringApplication.run(Mp3MetaApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Transliterator toLatinTrans = Transliterator.getInstance(UKRAINIAN_TO_LATIN);
		List<File> mp3FilesFromDirectory = getMP3FilesFromDirectory(ROOT_DIRECTORY);
		int counter = 1;
		for (File file : mp3FilesFromDirectory) {
			System.out.println("Transliterating file " + counter + " of " + mp3FilesFromDirectory.size() + ": " + file.getName());
			transliterate(file, toLatinTrans, ROOT_DIRECTORY);
			counter++;
		}
		System.out.println("Done.");
	}

	public List<File> getMP3FilesFromDirectory(String dirPath) {
		File dir = new File(dirPath);
		File[] files = dir.listFiles((d, name) -> name.endsWith(".mp3"));
		if (files != null) {
			if (VERBOSE) {
				List<String> filenames = Stream.of(files).map((f) -> f.getName()).collect(Collectors.toList());
				System.out.println("Found files in directory: " + StringUtils.join(filenames, "; "));
			}
			return List.of(files);
		} else {
			return Collections.emptyList();
		}
	}
	
	public static void transliterate(File file, Transliterator toLatinTrans, String dirpath) throws UnsupportedTagException, InvalidDataException, IOException, NotSupportedException {
		Mp3File mp3file = new Mp3File(file);
		if (VERBOSE) {
			System.out.println("Length of this mp3 is: " + mp3file.getLengthInSeconds() + " seconds");
			System.out.println("Bitrate: " + mp3file.getBitrate() + " kbps " + (mp3file.isVbr() ? "(VBR)" : "(CBR)"));
			System.out.println("Sample rate: " + mp3file.getSampleRate() + " Hz");
			System.out.println("Has ID3v1 tag?: " + (mp3file.hasId3v1Tag() ? "YES" : "NO")); // outdated
			System.out.println("Has ID3v2 tag?: " + (mp3file.hasId3v2Tag() ? "YES" : "NO")); // most common
			System.out.println("Has custom tag?: " + (mp3file.hasCustomTag() ? "YES" : "NO"));
		}

		if (mp3file.hasId3v2Tag()) {
			ID3v2 id3v2Tag = mp3file.getId3v2Tag();
			if (VERBOSE) {
				System.out.println("Track: " + id3v2Tag.getTrack());
				System.out.println("Artist: " + id3v2Tag.getArtist());
				System.out.println("Title: " + id3v2Tag.getTitle());
				
				System.out.println("Artist transliterated: " + toLatinTrans.transliterate(id3v2Tag.getArtist()));
				System.out.println("Title transliterated: " + toLatinTrans.transliterate(id3v2Tag.getTitle()));
				
				System.out.println("Album: " + id3v2Tag.getAlbum());
				System.out.println("Year: " + id3v2Tag.getYear());
				System.out.println("Genre: " + id3v2Tag.getGenre() + " (" + id3v2Tag.getGenreDescription() + ")");
				System.out.println("Comment: " + id3v2Tag.getComment());
				System.out.println("Lyrics: " + id3v2Tag.getLyrics());
				System.out.println("Composer: " + id3v2Tag.getComposer());
				System.out.println("Publisher: " + id3v2Tag.getPublisher());
				System.out.println("Original artist: " + id3v2Tag.getOriginalArtist());
				System.out.println("Album artist: " + id3v2Tag.getAlbumArtist());
				System.out.println("Copyright: " + id3v2Tag.getCopyright());
				System.out.println("URL: " + id3v2Tag.getUrl());
				System.out.println("Encoder: " + id3v2Tag.getEncoder());
				byte[] albumImageData = id3v2Tag.getAlbumImage();
				if (albumImageData != null) {
					System.out.println("Have album image data, length: " + albumImageData.length + " bytes");
					System.out.println("Album image mime type: " + id3v2Tag.getAlbumImageMimeType());
				}
			}
			
			String originalArtist = "";
			if (StringUtils.isNotEmpty(id3v2Tag.getArtist())) {
				originalArtist = id3v2Tag.getArtist() + " - ";
				id3v2Tag.setArtist(toLatinTrans.transliterate(id3v2Tag.getArtist()));
			}
			
			String originalTitle = id3v2Tag.getTitle();
			if (StringUtils.isNotEmpty(id3v2Tag.getTitle())) {
				id3v2Tag.setTitle(toLatinTrans.transliterate(id3v2Tag.getTitle()));
			}
			
			if (StringUtils.isNotEmpty(id3v2Tag.getAlbum())) {
				id3v2Tag.setAlbum(toLatinTrans.transliterate(id3v2Tag.getAlbum()));
			}
			id3v2Tag.setComment("Empty");
			
			mp3file.save(dirpath + "/" + originalArtist + originalTitle + ".mp3");
		}
	}
}
