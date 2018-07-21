package com.hellosscat;

import com.mpatric.mp3agic.*;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 获得MP3文件的信息
 * <p>
 * Mp3 最后128字节存储信息格式：
 * 字节     长度 (字节)  说       明
 * 1-3     3       存放“TAG”字符，表示ID3 V1.0标准，紧接其后的是歌曲信息。
 * 4-33    30      歌名
 * 34-63   30      作者
 * 64-93   30      专辑名
 * 94-97   4       年份
 * 98-127  30      附注
 * 128     1       MP3音乐类别，共147种。
 */
public class Mp3FileDeal {
	private static final String KeyWords = "(V信公号hellosscat)";
	private static final String Auth = "V信公号:霜霜的菜和茶 | hellosscat";
	private static final String Architor = "WX:hellosscat2 | QQ:1825136213";
	private static final String WX_OPEN = "一手资源,V信公号hellosscat";
	private static final String QQ = "QQ:1825136213 付费资源获取 | V信公号: hellosscat";
	private static final String Web = "https://www.alibrother.top";
	private static final File coverImg = new File("G:/公众号推广/cover.jpg");
	private static final File WX_OPEN_FILE = new File("G:/公众号推广/微信公众号.jpg");
	private static final File WX_FILE = new File("G:/公众号推广/微信.jpg");

	private static final String InvalidKeyWords = "（v信：hanniu907）";
	private static final String ValidKeyWords = KeyWords;
	private static final String InvalidKeyWords2 = "hanniu907";
	private static final String ValidKeyWords2 = "hellosscat2";
	private static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(16, 32, 120, TimeUnit.SECONDS,new SynchronousQueue<Runnable>(),
			new ThreadPoolExecutor.CallerRunsPolicy());
	private static AtomicLong count = new AtomicLong(0);
	private static byte[] coverData;

	static{
		try {
			BufferedImage bufferedImage = ImageIO.read(coverImg);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "jpg", outputStream);
			coverData = outputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}




	@Test
	public void begin() throws IOException, InvalidDataException, NotSupportedException, UnsupportedTagException, InterruptedException {
//		this.copy(new File("I:/知识付费/test"), "I:/知识付费/专栏产品2");
//		this.copy(new File("I:/知识付费/02 得 到 专 栏"), "I:/知识付费/target");
//		this.copy(new File("I:/知识付费/01喜 马 拉 雅 课 程"), "I:/知识付费/target");
		this.copy(new File("I:/知识付费/03 樊 登 读 书 会"), "I:/知识付费/target");
		while (true) {

			System.out.println("---count="+ count.get() + "---");
			Thread.currentThread().sleep(50000);
		}
	}

	/**
	 * 文件拷贝
	 *
	 * @param srcFile
	 * @param targetPath
	 * @throws IOException
	 */
	private void copy(File srcFile, String targetPath) throws IOException, InvalidDataException, NotSupportedException, UnsupportedTagException, InterruptedException {
		count.incrementAndGet();
		if (srcFile.isFile()) {
			doFile(srcFile, targetPath);
		}
		if (srcFile.isDirectory()) {
			doDirectory(srcFile, targetPath);
		}
	}

	private void doDirectory(File srcFile, String targetPath) throws IOException, InvalidDataException, NotSupportedException, UnsupportedTagException, InterruptedException {
//		System.out.println("Src AbsolutePath=" + srcFile.getCanonicalPath());
		final String targetAbsolutePath = genTargetAbsoluteFile(srcFile, targetPath);
		File targetFile = new File(targetAbsolutePath);
		targetFile.mkdir();
		System.out.println("Target Directory create Success. directory=" + targetAbsolutePath);
//		System.out.println("---");
		copy(WX_OPEN_FILE, targetAbsolutePath);
		copy(WX_FILE, targetAbsolutePath);
		File[] subFiles = srcFile.listFiles();
//		final CountDownLatch latch = new CountDownLatch(subFiles.length);
		for (File subFile : subFiles) {
			threadPool.submit(new Runnable() {
				@Override
				public void run() {
					try {
						copy(subFile, targetAbsolutePath);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
//						latch.countDown();
					}
				}
			});
		}
//		latch.await(120000, TimeUnit.MILLISECONDS);
	}

	private void doFile(File srcFile, String targetPath) throws InvalidDataException, IOException, UnsupportedTagException, NotSupportedException {
//		System.out.println("Src FileName=" + srcFile.getCanonicalPath());
		String targetFile = genTargetAbsoluteFile(srcFile, targetPath);
		// 处理MP3文件
		if (srcFile.getName().trim().endsWith(".mp3")) {
			copyMP3FileWithOverrideID3(srcFile, targetFile);
			System.out.println("Target File copy Success, file = " + targetFile);
//			System.out.println("---");
		} else {
			copyFile(srcFile, new File(targetFile));
			System.out.println("Target MP3 override Success, file = " + targetFile);
//			System.out.println("---");
		}
	}



	private String genTargetAbsoluteFile(File srcFile, String targetPath) {
		String targetAbsoluteFile = targetPath + "/" + srcFile.getName().replaceAll(InvalidKeyWords, ValidKeyWords).replaceAll(InvalidKeyWords2, ValidKeyWords2);
		if (srcFile.isDirectory()) {
			targetAbsoluteFile += KeyWords;
		} else {
			String fullFileName = targetAbsoluteFile.trim();
			String subfix = fullFileName.substring(fullFileName.lastIndexOf("."));
			int num = subfix.length();//得到后缀名长度
			String filedName = fullFileName.substring(0, fullFileName.length() - num);//得到文件名。去掉了后缀
			targetAbsoluteFile = filedName + KeyWords + subfix;
		}
//		System.out.println("Gen TargetFile=" + targetAbsoluteFile);
		return targetAbsoluteFile;
	}

	private void copyFile(File s, File t) {
		FileInputStream fi = null;
		FileOutputStream fo = null;
		FileChannel in = null;
		FileChannel out = null;
		try {
			fi = new FileInputStream(s);
			fo = new FileOutputStream(t);
			in = fi.getChannel();//得到对应的文件通道
			out = fo.getChannel();//得到对应的文件通道
			in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fi.close();
				in.close();
				fo.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void test() throws UnsupportedTagException, NotSupportedException, InvalidDataException, IOException {
//		System.out.println(genTargetAbsoluteFile(
//				new File("I:\\知识付费\\test\\02 得 到 专 栏\\16 吴.军.的.谷.歌.方.法.论\\05月\\wj20180501第090封信从一家伟大公司的衰落谈谈大爱和小爱.mp3"),
//				"I:/知识付费"));

		copyMP3FileWithOverrideID3(new File("G:/公众号推广/xs1230.mp3"),"G:/公众号推广/test.mp3");
		copyMP3FileWithOverrideID3(new File("G:/公众号推广/wj20180501第090封信从一家伟大公司的衰落谈谈大爱和小爱.mp3"),"G:/公众号推广/test2.mp3");
	}


	private void copyMP3FileWithOverrideID3(File sourceFile, String targetAbsoluteFile) throws InvalidDataException, IOException, UnsupportedTagException, NotSupportedException {
		try {
			Mp3File mp3file = new Mp3File(sourceFile);
			ID3v2 id3v2Tag;
			if (mp3file.hasId3v2Tag()) {
                id3v2Tag = mp3file.getId3v2Tag();
            } else {
                // mp3 does not have an ID3v2 tag, let's create one..
                id3v2Tag = new ID3v24Tag();
                mp3file.setId3v2Tag(id3v2Tag);
            }
			id3v2Tag.setTrack(Auth);
//			id3v2Tag.setTitle(WX_OPEN);
			id3v2Tag.setAlbum(WX_OPEN);
			id3v2Tag.setYear("2018");
			id3v2Tag.setGenre(12);
			id3v2Tag.setComment(WX_OPEN);
			id3v2Tag.setLyrics(WX_OPEN);
			id3v2Tag.setPublisher(WX_OPEN);
			id3v2Tag.setOriginalArtist(QQ);
			id3v2Tag.setAlbumArtist(Architor);
			id3v2Tag.setComposer(Auth);
			id3v2Tag.setArtist(Architor);
			id3v2Tag.setCopyright(Auth);
			id3v2Tag.setUrl(Web);
			id3v2Tag.setEncoder(QQ);
			id3v2Tag.setAudiofileUrl(Web);
			id3v2Tag.setAudioSourceUrl(Web);
			id3v2Tag.setCommercialUrl(Web);
			id3v2Tag.setCopyrightUrl(Web);
			id3v2Tag.setGrouping(WX_OPEN);
			id3v2Tag.setKey(WX_OPEN);
			id3v2Tag.setPaymentUrl(Web);
			id3v2Tag.setRadiostationUrl(Web);
			id3v2Tag.setPartOfSet(WX_OPEN);

			ID3v1 id3v1Tag;
			if (mp3file.hasId3v1Tag()) {
                id3v1Tag = mp3file.getId3v1Tag();
            } else {
                // mp3 does not have an ID3v1 tag, let's create one..
                id3v1Tag = new ID3v1Tag();
                mp3file.setId3v1Tag(id3v1Tag);
            }
			id3v1Tag.setTrack(Auth);
			id3v1Tag.setArtist(Architor);
			id3v1Tag.setTitle(WX_OPEN);
			id3v1Tag.setAlbum(Architor);
			id3v1Tag.setYear("2018");
			id3v1Tag.setGenre(12);
			id3v1Tag.setComment(QQ);
//			BufferedImage bufferedImage = ImageIO.read(coverImg);
//			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//			ImageIO.write(bufferedImage, "jpg", outputStream);
//			byte[] coverData = outputStream.toByteArray();
			id3v2Tag.setAlbumImage(coverData, "image/jpeg");
			mp3file.save(targetAbsoluteFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void read() throws IOException, InvalidDataException, UnsupportedTagException {
		String sourceFile = "G:/公众号推广/xs1230.mp3";
		Mp3File mp3file = new Mp3File(sourceFile);
		System.out.println("Length of this mp3 is: " + mp3file.getLengthInSeconds() + " seconds");
		System.out.println("Bitrate: " + mp3file.getBitrate() + " kbps " + (mp3file.isVbr() ? "(VBR)" : "(CBR)"));
		System.out.println("Sample rate: " + mp3file.getSampleRate() + " Hz");
		System.out.println("Has ID3v1 tag?: " + (mp3file.hasId3v1Tag() ? "YES" : "NO"));
		System.out.println("Has ID3v2 tag?: " + (mp3file.hasId3v2Tag() ? "YES" : "NO"));
		System.out.println("Has custom tag?: " + (mp3file.hasCustomTag() ? "YES" : "NO"));

		System.out.println("---Id3v1 Info---");
		if (mp3file.hasId3v1Tag()) {
			ID3v1 id3v1Tag = mp3file.getId3v1Tag();
			System.out.println("Track: " + id3v1Tag.getTrack());
			System.out.println("Artist: " + id3v1Tag.getArtist());
			System.out.println("Title: " + id3v1Tag.getTitle());
			System.out.println("Album: " + id3v1Tag.getAlbum());
			System.out.println("Year: " + id3v1Tag.getYear());
			System.out.println("Genre: " + id3v1Tag.getGenre() + " (" + id3v1Tag.getGenreDescription() + ")");
			System.out.println("Comment: " + id3v1Tag.getComment());
		}

		System.out.println();
		System.out.println("---Id3v2 Info---");

		if (mp3file.hasId3v2Tag()) {
			ID3v2 id3v2Tag = mp3file.getId3v2Tag();
			System.out.println("Track: " + id3v2Tag.getTrack());
			System.out.println("Artist: " + id3v2Tag.getArtist());
			System.out.println("Title: " + id3v2Tag.getTitle());
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

				BufferedImage img = ImageIO.read(new ByteArrayInputStream(albumImageData));
				File cover = new File("G:/公众号推广/old.jpg");
				ImageIO.write(img, "jpg", cover);
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (threadPool != null && !threadPool.isShutdown()) {
			threadPool.shutdownNow();
		}
		super.finalize();
	}
}
