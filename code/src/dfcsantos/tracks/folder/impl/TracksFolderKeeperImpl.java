package dfcsantos.tracks.folder.impl;

import static sneer.foundation.environments.Environments.my;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import sneer.bricks.hardware.cpu.threads.Threads;
import sneer.bricks.hardwaresharing.files.map.FileMap;
import sneer.bricks.pulp.blinkinglights.BlinkingLights;
import sneer.bricks.pulp.blinkinglights.LightType;
import sneer.bricks.pulp.reactive.Register;
import sneer.bricks.pulp.reactive.Signal;
import sneer.bricks.pulp.reactive.Signals;
import sneer.bricks.software.bricks.statestore.BrickStateStore;
import sneer.bricks.software.folderconfig.FolderConfig;
import sneer.foundation.lang.Consumer;
import dfcsantos.tracks.folder.TracksFolderKeeper;

class TracksFolderKeeperImpl implements TracksFolderKeeper {

	private static final BrickStateStore _store = my(BrickStateStore.class);

	private final Register<File> _playingFolder = my(Signals.class).newRegister(defaultTracksFolder());

	private final Register<File> _sharedTracksFolder = my(Signals.class).newRegister(defaultTracksFolder());

	private File _peerTracksFolder;

	@SuppressWarnings("unused") private Object _refToAvoidGc;

	@SuppressWarnings("unused") private Object _refToAvoidGc2;

	TracksFolderKeeperImpl() {
		takeCareOfPersistence();
	}

	@Override
	public Signal<File> playingFolder() {
		return _playingFolder.output();
	}

	@Override
	public void setPlayingFolder(File playingFolder) {
		_playingFolder.setter().consume(playingFolder);
	}


	@Override
	public Signal<File> sharedTracksFolder() {
		return _sharedTracksFolder.output();
	}

	@Override
	public void setSharedTracksFolder(File sharedTracksFolder) {
		_sharedTracksFolder.setter().consume(sharedTracksFolder);
		startSharedTracksFolderMappingDeamon();
	}

	@Override
	public File peerTracksFolder() {
		if (_peerTracksFolder == null)
			_peerTracksFolder = mkDirs(new File(my(FolderConfig.class).tmpFolderFor(TracksFolderKeeper.class), "peertracks"));
				
		return _peerTracksFolder;
	}

	private File mkDirs(File folder) {
		if (!folder.exists() && !folder.mkdirs())
			my(BlinkingLights.class).turnOn(LightType.ERROR, "Unable to create folder.", "Unable to create folder: " + folder);
		return folder;
	}

	private File defaultTracksFolder() {
		return mkDirs(new File(my(FolderConfig.class).storageFolder().get() ,"media/tracks"));
	}

	private void takeCareOfPersistence() {
		restore();

		_refToAvoidGc = playingFolder().addReceiver(new Consumer<File>(){ @Override public void consume(File newPlayingFolder) {
			if (newPlayingFolder == null) return;
			save(foldersPathList(newPlayingFolder, sharedTracksFolder().currentValue()));
		}});

		_refToAvoidGc2 = sharedTracksFolder().addReceiver(new Consumer<File>(){ @Override public void consume(File newSharedTracksFolder) {
			if (newSharedTracksFolder == null) return;
			save(foldersPathList(playingFolder().currentValue(), newSharedTracksFolder));
		}});
	}

	private void restore() {
		startPeerTracksFolderMapping();

		List<String> restoredFolderPaths = (List<String>) _store.readObjectFor(TracksFolderKeeper.class, getClass().getClassLoader());
		if (restoredFolderPaths == null) {
			startSharedTracksFolderMappingDeamon();
			return;
		}

		setPlayingFolder(new File(restoredFolderPaths.get(0)));
		setSharedTracksFolder(new File(restoredFolderPaths.get(1)));
	}

	private void startSharedTracksFolderMappingDeamon() {
		my(Threads.class).startDaemon("Shared Tracks Folder Mapping", new Runnable() { @Override public void run() {
			try {
				my(FileMap.class).put(sharedTracksFolder().currentValue());
			} catch (IOException e) {
				throw new sneer.foundation.lang.exceptions.NotImplementedYet(e); // Fix Handle this exception.
			}		
		}});
	}

	private void startPeerTracksFolderMapping() {
		my(Threads.class).startDaemon("Peer Tracks Folder Mapping", new Runnable() { @Override public void run() {
			try {
				my(FileMap.class).put(peerTracksFolder());
			} catch (IOException e) {
				throw new sneer.foundation.lang.exceptions.NotImplementedYet(e); // Fix Handle this exception.
			}
		}});
	}

	private List<String> foldersPathList(File playingFolder, File sharedTracksFolder) {
		return Arrays.asList(playingFolder.getPath(), sharedTracksFolder.getPath());
	}

	private void save(List<String> foldersPathToPersist) {
		_store.writeObjectFor(TracksFolderKeeper.class, foldersPathToPersist);
	}

}
