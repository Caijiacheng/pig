package com.mm.photo.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.iq80.leveldb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.mm.photo.proto.Storage.ImageFragInfo;
import com.mm.photo.proto.Storage.ImageKey;

public class LevelDBFile extends ByteSource {
	
	static Logger LOG = LoggerFactory.getLogger(LevelDBFile.class);
	
	DB db;
	ImageKey key;
	ByteSource wrapSource = null;

	public LevelDBFile(DB db, ImageKey key) {
		this.db = db;
		this.key = key;
	}

	public ImageKey getKey() {
		return key;
	}

	@Override
	public InputStream openStream() throws IOException {
		if (wrapSource == null) {
			if (!key.getIsSplit()) {
				wrapSource = new LevelDBData(db, key.toByteArray());
			} else {

				ImageFragInfo frags = ImageFragInfo.parseFrom(db.get(key
						.toByteArray()));
				List<LevelDBData> datas = Lists.newArrayList();
				for (ImageKey k : frags.getFrageKeysList()) {
//					System.err.println("frage key: " + k.getUrl());
					datas.add(new LevelDBData(db, k.toByteArray()));
				}
				wrapSource = ByteSource.concat(datas);
			}
		}
		return wrapSource.openStream();
	}

	static class Builder extends ByteSink {

		DB db;
		ImageKey.Builder keybuilder;
		int cnt = 0;
		ByteArrayOutputStream lastOutput = null;

		List<BuilderOutputStream> allOtherOpens = Lists.newArrayList();

		public Builder(DB db, ImageKey.Builder keybuilder) {

			Preconditions.checkArgument(keybuilder.hasUrl(),
					"ImageKey Builder must has url");

			this.db = db;
			this.keybuilder = keybuilder;

		}

		public LevelDBFile build() {
			Preconditions.checkArgument(lastOutput != null,
					"not any data to build the photo");
			if (cnt == 0) {
				keybuilder.setIsSplit(false);
				ImageKey key = keybuilder.build();
				db.put(key.toByteArray(), lastOutput.toByteArray());

			} else {

				ImageFragInfo.Builder frags = ImageFragInfo.newBuilder();

				ImageKey key0 = ImageKey.newBuilder().setIsSplit(false)
						.setUrl(getStoreKey(keybuilder.getUrl(), 0)).build();
//				LOG.error("db.put:{}, bytes[]:{}", key0.getUrl(), key0.toByteArray());
				db.put(key0.toByteArray(), lastOutput.toByteArray());
				frags.addFrageKeys(key0);
				for (BuilderOutputStream other : allOtherOpens) {
					Preconditions.checkArgument(other.isClosed,
							"DB Data Leak: " + other.toString());
					frags.addFrageKeys(other.getKey());
				}
				keybuilder.setIsSplit(true);
				db.put(keybuilder.build().toByteArray(), frags.build()
						.toByteArray());

			}
			return new LevelDBFile(db, keybuilder.build());
		}

		static String getStoreKey(String url, int id) {
			return String.format("%s-[%d]", url, id);
		}

		@Override
		public OutputStream openStream() throws IOException {
			if (lastOutput == null) {
				lastOutput = new ByteArrayOutputStream();
				return lastOutput;
			}
			ImageKey key = ImageKey.newBuilder().setIsSplit(false)
					.setUrl(getStoreKey(keybuilder.getUrl(), ++cnt)).build();
			BuilderOutputStream next = new BuilderOutputStream(key);
			allOtherOpens.add(next);
			return next;
		}

		class BuilderOutputStream extends ByteArrayOutputStream {
			ImageKey key;
			boolean isClosed = false;

			public BuilderOutputStream(ImageKey key) {
				this.key = key;
			}

			public ImageKey getKey() {
				return key;
			}

			public boolean isClose() {
				return isClosed;
			}

			@Override
			public void close() throws IOException {
				if (!isClosed) {
					Preconditions.checkArgument(size() != 0,
							"OutputStream not do any write!");
					isClosed = true;
//					LOG.error("db.put:{}, bytes[]:{}", key, key.toByteArray());
					db.put(key.toByteArray(), toByteArray());
				}
				super.close();
			}
		}

	}

}
