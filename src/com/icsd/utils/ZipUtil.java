package com.icsd.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

import android.util.Log;

public class ZipUtil
{
	private static final String TAG = "ZIP_UTILITY";
	private static final int BUFFER_SIZE = 1024 * 2;
	
	public static final void unzipAll(File zipFile, File targetDir) throws IOException
	{
		Log.i(TAG, "[METHOD] void unzipAll(zipFile:" + zipFile + ", targetDir:" + targetDir + ")");

		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
		ZipEntry zentry = null;

		// if exists remove
		if (targetDir.exists())
		{
			FileUtils.deleteDirectory(targetDir);
			targetDir.mkdirs();
		}
		else
		{
			targetDir.mkdirs();
		}
		Log.d(TAG, "targetDir: " + targetDir);

		// unzip all entries
		while ((zentry = zis.getNextEntry()) != null)
		{
			String fileNameToUnzip = zentry.getName();
			File targetFile = new File(targetDir, fileNameToUnzip);

			// if directory
			if (zentry.isDirectory())
			{
				(new File(targetFile.getAbsolutePath())).mkdirs();
			}
			else
			{
				// make parent dir
				(new File(targetFile.getParent())).mkdirs();
				unzipEntry(zis, targetFile);
				Log.d(TAG, "Unzip file: " + targetFile);
			}
		}
		zis.close();
	}
	
	private static final File unzipEntry(ZipInputStream zis, File targetFile) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(targetFile);

		byte[] buffer = new byte[BUFFER_SIZE];
		int len = 0;
		while ((len = zis.read(buffer)) != -1)
		{
			fos.write(buffer, 0, len);
		}

		return targetFile;
	}
}
