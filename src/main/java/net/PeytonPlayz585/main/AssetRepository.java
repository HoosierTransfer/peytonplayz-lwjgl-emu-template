package net.PeytonPlayz585.main;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

import com.jcraft.jzlib.InflaterInputStream;

public class AssetRepository {

	private static int numFiles;
	private static CRC32 crc32;
	private static boolean isFinished = false;
	private static InputStream zis;

	private static final HashMap<String, byte[]> filePool = new HashMap();

	public static final void install(byte[] data) throws IOException {
		ByteArrayInputStream in2 = new ByteArrayInputStream(data);
		byte[] header = new byte[8];
		readFully(in2, header);
		if (Arrays.equals(header, new byte[] { (byte) 69, (byte) 65, (byte) 71, (byte) 80, (byte) 75, (byte) 71,
				(byte) 36, (byte) 36 })) {
			byte[] endCode = new byte[] { (byte) ':', (byte) ':', (byte) ':', (byte) 'Y',
					(byte) 'E', (byte) 'E', (byte) ':', (byte) '>' };
			for (int i = 0; i < 8; ++i) {
				if (data[data.length - 8 + i] != endCode[i]) {
					throw new IOException("EPK file is missing EOF code (:::YEE:>)");
				}
			}

			in2 = new ByteArrayInputStream(data, 8, data.length - 16);
			InputStream is = in2;

			String vers = readASCII(is);
			if (!vers.startsWith("ver2.")) {
				throw new IOException("Unknown or invalid EPK version: " + vers);
			}

			skipFully(is, is.read());
			skipFully(is, loadShort(is));
			skipFully(is, 8);

			numFiles = loadInt(is);

			char compressionType = (char) is.read();

			switch (compressionType) {
				case 'G':
					zis = new InflaterInputStream(is);
					break;
				case 'Z':
					zis = new GZIPInputStream(is);
					break;
				case '0':
					zis = is;
					break;
				default:
					throw new IOException("Invalid or unsupported EPK compression: " + compressionType);
			}

			crc32 = new CRC32();
		} else if (Arrays.equals(header, new byte[] { (byte) 69, (byte) 65, (byte) 71, (byte) 80, (byte) 75, (byte) 71,
				(byte) 33, (byte) 33 })) {
			throw new IOException("FILE IS AN UNSUPPORTED LEGACY FORMAT!");
		} else {
			throw new IOException("FILE IS NOT AN EPK FILE!");
		}

		while (numFiles > 0) {
			FileEntry entry = readFile();
			if (entry != null) {
				filePool.put(entry.name, entry.data);
			}
		}
	}

	public static FileEntry readFile() throws IOException {
		if (isFinished) {
			return null;
		}

		byte[] typeBytes = new byte[4];
		readFully(zis, typeBytes);
		String type = readASCII(typeBytes);
		if (numFiles == 0) {
			if (!"END$".equals(type)) {
				throw new IOException("EPK file is missing END code (END$)");
			}
			isFinished = true;
			return null;
		} else {
			if ("END$".equals(type)) {
				throw new IOException("Unexpected END when there are still " + numFiles + " files remaining");
			} else {
				String name = readASCII(zis);
				int len = loadInt(zis);
				byte[] data;

				if ("FILE".equals(type)) {
					if (len < 5) {
						throw new IOException("File '" + name + "' is incomplete (no crc)");
					}

					int loadedCrc = loadInt(zis);

					data = new byte[len - 5];
					IOUtils.readFully(zis, data);

					crc32.reset();
					crc32.update(data, 0, data.length);
					if ((int) crc32.getValue() != loadedCrc) {
						throw new IOException("File '" + name + "' has an invalid checksum");
					}

					if (zis.read() != ':') {
						throw new IOException("File '" + name + "' is incomplete");
					}
				} else {
					data = new byte[len];
					IOUtils.readFully(zis, data);
				}

				if (zis.read() != '>') {
					throw new IOException("Object '" + name + "' is incomplete");
				}

				--numFiles;
				return new FileEntry(type, name, data);
			}
		}
	}

	public static final byte[] getResource(String path) {
		if (path.startsWith("/"))
			path = path.substring(1);
		return filePool.get(path);
	}

	public static final int loadShort(InputStream is) throws IOException {
		return (is.read() << 8) | is.read();
	}

	public static final int loadInt(InputStream is) throws IOException {
		return (is.read() << 24) | (is.read() << 16) | (is.read() << 8) | is.read();
	}

	public static final String readASCII(byte[] bytesIn) throws IOException {
		char[] charIn = new char[bytesIn.length];
		for (int i = 0; i < bytesIn.length; ++i) {
			charIn[i] = (char) ((int) bytesIn[i] & 0xFF);
		}
		return new String(charIn);
	}

	public static final String readASCII(InputStream bytesIn) throws IOException {
		int len = bytesIn.read();
		char[] charIn = new char[len];
		for (int i = 0; i < len; ++i) {
			charIn[i] = (char) (bytesIn.read() & 0xFF);
		}
		return new String(charIn);
	}

	public static long skipFully(InputStream is, long skip) throws IOException {
		long i = 0, j;
		while (i < skip && (j = is.skip(skip - i)) != 0) {
			i += j;
		}
		return i;
	}

	public static int readFully(InputStream is, byte[] out) throws IOException {
		int i = 0, j;
		while (i < out.length && (j = is.read(out, i, out.length - i)) != -1) {
			i += j;
		}
		return i;
	}

	public static class FileEntry {
		public final String type;
		public final String name;
		public final byte[] data;

		protected FileEntry(String type, String name, byte[] data) {
			this.type = type;
			this.name = name;
			this.data = data;
		}
	}

}