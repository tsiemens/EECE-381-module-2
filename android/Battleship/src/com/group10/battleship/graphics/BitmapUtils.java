package com.group10.battleship.graphics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.group10.battleship.BattleshipApplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class BitmapUtils {

	/**
	 * Decodes the image into a bitmap, with the shortest edge being as close as possible to reqHeight 
	 * and reqWidth.
	 * @param imageUri
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 * @throws IOException 
	 */
	public static Bitmap decodeSampledBitmapFromUri(Uri imageUri,
			int reqWidth, int reqHeight) throws IOException {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		InputStream stream = BattleshipApplication.getAppContext().getContentResolver()
				.openInputStream(imageUri);
		BitmapFactory.decodeStream(stream, null, options);
		stream.close();

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		stream = BattleshipApplication.getAppContext().getContentResolver()
				.openInputStream(imageUri);
		Bitmap bm = BitmapFactory.decodeStream(stream, null, options);
		stream.close();
		return bm;
	}


	private static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
}
