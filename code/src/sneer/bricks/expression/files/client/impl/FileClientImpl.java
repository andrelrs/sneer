package sneer.bricks.expression.files.client.impl;

import static sneer.foundation.environments.Environments.my;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import sneer.bricks.expression.files.client.FileClient;
import sneer.bricks.expression.files.client.downloads.Download;
import sneer.bricks.expression.files.client.downloads.Downloads;
import sneer.bricks.hardware.cpu.crypto.Hash;
import sneer.bricks.identity.seals.Seal;
import sneer.foundation.lang.Closure;
import sneer.foundation.lang.Producer;

class FileClientImpl implements FileClient {

	private final Map<Hash, WeakReference<Download>> _downloadsByHash = new HashMap<Hash, WeakReference<Download>>();

	@Override
	public Download startFileDownload(final File file, final long lastModified, final Hash hashOfFile, final Seal source) {
		return startDownload(hashOfFile, new Producer<Download>() { @Override public Download produce() {
			return my(Downloads.class).newFileDownload(file, lastModified, hashOfFile, source, downloadCleaner(hashOfFile));
		}});
	}

	@Override
	public Download startFolderDownload(final File folder, final Hash hashOfFolder) {
		return startDownload(hashOfFolder, new Producer<Download>() { @Override public Download produce() {
			return my(Downloads.class).newFolderDownload(folder, hashOfFolder, downloadCleaner(hashOfFolder));
		}});
	}

	private Download startDownload(final Hash hash, Producer<Download> downloadFactory) {
		Download result;

		synchronized (_downloadsByHash) {
			WeakReference<Download> weakRef = _downloadsByHash.get(hash);

			if (weakRef != null) {
				result = weakRef.get();
				if (result != null)
					return result;
			}

			result = downloadFactory.produce();
			_downloadsByHash.put(hash, new WeakReference<Download>(result));
		}

		return result;
	}

	private Runnable downloadCleaner(final Hash hash) { 
		return new Closure() { @Override public void run() {
			synchronized (_downloadsByHash) {
				_downloadsByHash.remove(hash);				
			}
		}};
	}

}
