package com.mm.photo.storage;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mm.photo.proto.Storage.ImageFragInfo;
import com.mm.photo.proto.Storage.ImageKey;

public class LevelDBFile extends ByteSource {

	static Logger LOG = LoggerFactory.getLogger(LevelDBFile.class);

	DB db;
	ImageKey key;
	ImageFragInfo data;
	ByteSource wrapSource = null;

	public LevelDBFile(DB db, ImageKey key) {
		this.db = db;
		this.key = key;
	}

	public ImageKey getKey() {
		return key;
	}

	public boolean isExist()
	{
		try
		{
			if (getFragInfo() == null)
			{
				return false;
			}
		}catch(Throwable e)
		{
			return false;
		}
		
		return true;
	}
	
	public ImageFragInfo getFragInfo() throws FileNotFoundException{
		if (data == null) {
			try {
				data = ImageFragInfo.parseFrom(db.get(getKey().toByteArray()));
			} catch (InvalidProtocolBufferException | DBException e) {
				throw new FileNotFoundException("db has not ImageKey:"
						+ getKey().toString());
			}
		}
		return data;
	}

	boolean isSplit() throws FileNotFoundException{
		return getFragInfo().getIsSplit();
	}

	@Override
	public InputStream openStream() throws IOException {
		if (!isSplit()) {
			return getFragInfo().getData().newInput();
		} else {

			if (wrapSource == null) {
				List<LevelDBData> datas = Lists.newArrayList();
				for (ImageKey k : getFragInfo().getFrageKeysList()) {
					datas.add(new LevelDBData(db, k.toByteArray()));
				}
				wrapSource = ByteSource.concat(datas);
			}
			return wrapSource.openStream();
		}

	}
	
	public Builder newBuilder()
	{
		return new Builder(db, key);
	}

	static class Builder extends ByteSink {

		DB db;
		ImageKey key;

		static final int BYTES_PIECE_SIZE = 16 * 1024;

		public Builder(DB db, ImageKey key) {
			this.db = db;
			this.key = key;
		}

		public LevelDBFile build() {
			return new LevelDBFile(db, key);
		}

		@Override
		public OutputStream openStream() throws IOException {
			return new BufferedOutputStream(new BuilderOutputStream(db, key),
					BYTES_PIECE_SIZE);
		}

		static class BuilderOutputStream extends OutputStream {

			ImageKey key;
			DB db;
			ImageFragInfo.Builder fragInfoBuilder;
			int cur_piece = 0;
			byte[] direct_bytes;

			String keyFormat(String url, int id) {
				return String.format("%s-[%d]", url, id);
			}

			public BuilderOutputStream(DB db, ImageKey key) {
				this.key = key;
				this.db = db;
				fragInfoBuilder = ImageFragInfo.newBuilder();
			}

			@Override
			public void write(int b) throws IOException {
				throw new UnsupportedOperationException(
						"please wrapper BufferOutputStream()");
			}

			ImageKey buildPieceKey(int id) {
				return ImageKey.newBuilder()
						.setUrl(keyFormat(key.getUrl(), id)).build();
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				if (cur_piece == 0) {
					direct_bytes = Arrays.copyOfRange(b, off, off + len);
				} else {
					if (cur_piece == 1) {
						fragInfoBuilder.setIsSplit(true);
						ImageKey k = buildPieceKey(0);
						db.put(k.toByteArray(), direct_bytes);
						fragInfoBuilder.addFrageKeys(k);
					}
					ImageKey k = buildPieceKey(cur_piece);
					db.put(k.toByteArray(),
							Arrays.copyOfRange(b, off, off + len));
					fragInfoBuilder.addFrageKeys(k);
				}
				cur_piece++;
			}

			@Override
			public void close() throws IOException {
				if (cur_piece != 0)
				{
					if (cur_piece == 1) {
						fragInfoBuilder.setIsSplit(false);
						fragInfoBuilder.setData(ByteString.copyFrom(direct_bytes));
					} else {
						fragInfoBuilder.setIsSplit(true);
					}
					db.put(key.toByteArray(), fragInfoBuilder.build().toByteArray());
				}
				super.close();
			}

		}

	}

}
