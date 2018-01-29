/*
 * Copyright 2017 Mario Velasco Casquero
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mvc.imagepicker.sample;

import android.Manifest.permission;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mvc.imagepicker.ImagePicker;
import com.mvc.imagepicker.ImageRotator;
import com.mvc.imagepicker.ImageUtils;
import com.squareup.picasso.Picasso;
import java.util.List;

/**
 * Author: Mario Velasco Casquero
 * Date: 21/03/2017
 */

public class MainFragment extends Fragment {

  public static final String CACHED_IMG_KEY = "img_key";

  public static final int CAMERA_IMAGE = 1313;
  public static final int GALLERY_IMAGE = 1212;

  private ImageView imageView1;
  private ImageView imageView2;


  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // width and height will be at least 600px long (optional).
    ImagePicker.setMinQuality(600, 600);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_main, container, false);
    imageView1 = v.findViewById(R.id.image_view_1);
    imageView2 = v.findViewById(R.id.image_view_2);
    imageView1.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        openImageCamera();
      }
    });
    imageView2.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        openImageGallery();
      }
    });
    return v;
  }


  private void openImageGallery() {
    Dexter.withActivity(getActivity())
        .withPermissions(
            permission.READ_EXTERNAL_STORAGE,
            permission.WRITE_EXTERNAL_STORAGE,
            permission.CAMERA)
        .withListener(new MultiplePermissionsListener() {
          @Override
          public void onPermissionsChecked(MultiplePermissionsReport report) {
            if (report.areAllPermissionsGranted()) {
              ImagePicker.pickImageGallery(MainFragment.this, GALLERY_IMAGE);
            }
          }

          @Override
          public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
              PermissionToken token) {
            token.continuePermissionRequest();
          }
        })
        .onSameThread()
        .check();
  }

  private void openImageCamera() {
    Dexter.withActivity(getActivity())
        .withPermissions(
            permission.READ_EXTERNAL_STORAGE,
            permission.WRITE_EXTERNAL_STORAGE,
            permission.CAMERA)
        .withListener(new MultiplePermissionsListener() {
          @Override
          public void onPermissionsChecked(MultiplePermissionsReport report) {
            if (report.areAllPermissionsGranted()) {
              ImagePicker.pickImageCamera(MainFragment.this, CAMERA_IMAGE);
            }
          }

          @Override
          public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
              PermissionToken token) {
            token.continuePermissionRequest();
          }
        })
        .onSameThread()
        .check();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case CAMERA_IMAGE:
        String imagePathFromResult = ImagePicker.getImagePathFromCameraResult(getActivity(),
            requestCode, resultCode);
        if (imagePathFromResult != null) {
          String path = "file:///" + imagePathFromResult;
          BitmapFactory.Options bmOptions = new BitmapFactory.Options();
          Bitmap bitmap = BitmapFactory.decodeFile(imagePathFromResult, bmOptions);
          if (bitmap != null) {
            int degrees = ImageRotator.getRotation(getContext(), Uri.parse(path), true);
            bitmap = ImageRotator.rotate(bitmap, degrees);
            path = "file:///" + ImageUtils
                .savePicture(getContext(), bitmap,
                    String.valueOf(imagePathFromResult.hashCode()).concat(".jpeg"));
          }
          Picasso.with(getActivity()).load(path).into(imageView1);
        }
        break;
      case GALLERY_IMAGE:
        String pathFromGallery =
            "file:///" + ImagePicker.getImagePathFromResult(getActivity(), requestCode,
                resultCode, data);
        Picasso.with(getActivity()).load(pathFromGallery).into(imageView2);
        break;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

}
