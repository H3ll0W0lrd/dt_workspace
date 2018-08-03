package com.rtmap.ambassador.http;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.rtmap.ambassador.http.DTHttpUtil.ProgressListener;

public class CountingInputStream extends FilterInputStream {

	private final ProgressListener listener;
	private long transferred;
	private long fileSize;

	protected CountingInputStream(final InputStream in, long fileSize,
			final ProgressListener listener) {
		super(in);
		this.listener = listener;
		this.fileSize = fileSize;
		this.transferred = 0;
	}

	@Override
	public int read() throws IOException {
		int read = in.read();
		readCount(read);
		return read;
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		int read = in.read(buffer);
		readCount(read);
		return read;
	}

	@Override
	public int read(byte[] buffer, int byteOffset, int byteCount)
			throws IOException {
		int read = in.read(buffer, byteOffset, byteCount);
		readCount(read);
		return read;
	}

	@Override
	public long skip(long byteCount) throws IOException {
		long skip = in.skip(byteCount);
		readCount(skip);
		return skip;
	}

	private void readCount(long read) {
		if (read > 0) {
			this.transferred += read;
			if (listener != null)
				listener.transferred(transferred, fileSize);
		}
	}
}
