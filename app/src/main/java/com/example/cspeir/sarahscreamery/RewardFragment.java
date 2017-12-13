package com.example.cspeir.sarahscreamery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by cspeir on 12/6/2017.
 */

public class RewardFragment extends Fragment {
    public final static int QRcodeWidth = 500 ;
    private static final String IMAGE_DIRECTORY = "/QRcodeDemonuts";
    Bitmap bitmap ;
    private ImageView iv;
    private Button btn;
    TextView start, end, startHint, endHint;
    private Reward mReward;
    public RewardFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceSate) {
        super.onCreate(savedInstanceSate);
        setHasOptionsMenu(true);
        String rewardName, description, directions, objectId;
        Date endDate, startDate;
        Intent intent = getActivity().getIntent();
        objectId = intent.getStringExtra("objectId");
        rewardName = intent.getStringExtra("rewardName");
        description = intent.getStringExtra("description");
        directions = intent.getStringExtra("directions");
        endDate = (Date)intent.getSerializableExtra("endDate");
        startDate = (Date)intent.getSerializableExtra("startDate");
        mReward = new Reward();
        mReward.setDirection(directions);
        mReward.setDescription(description);
        mReward.setRewardName(rewardName);
        mReward.setObjectId(objectId);
        mReward.setShared(true);
        mReward.setStartDate(startDate);
        mReward.setEndDate(endDate);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, final Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_reward, parent, false);
        iv  = ( ImageView ) v.findViewById(R.id.img_result);
        final ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.simpleProgressBar);
        end = (TextView) v.findViewById(R.id.enddat_text);
        start = (TextView) v.findViewById(R.id.startdate_text);
        endHint = (TextView) v.findViewById(R.id.end_date_hint);
        startHint = (TextView) v.findViewById(R.id.start_date_hint);
        final TextView rewardNameText, rewarddescriptionText, cautionText;
        rewardNameText = (TextView) v.findViewById(R.id.qr_reward_name);
        cautionText = (TextView) v.findViewById(R.id.textView);
        rewarddescriptionText = (TextView) v.findViewById(R.id.qr_reward_description);
        btn = (Button) v.findViewById(R.id.qr_code_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                View mView = getLayoutInflater(savedInstanceState).inflate(R.layout.qr_caution, null);
                Button redeem, cancel;
                redeem = (Button) mView.findViewById(R.id.redeem_btn);
                cancel = (Button) mView.findViewById(R.id.cancel_btn);
                builder.setView(mView);
                final AlertDialog dialog = builder.create();
                dialog.show();
                redeem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mReward.getDirection().length() == 0) {
                            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                BackendlessUser currentUser = Backendless.UserService.CurrentUser();
                                if (!mReward.getRewardName().contains("Birthday")){
                                    currentUser.setProperty("rewardsUsed",currentUser.getProperty("rewardsUsed")+" "+ mReward.getObjectId());
                                }
                                else {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
                                    Date date = new Date();
                                    currentUser.setProperty("birthdayYear", currentUser.getProperty("birthdayYear")+" "+dateFormat.format(date));
                                }

                                Backendless.UserService.update(currentUser, new AsyncCallback<BackendlessUser>() {
                                    @Override
                                    public void handleResponse(BackendlessUser response) {

                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {

                                    }
                                });
                                bitmap = TextToImageEncode(mReward.getDirection());
                                iv.setImageBitmap(bitmap);
                                String path = saveImage(bitmap);
                                iv.setVisibility(View.VISIBLE);
                                btn.setVisibility(View.GONE);
                                end.setVisibility(View.GONE);
                                start.setVisibility(View.GONE);
                                endHint.setVisibility(View.GONE);
                                startHint.setVisibility(View.GONE);
                                cautionText.setVisibility(View.GONE);
                                progressBar.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                                ValueAnimator animator = ValueAnimator.ofInt(0, progressBar.getMax());
                                animator.setDuration(120000);
                                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        progressBar.setProgress((Integer) animation.getAnimatedValue());
                                    }
                                });
                                animator.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        iv.setVisibility(View.GONE);
                                        progressBar.setVisibility(View.GONE);
                                        rewardNameText.setText("Reward");
                                        rewarddescriptionText.setText("Expired");
                                    }
                                });
                                animator.start();
                                //give read write permission


                            } catch (WriterException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });


                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });


            }

        });

        rewardNameText.setText(mReward.getRewardName());
        rewarddescriptionText.setText(mReward.getDescription());
        end.setText(DateFormat.format(RewardsListFragment.DATE_FORMAT, mReward.getEndDate()));
        start.setText(DateFormat.format(RewardsListFragment.DATE_FORMAT, mReward.getStartDate()));
        return v;
    }
    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.

        if (!wallpaperDirectory.exists()) {
            Log.d("dirrrrrr", "" + wallpaperDirectory.mkdirs());
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();   //give read write permission
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(getContext(),
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";

    }
    private Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.black):getResources().getColor(R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

}
