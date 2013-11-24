package com.group10.battleship;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ProfileActivity extends SherlockActivity implements OnClickListener{

	private static final String TAG = ProfileActivity.class.getSimpleName();
	private static final int IMAGE_REQUEST_CODE = 1;

	private TextView mProfileName;
	private EditText mProfileNameEditor;
	
	private ImageView mProfileImage;
	private TextView mImageEditHint;
	
	private Bitmap mImageBitmap;
	// A temporary bitmap, to allow undoing
	private Bitmap mEditModeBitmap;
	private Uri mEditModeImageUri;

	private boolean mIsEditMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mProfileName = (TextView)findViewById(R.id.tv_profile_name);
		
		mProfileNameEditor = (EditText)findViewById(R.id.et_profile_name);

		mProfileImage = (ImageView)findViewById(R.id.iv_profile_image);

		mImageEditHint = (TextView)findViewById(R.id.tv_profile_image_edit_hint);
		mImageEditHint.setOnClickListener(this);
		
		refreshProfileData();
		// TODO check preferences for existing profile
		setEditMode(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.profile, menu);

		MenuItem mi;
		for (int i = 0; i < menu.size(); i++) {
			mi = menu.getItem(i);
			if (mi.getItemId() == R.id.confirm_item ) {
				if (mIsEditMode) {
					mi.setVisible(true);
				} else {
					mi.setVisible(false);
				}
			} else if (mi.getItemId() == R.id.cancel_item) {
				if (mIsEditMode) {
					mi.setVisible(true);
				} else {
					mi.setVisible(false);
				}
			} else if (mi.getItemId() == R.id.edit_item) {
				if (mIsEditMode) {
					mi.setVisible(false);
				} else {
					mi.setVisible(true);
				}
			}
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		} else if (item.getItemId() == R.id.edit_item) {
			enterEditMode();
		} else if (item.getItemId() == R.id.confirm_item) {
			confirmEdit();
		} else if (item.getItemId() == R.id.cancel_item) {
			cancelEdit();
		}
		return true;
	}
	
	private void refreshProfileData() {
		// TODO get data from user prefs
	}
	
	private void enterEditMode() {
		mProfileNameEditor.setText(mProfileName.getText());
		mEditModeBitmap = null;
		setEditMode(true);
	}
	
	private void confirmEdit() {
		// TODO save to shared prefs
		mProfileName.setText(mProfileNameEditor.getEditableText().toString());
		if (mEditModeBitmap != null) {
			// We need to recyle unused bitmaps
			if (mImageBitmap != null) {
				mImageBitmap.recycle();
			}
			mImageBitmap = mEditModeBitmap;
			mEditModeBitmap = null;
		}
		setEditMode(false);
	}
	
	private void cancelEdit() {
		// We need to recyle unused bitmaps
		if (mEditModeBitmap != null) {
			mEditModeBitmap.recycle();
			mEditModeBitmap = null;
			mProfileImage.setImageBitmap(mImageBitmap);
		}
		setEditMode(false);
	}

	private void setEditMode(boolean isEditMode) {
		mIsEditMode = isEditMode;
		if (isEditMode) {
			setTitle(R.string.profile_activity_title_edit_mode);
			mProfileName.setVisibility(View.GONE);
			mProfileNameEditor.setVisibility(View.VISIBLE);
			mImageEditHint.setVisibility(View.VISIBLE);
		} else {
			setTitle(R.string.profile_activity_title_edit_mode);
			setTitle(R.string.profile_activity_title);
			mProfileName.setVisibility(View.VISIBLE);
			mProfileNameEditor.setVisibility(View.GONE);
			mImageEditHint.setVisibility(View.GONE);
		}
		supportInvalidateOptionsMenu();
	}

	@Override
	public void onClick(View view) {
		if (view == mImageEditHint) {
			findProfileImage();
		} 	
	}

	public void findProfileImage() {
		Log.v(TAG, "Starting find image intent");
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, IMAGE_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			Log.v(TAG, "Found image: "+data.getDataString());
			try {
				InputStream stream = getContentResolver().openInputStream(
						data.getData());
				mEditModeBitmap = BitmapFactory.decodeStream(stream);
				stream.close();
				mProfileImage.setImageBitmap(mEditModeBitmap);
				mEditModeImageUri = data.getData();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
